package fr.nekotine.core.bot;

import fr.nekotine.core.bot.internal.BotBody;
import fr.nekotine.core.bot.internal.BotSelfTest;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.ticking.TickingModule;
import fr.nekotine.core.ticking.event.TickElapsedEvent;
import fr.nekotine.core.util.EventUtil;
import io.papermc.paper.event.player.PlayerFailMoveEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Spawns, lists, ticks and removes the host plugin's bots. Resolve it with
 * {@code Ioc.resolve(BotRoster.class)}. Every method is main-thread only.
 *
 * <p>
 * Resolve it eagerly in the host's {@code onEnable()}, after
 * {@code super.onEnable()}: Core loads modules lazily and only logs constructor
 * errors, and the roster runs its startup self-test when it is constructed. The
 * self-test resolves every server internal the library links against; it logs
 * {@code Bots self-test passed} on success. On failure it logs one error naming
 * each broken accessor, the roster never ticks, {@link #isAvailable()} is false
 * and {@link #spawn}, {@link #remove} and {@link #list} throw
 * {@link BotsUnavailableException}; the host and the server keep running.
 *
 * <p>
 * Each server tick, every live bot, in spawn order: runs its connection tick,
 * sends its pending client acks, fires its {@link BotTickEvent}, sends its
 * changed inputs, runs its physics step and sends its move, then its slot,
 * hands, commands and respawn (see {@link Bot}), then flushes; a bot whose
 * connection closed then leaves. Bots spawned or removed during this loop (from
 * a listener) join or leave the loop at its end: a spawned bot joins the server
 * at once but is listed and ticked only after the loop; a removed bot is not
 * ticked again and leaves after the loop.
 *
 * <p>
 * If the library's own work for a bot throws, the error is logged once, the bot
 * becomes {@link BotState#FAILED} and stops ticking (no connection tick, acks
 * or event) while the other bots carry on; it stays connected and listed until
 * removed. Exceptions from {@link BotTickEvent} listeners are the server's to
 * log and never fail the bot.
 *
 * <p>
 * Each {@code PlayerFailMoveEvent} the server fires for one of its bots (a move
 * it found wrong, too quick, into an unloaded chunk or into a block) is logged
 * as one warning starting with the bot's name, with the reason, the target and
 * whether the move was refused.
 *
 * <p>
 * Every exit (removal, {@link #unload()}, kick, lost connection) takes the same
 * path: flush, close, the server's quit ({@code PlayerQuitEvent}, then save),
 * drop from the roster, then delete the saved files its {@link BotWipe}
 * selects. {@link #unload()} removes every bot: bots never outlive the host.
 */
public class BotRoster implements IPluginModule, Listener {

	private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

	private final ComponentLogger logger = NekotineLogger.make();

	private final List<BotBody> bots = new ArrayList<>();

	/** Bots spawned during the tick loop, listed once it ends. */
	private final List<BotBody> pendingSpawns = new ArrayList<>();

	/** Bots removed during the tick loop, leaving once it ends. */
	private final Set<BotBody> pendingRemovals = new LinkedHashSet<>();

	private boolean ticking;

	private final boolean available;

	/**
	 * Runs the startup self-test, then, if it passed, starts ticking. Never throws
	 * on a failed self-test: the roster is then unavailable.
	 */
	public BotRoster() {
		available = selfTest();
		Ioc.resolve(TickingModule.class);
		if (available) {
			EventUtil.register(this);
		}
	}

	private boolean selfTest() {
		BotSelfTest.Result result;
		try {
			result = BotSelfTest.run(BotRoster.class.getClassLoader());
		} catch (RuntimeException | LinkageError e) {
			logger.error("Bots self-test failed to run, bots are disabled", e);
			return false;
		}
		if (result.passed()) {
			logger.info(
					"Bots self-test passed: " + result.accessors() + " accessors in " + result.classes() + " classes");
			return true;
		}
		logger.error("Bots self-test failed, bots are disabled (BotRoster calls throw BotsUnavailableException). "
				+ "Server internals the bots library uses are missing or changed, usually after a Paper update: "
				+ String.join(", ", result.broken()));
		return false;
	}

	/**
	 * @return whether the startup self-test passed; if not, {@link #spawn},
	 *         {@link #remove} and {@link #list} throw
	 *         {@link BotsUnavailableException}
	 */
	public boolean isAvailable() {
		return available;
	}

	private void requireAvailable() {
		if (!available) {
			throw new BotsUnavailableException(
					"Bots unavailable: the bots self-test failed at startup, see the server log");
		}
	}

	/**
	 * Joins a new bot named {@code name} at {@code location}, with every saved file
	 * wiped once it leaves ({@link BotWipe#ALL}).
	 *
	 * @see #spawn(String, Location, BotWipe)
	 */
	public Bot spawn(String name, Location location) {
		return spawn(name, location, BotWipe.ALL);
	}

	/**
	 * Joins a new bot named {@code name} at {@code location}, firing
	 * {@code PlayerJoinEvent}. Its UUID is the offline-mode UUID of its name. Once
	 * it has left, the saved files {@code wipe} selects are deleted. Called during
	 * the tick loop, the bot joins at once but is listed and ticked only once the
	 * loop ends.
	 *
	 * @throws IllegalArgumentException
	 *             if the name is invalid or a player or bot with that name is
	 *             online, or the location has no world
	 * @throws IllegalStateException
	 *             off the main thread, or if the join failed
	 * @throws BotsUnavailableException
	 *             if the startup self-test failed
	 */
	public Bot spawn(String name, Location location, BotWipe wipe) {
		requireAvailable();
		requireMainThread();
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(wipe, "wipe");
		if (!NAME.matcher(name).matches()) {
			throw new IllegalArgumentException("Invalid bot name " + name + ": expected 1-16 of [A-Za-z0-9_]");
		}
		if (location.getWorld() == null) {
			throw new IllegalArgumentException("Cannot spawn bot " + name + ": location has no world");
		}
		if (Bukkit.getPlayerExact(name) != null || Bukkit.getPlayer(BotBody.offlineUuid(name)) != null || Stream
				.concat(bots.stream(), pendingSpawns.stream()).anyMatch(b -> b.name().equalsIgnoreCase(name))) {
			throw new IllegalArgumentException(
					"Cannot spawn bot " + name + ": a player or bot with that name is already online");
		}
		BotBody body;
		try {
			body = BotBody.join(name, location.clone(), wipe);
		} catch (Exception e) {
			throw new IllegalStateException("Bot " + name + " failed to join", e);
		}
		(ticking ? pendingSpawns : bots).add(body);
		logger.info(name + " joined at " + location.getWorld().getName() + " " + location.getX() + " " + location.getY()
				+ " " + location.getZ());
		return body;
	}

	/**
	 * Removes a bot through the server's quit path (fires {@code PlayerQuitEvent}),
	 * drops it from the roster, then deletes its saved files as its {@link BotWipe}
	 * says. Called during the tick loop, the bot is not ticked again and leaves
	 * once the loop ends. Removing an unknown or already removed bot does nothing.
	 * Works on a {@link BotState#FAILED} bot.
	 *
	 * @throws IllegalStateException
	 *             off the main thread
	 * @throws BotsUnavailableException
	 *             if the startup self-test failed
	 */
	public void remove(Bot bot) {
		requireAvailable();
		requireMainThread();
		if (!(bot instanceof BotBody body) || !(bots.contains(body) || pendingSpawns.contains(body))
				|| body.isRemoved()) {
			return;
		}
		if (ticking) {
			pendingRemovals.add(body);
			return;
		}
		exit(body);
	}

	/**
	 * @return the roster's bots, in spawn order (a snapshot), failed ones included
	 * @throws BotsUnavailableException
	 *             if the startup self-test failed
	 */
	public List<Bot> list() {
		requireAvailable();
		requireMainThread();
		return List.copyOf(bots);
	}

	@EventHandler
	public void onTick(TickElapsedEvent event) {
		ticking = true;
		try {
			for (var body : List.copyOf(bots)) {
				if (body.isRemoved() || pendingRemovals.contains(body)) {
					continue;
				}
				tick(body);
				if (!body.isRemoved() && !pendingRemovals.contains(body) && !body.isConnected()) {
					logger.info(body.name() + " lost its connection, removing it");
					exitQuietly(body);
				}
			}
		} finally {
			ticking = false;
		}
		bots.addAll(pendingSpawns);
		pendingSpawns.clear();
		while (!pendingRemovals.isEmpty()) {
			var body = pendingRemovals.iterator().next();
			pendingRemovals.remove(body);
			exitQuietly(body);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFailMove(PlayerFailMoveEvent event) {
		var player = event.getPlayer();
		var body = Stream.concat(bots.stream(), pendingSpawns.stream())
				.filter(b -> !b.isRemoved() && b.player().equals(player)).findFirst().orElse(null);
		if (body == null) {
			return;
		}
		var to = event.getTo();
		logger.warn(body.name() + " move " + (event.isAllowed() ? "allowed" : "refused") + " by the server ("
				+ event.getFailReason() + ") to " + to.getWorld().getName() + " " + to.getX() + " " + to.getY() + " "
				+ to.getZ());
	}

	private void tick(BotBody body) {
		if (body.state() == BotState.FAILED) {
			body.tickFailed();
			return;
		}
		if (!guard(body, body::tickBeforeEvent) || !body.isConnected()) {
			return;
		}
		// Not guarded: host listener exceptions are Paper's to log.
		Bukkit.getPluginManager().callEvent(new BotTickEvent(body));
		if (body.isRemoved() || pendingRemovals.contains(body)) {
			// Its exit flushes.
			return;
		}
		guard(body, body::tickAfterEvent);
	}

	/** Runs library work for a bot; a throw fails only that bot, logged once. */
	private boolean guard(BotBody body, Runnable work) {
		try {
			work.run();
			return true;
		} catch (RuntimeException | LinkageError e) {
			body.fail();
			logger.error(body.name() + " failed, it stops ticking", e);
			return false;
		}
	}

	@Override
	public void unload() {
		// Harmless when unavailable: the roster never registered and holds no bot.
		EventUtil.unregister(this);
		var all = new ArrayList<>(bots);
		all.addAll(pendingSpawns);
		pendingSpawns.clear();
		pendingRemovals.clear();
		ticking = false;
		for (var body : all) {
			exitQuietly(body);
		}
	}

	private void exitQuietly(BotBody body) {
		try {
			exit(body);
		} catch (RuntimeException e) {
			logger.error(body.name() + " could not be removed", e);
		}
	}

	/**
	 * The single exit path: flush, close, quit and save, drop from the roster,
	 * wipe. A re-entrant call (from a {@code PlayerQuitEvent} listener) is a no-op.
	 */
	private void exit(BotBody body) {
		if (body.isRemoved()) {
			return;
		}
		try {
			body.remove();
		} finally {
			bots.remove(body);
			pendingSpawns.remove(body);
			pendingRemovals.remove(body);
		}
		logger.info(body.name() + " left");
		body.wipe(logger);
	}

	private static void requireMainThread() {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("BotRoster must be used from the main thread");
		}
	}
}
