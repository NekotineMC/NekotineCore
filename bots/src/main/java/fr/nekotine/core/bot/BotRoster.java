package fr.nekotine.core.bot;

import fr.nekotine.core.bot.internal.BotBody;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.ticking.TickingModule;
import fr.nekotine.core.ticking.event.TickElapsedEvent;
import fr.nekotine.core.util.EventUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Spawns, lists, ticks and removes the host plugin's bots. Resolve it with
 * {@code Ioc.resolve(BotRoster.class)}. Every method is main-thread only.
 *
 * <p>
 * Each server tick, every bot (in spawn order) runs its connection tick, sends
 * its pending client acks and flushes. {@link #unload()} removes every bot:
 * bots never outlive the host.
 */
public class BotRoster implements IPluginModule, Listener {

	private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

	private final ComponentLogger logger = NekotineLogger.make();

	private final List<BotBody> bots = new ArrayList<>();

	public BotRoster() {
		Ioc.resolve(TickingModule.class);
		EventUtil.register(this);
	}

	/**
	 * Joins a new bot named {@code name} at {@code location}, firing
	 * {@code PlayerJoinEvent}. Its UUID is the offline-mode UUID of its name.
	 *
	 * @throws IllegalArgumentException
	 *             if the name is invalid or a player or bot with that name is
	 *             online, or the location has no world
	 * @throws IllegalStateException
	 *             off the main thread, or if the join failed
	 */
	public Bot spawn(String name, Location location) {
		requireMainThread();
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(location, "location");
		if (!NAME.matcher(name).matches()) {
			throw new IllegalArgumentException("Invalid bot name " + name + ": expected 1-16 of [A-Za-z0-9_]");
		}
		if (location.getWorld() == null) {
			throw new IllegalArgumentException("Cannot spawn bot " + name + ": location has no world");
		}
		if (Bukkit.getPlayerExact(name) != null || Bukkit.getPlayer(BotBody.offlineUuid(name)) != null
				|| bots.stream().anyMatch(b -> b.name().equalsIgnoreCase(name))) {
			throw new IllegalArgumentException(
					"Cannot spawn bot " + name + ": a player or bot with that name is already online");
		}
		BotBody body;
		try {
			body = BotBody.join(name, location.clone());
		} catch (Exception e) {
			throw new IllegalStateException("Bot " + name + " failed to join", e);
		}
		bots.add(body);
		logger.info(name + " joined at " + location.getWorld().getName() + " " + location.getX() + " " + location.getY()
				+ " " + location.getZ());
		return body;
	}

	/**
	 * Removes a bot through the server's quit path (fires {@code PlayerQuitEvent}),
	 * then drops it from the roster. Removing an unknown or already removed bot
	 * does nothing.
	 *
	 * @throws IllegalStateException
	 *             off the main thread
	 */
	public void remove(Bot bot) {
		requireMainThread();
		if (!(bot instanceof BotBody body) || !bots.contains(body)) {
			return;
		}
		removeBody(body);
	}

	/** @return the roster's bots, in spawn order (a snapshot). */
	public List<Bot> list() {
		return List.copyOf(bots);
	}

	@EventHandler
	public void onTick(TickElapsedEvent event) {
		for (var body : List.copyOf(bots)) {
			if (!bots.contains(body)) {
				continue;
			}
			body.tick();
			if (!body.isConnected()) {
				logger.info(body.name() + " lost its connection, removing it");
				removeBody(body);
			}
		}
	}

	@Override
	public void unload() {
		EventUtil.unregister(this);
		for (var body : List.copyOf(bots)) {
			try {
				removeBody(body);
			} catch (RuntimeException e) {
				logger.error(body.name() + " could not be removed while unloading", e);
			}
		}
	}

	private void removeBody(BotBody body) {
		try {
			body.remove();
		} finally {
			bots.remove(body);
		}
		logger.info(body.name() + " left");
	}

	private static void requireMainThread() {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("BotRoster must be used from the main thread");
		}
	}
}
