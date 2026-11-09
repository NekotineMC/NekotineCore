package fr.nekotine.botstesthost.it;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import fr.nekotine.core.bot.Bot;
import fr.nekotine.core.bot.BotRoster;
import fr.nekotine.core.bot.BotState;
import fr.nekotine.core.bot.internal.BotBody;
import fr.nekotine.core.ioc.Ioc;
import fr.xxgoldenbluexx.paperjunitserver.PaperServer;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipFile;
import net.kyori.adventure.text.Component;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.Test;

/**
 * Story 1.3: a bot joins and leaves as a real player, answers the client acks
 * and is seen by ProtocolLib.
 */
class BotLifecycleIT {

	private static final String ALICE = "Bot_Alice";

	private static BotRoster roster() throws Exception {
		return PaperServer.onMain(() -> Ioc.resolve(BotRoster.class));
	}

	private static Location spawnLocation() throws Exception {
		return PaperServer.onMain(() -> Bukkit.getWorlds().getFirst().getSpawnLocation().add(0.5, 0, 0.5));
	}

	private static void cleanUp(BotRoster roster, Object... listeners) throws Exception {
		PaperServer.onMain(() -> {
			for (var bot : roster.list()) {
				roster.remove(bot);
			}
			var manager = ProtocolLibrary.getProtocolManager();
			for (var listener : listeners) {
				if (listener instanceof PacketAdapter adapter) {
					manager.removePacketListener(adapter);
				} else if (listener instanceof Listener bukkit) {
					HandlerList.unregisterAll(bukkit);
				}
			}
			return null;
		});
	}

	private static void register(Listener listener) throws Exception {
		PaperServer.onMain(() -> {
			Bukkit.getPluginManager().registerEvents(listener, PaperServer.plugin());
			return null;
		});
	}

	private static void register(PacketAdapter adapter) throws Exception {
		PaperServer.onMain(() -> {
			ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
			return null;
		});
	}

	private static ServerGamePacketListenerImpl listenerOf(Bot bot) {
		return ((CraftPlayer) bot.player()).getHandle().connection;
	}

	/** Records the players of the Bukkit events it listens to. */
	public static final class PlayerEvents implements Listener {
		final Queue<Player> joined = new ConcurrentLinkedQueue<>();
		final Queue<Player> quit = new ConcurrentLinkedQueue<>();
		final Queue<Player> kicked = new ConcurrentLinkedQueue<>();

		@EventHandler
		public void onJoin(PlayerJoinEvent event) {
			joined.add(event.getPlayer());
		}

		@EventHandler
		public void onQuit(PlayerQuitEvent event) {
			quit.add(event.getPlayer());
		}

		@EventHandler
		public void onKick(PlayerKickEvent event) {
			kicked.add(event.getPlayer());
		}
	}

	/**
	 * Records received packets of the given type: the player's UUID and the first
	 * int field.
	 */
	private static final class Receiving extends PacketAdapter {
		final Queue<UUID> players = new ConcurrentLinkedQueue<>();
		final Queue<Integer> ints = new ConcurrentLinkedQueue<>();
		final Queue<Boolean> onMain = new ConcurrentLinkedQueue<>();

		Receiving(PacketType type) {
			super(PaperServer.plugin(), type);
		}

		@Override
		public void onPacketReceiving(PacketEvent event) {
			players.add(event.getPlayer().getUniqueId());
			ints.add(event.getPacket().getIntegers().readSafely(0));
			onMain.add(Bukkit.isPrimaryThread());
		}
	}

	/**
	 * Captures log lines (message plus thrown class names) of the server's log4j
	 * context.
	 */
	private static final class LogCapture extends AbstractAppender implements AutoCloseable {
		final Queue<String> lines = new ConcurrentLinkedQueue<>();
		private final LoggerContext context;

		LogCapture() {
			super("BotLifecycleIT-capture", null, null, true, Property.EMPTY_ARRAY);
			context = (LoggerContext) LogManager.getContext(Bukkit.class.getClassLoader(), false);
			start();
			context.getConfiguration().getRootLogger().addAppender(this, null, null);
			context.updateLoggers();
		}

		@Override
		public void append(LogEvent event) {
			var line = new StringBuilder(String.valueOf(event.getMessage().getFormattedMessage()));
			for (var thrown = event.getThrown(); thrown != null; thrown = thrown.getCause()) {
				line.append(' ').append(thrown.getClass().getName()).append(": ").append(thrown.getMessage());
			}
			lines.add(line.toString());
		}

		@Override
		public void close() {
			context.getConfiguration().getRootLogger().removeAppender(getName());
			context.updateLoggers();
			stop();
		}
	}

	@Test
	void spawnJoinsAsRealPlayer() throws Exception {
		var roster = roster();
		var events = new PlayerEvents();
		register(events);
		try {
			var location = spawnLocation();
			var bot = PaperServer.onMain(() -> roster.spawn(ALICE, location));
			PaperServer.onMain(() -> {
				var player = bot.player();
				assertEquals(UUIDUtil.createOfflinePlayerUUID(ALICE), player.getUniqueId());
				assertEquals(List.of(player), List.copyOf(events.joined), "PlayerJoinEvent not fired once for the bot");
				assertTrue(player.isOnline(), "bot is not online");
				assertTrue(Bukkit.getOnlinePlayers().contains(player), "getOnlinePlayers() lacks the bot");
				assertSame(player, Bukkit.getPlayer(player.getUniqueId()), "Bukkit.getPlayer(uuid) is not the bot");
				var at = player.getLocation();
				assertSame(location.getWorld(), at.getWorld());
				assertEquals(location.getX(), at.getX(), 1e-6);
				assertEquals(location.getY(), at.getY(), 1e-6);
				assertEquals(location.getZ(), at.getZ(), 1e-6);
				var listed = roster.list();
				assertEquals(1, listed.size());
				assertEquals(ALICE, listed.getFirst().name());
				assertSame(player, listed.getFirst().player());
				assertEquals(BotState.ACTIVE, listed.getFirst().state());
				return null;
			});
		} finally {
			cleanUp(roster, events);
		}
	}

	@Test
	void duplicateNameIsRefused() throws Exception {
		var roster = roster();
		var events = new PlayerEvents();
		register(events);
		try {
			var location = spawnLocation();
			PaperServer.onMain(() -> roster.spawn(ALICE, location));
			var error = PaperServer
					.onMain(() -> assertThrows(IllegalArgumentException.class, () -> roster.spawn(ALICE, location)));
			assertTrue(error.getMessage().contains(ALICE), "message does not name the bot: " + error.getMessage());
			assertEquals(1, events.joined.size(), "a second join happened");
			assertEquals(1, PaperServer.onMain(() -> roster.list().size()));
		} finally {
			cleanUp(roster, events);
		}
	}

	@Test
	void offThreadCallsAreRefused() throws Exception {
		var roster = roster();
		try {
			var location = spawnLocation();
			assertFalse(Bukkit.isPrimaryThread());
			assertThrows(IllegalStateException.class, () -> roster.spawn(ALICE, location));
			assertNull(PaperServer.onMain(() -> Bukkit.getPlayerExact(ALICE)), "an off-thread spawn joined");
			var bot = PaperServer.onMain(() -> roster.spawn(ALICE, location));
			assertThrows(IllegalStateException.class, () -> roster.remove(bot));
			assertThrows(IllegalStateException.class, roster::list);
			PaperServer.onMain(() -> {
				assertTrue(bot.player().isOnline(), "an off-thread remove changed the bot");
				assertEquals(List.of(bot), roster.list());
				return null;
			});
		} finally {
			cleanUp(roster);
		}
	}

	@Test
	void teleportsAreAckedOnceWithLatestId() throws Exception {
		var roster = roster();
		var acks = new Receiving(PacketType.Play.Client.TELEPORT_ACCEPT);
		try {
			var location = spawnLocation();
			var bot = PaperServer.onMain(() -> roster.spawn(ALICE, location));
			PaperServer.awaitTicks(5);
			register(acks);
			var awaiting = ServerGamePacketListenerImpl.class.getDeclaredField("awaitingTeleport");
			awaiting.setAccessible(true);
			int[] ids = PaperServer.onMain(() -> {
				var listener = listenerOf(bot);
				int before = awaiting.getInt(listener);
				bot.player().teleport(location.clone().add(1, 0, 0));
				bot.player().teleport(location.clone().add(2, 0, 0));
				return new int[]{before, awaiting.getInt(listener)};
			});
			assertEquals(ids[0] + 2, ids[1], "the two teleports did not each send a position packet");
			PaperServer.awaitTicks(5);
			assertEquals(List.of(ids[1]), List.copyOf(acks.ints), "expected one ack, for the latest teleport id");
			assertEquals(List.of(bot.player().getUniqueId()), List.copyOf(acks.players));
		} finally {
			cleanUp(roster, acks);
		}
	}

	@Test
	void protocolLibSeesInboundPackets() throws Exception {
		var roster = roster();
		var heldItem = new Receiving(PacketType.Play.Client.HELD_ITEM_SLOT);
		try {
			var location = spawnLocation();
			var bot = PaperServer.onMain(() -> roster.spawn(ALICE, location));
			PaperServer.awaitTicks(2);
			register(heldItem);
			PaperServer.onMain(() -> {
				assertInstanceOf(BotBody.class, bot).writeInbound(new ServerboundSetCarriedItemPacket(3));
				return null;
			});
			PaperServer.awaitTicks(2);
			assertEquals(List.of(bot.player().getUniqueId()), List.copyOf(heldItem.players),
					"the HELD_ITEM_SLOT adapter did not fire once for the bot");
			assertEquals(List.of(3), List.copyOf(heldItem.ints));
			assertEquals(3, PaperServer.onMain(() -> bot.player().getInventory().getHeldItemSlot()),
					"the server handler did not run");
			assertEquals(List.of(true), List.copyOf(heldItem.onMain),
					"the bot's packet was not handled on the main thread");
		} finally {
			cleanUp(roster, heldItem);
		}
	}

	@Test
	void idleBotIsLoadedAndNotKicked() throws Exception {
		var roster = roster();
		var events = new PlayerEvents();
		register(events);
		try {
			var location = spawnLocation();
			var bot = PaperServer.onMain(() -> roster.spawn(ALICE, location));
			PaperServer.awaitTicks(5);
			assertTrue(PaperServer.onMain(() -> listenerOf(bot).hasClientLoaded()),
					"hasClientLoaded() false 5 ticks after spawn: no player-loaded ack");
			PaperServer.awaitTicks(700);
			PaperServer.onMain(() -> {
				assertTrue(bot.player().isOnline(), "idle bot was disconnected");
				assertEquals(List.of(bot), roster.list(), "idle bot is no longer listed");
				return null;
			});
			assertTrue(events.kicked.isEmpty(), "idle bot was kicked");
			assertTrue(events.quit.isEmpty(), "idle bot quit");
		} finally {
			cleanUp(roster, events);
		}
	}

	@Test
	void removeLeavesThroughQuitPath() throws Exception {
		var roster = roster();
		var events = new PlayerEvents();
		register(events);
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var bot = PaperServer.onMain(() -> roster.spawn(ALICE, location));
			var other = PaperServer.onMain(() -> roster.spawn("Bot_Bob", location));
			PaperServer.awaitTicks(5);
			var uuid = bot.player().getUniqueId();
			PaperServer.onMain(() -> {
				var player = bot.player();
				roster.remove(bot);
				assertEquals(List.of(player), List.copyOf(events.quit), "PlayerQuitEvent not fired once for the bot");
				assertFalse(player.isOnline(), "removed bot is still online");
				assertFalse(Bukkit.getOnlinePlayers().contains(player), "getOnlinePlayers() still has the bot");
				assertNull(Bukkit.getPlayer(uuid), "Bukkit.getPlayer(uuid) still finds the bot");
				assertEquals(List.of(other), roster.list(), "removed bot is still listed");
				// Removing a removed bot is a no-op.
				assertDoesNotThrow(() -> roster.remove(bot));
				assertEquals(1, events.quit.size());
				return null;
			});
			PaperServer.awaitTicks(5);
			var probe = "BotLifecycleIT log probe " + UUID.randomUUID();
			LogManager.getContext(Bukkit.class.getClassLoader(), false).getLogger("BotLifecycleIT").info(probe);
			PaperServer.awaitTicks(5);
			assertTrue(logs.lines.stream().anyMatch(l -> l.contains(probe)), "log capture is not attached");
			var closed = logs.lines.stream().filter(l -> l.contains("ClosedChannelException")).toList();
			assertTrue(closed.isEmpty(), "closed-channel errors logged on removal: " + closed);
		} finally {
			cleanUp(roster, events);
		}
	}

	@Test
	void kickedBotIsDroppedFromRoster() throws Exception {
		var roster = roster();
		var events = new PlayerEvents();
		register(events);
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var bot = PaperServer.onMain(() -> roster.spawn(ALICE, location));
			PaperServer.awaitTicks(5);
			var player = bot.player();
			PaperServer.onMain(() -> {
				player.kick(Component.text("kicked by BotLifecycleIT"));
				return null;
			});
			PaperServer.awaitTicks(5);
			PaperServer.onMain(() -> {
				assertEquals(List.of(), roster.list(), "kicked bot is still listed");
				assertFalse(player.isOnline(), "kicked bot is still online");
				return null;
			});
			assertEquals(List.of(player), List.copyOf(events.quit), "PlayerQuitEvent not fired once for the bot");
			var probe = "BotLifecycleIT log probe " + UUID.randomUUID();
			LogManager.getContext(Bukkit.class.getClassLoader(), false).getLogger("BotLifecycleIT").info(probe);
			PaperServer.awaitTicks(5);
			assertTrue(logs.lines.stream().anyMatch(l -> l.contains(probe)), "log capture is not attached");
			var closed = logs.lines.stream().filter(l -> l.contains("ClosedChannelException")).toList();
			assertTrue(closed.isEmpty(), "closed-channel errors logged on kick: " + closed);
		} finally {
			cleanUp(roster, events);
		}
	}

	@Test
	void invalidSpawnArgumentsAreRefused() throws Exception {
		var roster = roster();
		var events = new PlayerEvents();
		register(events);
		try {
			var location = spawnLocation();
			var noWorld = new Location(null, location.getX(), location.getY(), location.getZ());
			PaperServer.onMain(() -> {
				assertThrows(IllegalArgumentException.class, () -> roster.spawn("Bad-Name", location));
				assertThrows(IllegalArgumentException.class, () -> roster.spawn("Bot_SeventeenChar", location));
				assertThrows(IllegalArgumentException.class, () -> roster.spawn(ALICE, noWorld));
				assertEquals(List.of(), roster.list(), "a refused spawn was listed");
				return null;
			});
			assertTrue(events.joined.isEmpty(), "a refused spawn joined");
		} finally {
			cleanUp(roster, events);
		}
	}

	@Test
	void publicApiExposesNoNms() throws Exception {
		var source = BotRoster.class.getProtectionDomain().getCodeSource();
		if (source == null) {
			fail("BotRoster has no code source to scan");
		}
		var names = new ArrayList<String>();
		try (var jar = new ZipFile(new File(source.getLocation().toURI()))) {
			jar.stream().map(e -> e.getName())
					.filter(n -> n.startsWith("fr/nekotine/core/bot/") && n.endsWith(".class")
							&& n.indexOf('/', "fr/nekotine/core/bot/".length()) < 0)
					.forEach(n -> names.add(n.substring(0, n.length() - ".class".length()).replace('/', '.')));
		}
		assertTrue(names.contains(BotRoster.class.getName()), "scan found no public bot types: " + names);
		var leaks = new ArrayList<String>();
		for (var name : names) {
			var type = Class.forName(name, false, BotRoster.class.getClassLoader());
			if (!Modifier.isPublic(type.getModifiers())) {
				continue;
			}
			var signatures = new ArrayList<String>();
			signatures.add(String.valueOf(type.getGenericSuperclass()));
			for (var itf : type.getGenericInterfaces()) {
				signatures.add(itf.getTypeName());
			}
			for (Method method : type.getDeclaredMethods()) {
				if (exposed(method.getModifiers())) {
					signatures.add(method.toGenericString());
				}
			}
			for (Constructor<?> constructor : type.getDeclaredConstructors()) {
				if (exposed(constructor.getModifiers())) {
					signatures.add(constructor.toGenericString());
				}
			}
			for (Field field : type.getDeclaredFields()) {
				if (exposed(field.getModifiers())) {
					signatures.add(field.toGenericString());
				}
			}
			signatures.stream().filter(s -> s.contains("net.minecraft") || s.contains("com.mojang"))
					.forEach(s -> leaks.add(name + ": " + s));
		}
		assertEquals(List.of(), leaks, "public bot API mentions NMS types");
	}

	private static boolean exposed(int modifiers) {
		return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
	}
}
