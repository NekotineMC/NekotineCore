package fr.nekotine.botstesthost.it;

import static fr.nekotine.botstesthost.it.BotLifecycleIT.cleanUp;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.register;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.roster;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.spawnLocation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import fr.nekotine.botstesthost.it.BotLifecycleIT.LogCapture;
import fr.nekotine.botstesthost.it.BotLifecycleIT.PlayerEvents;
import fr.nekotine.core.bot.Bot;
import fr.nekotine.core.bot.BotRoster;
import fr.nekotine.core.bot.BotState;
import fr.nekotine.core.bot.BotTickEvent;
import fr.nekotine.core.bot.BotWipe;
import fr.nekotine.core.bot.internal.BotBody;
import fr.xxgoldenbluexx.paperjunitserver.PaperServer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;

/**
 * Story 1.4: bots tick in a per-bot event, roster changes during the tick loop
 * are deferred, removed bots leave no saved files, and a library failure only
 * fails its bot.
 */
class BotTickIT {

	private static final String A = "Bot_A";

	private static final String B = "Bot_B";

	private static final String C = "Bot_C";

	private static final String FAILED_LINE = " failed, it stops ticking";

	/** One {@link BotTickEvent} as seen by {@link TickEvents}. */
	private record Seen(String name, int tick, boolean onMain, Bot bot) {
	}

	/**
	 * Records every {@link BotTickEvent} (and appends {@code event:<name>} to a
	 * shared sequence), then runs an optional hook.
	 */
	public static final class TickEvents implements Listener {
		final Queue<Seen> seen = new ConcurrentLinkedQueue<>();
		final Queue<String> sequence;
		volatile Consumer<BotTickEvent> hook = e -> {
		};

		TickEvents(Queue<String> sequence) {
			this.sequence = sequence;
		}

		TickEvents() {
			this(new ConcurrentLinkedQueue<>());
		}

		@EventHandler
		public void onBotTick(BotTickEvent event) {
			var name = event.getBot().name();
			seen.add(new Seen(name, Bukkit.getCurrentTick(), Bukkit.isPrimaryThread(), event.getBot()));
			sequence.add("event:" + name);
			hook.accept(event);
		}

		List<Seen> of(String name) {
			return seen.stream().filter(s -> s.name().equals(name)).toList();
		}
	}

	/** Appends {@code ack:<player name>} for every received teleport ack. */
	private static final class TeleportAcks extends PacketAdapter {
		final Queue<String> sequence;

		TeleportAcks(Queue<String> sequence) {
			super(PaperServer.plugin(), PacketType.Play.Client.TELEPORT_ACCEPT);
			this.sequence = sequence;
		}

		@Override
		public void onPacketReceiving(PacketEvent event) {
			sequence.add("ack:" + event.getPlayer().getName());
		}
	}

	/** @return the bot's data, stats and advancements files. */
	static List<Path> savedFiles(Bot bot) throws Exception {
		return PaperServer.onMain(() -> {
			var server = MinecraftServer.getServer();
			var uuid = bot.player().getUniqueId().toString();
			return List.of(server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(uuid + ".dat"),
					server.getWorldPath(LevelResource.PLAYER_STATS_DIR).resolve(uuid + ".json"),
					server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(uuid + ".json"));
		});
	}

	private static void deleteAll(List<Path> files) throws Exception {
		for (var file : files) {
			Files.deleteIfExists(file);
			Files.deleteIfExists(file.resolveSibling(file.getFileName() + "_old"));
		}
	}

	/**
	 * Every line of the library's logger must start with the name of the bot it is
	 * about (NFR9).
	 */
	private static void assertLibraryLinesNamed(LogCapture logs, String... names) {
		var library = logs.byLogger.stream().filter(e -> e.getKey().startsWith("NekotineCore")).map(Map.Entry::getValue)
				.toList();
		assertFalse(library.isEmpty(), "no library log line captured");
		var unnamed = library.stream().filter(l -> List.of(names).stream().noneMatch(l::startsWith)).toList();
		assertEquals(List.of(), unnamed, "library log lines not starting with a bot name");
	}

	@Test
	void tickEventsFireInSpawnOrderAfterAcks() throws Exception {
		var roster = roster();
		var sequence = new ConcurrentLinkedQueue<String>();
		var events = new TickEvents(sequence);
		var acks = new TeleportAcks(sequence);
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var a = PaperServer.onMain(() -> roster.spawn(A, location));
			var b = PaperServer.onMain(() -> roster.spawn(B, location));
			PaperServer.awaitTicks(5);
			register(events);
			register(acks);
			PaperServer.awaitTicks(3);
			PaperServer.onMain(() -> {
				sequence.add("teleport");
				a.player().teleport(location.clone().add(1, 0, 0));
				return null;
			});
			PaperServer.awaitTicks(5);
			var seen = List.copyOf(events.seen);
			assertTrue(seen.size() >= 10, "too few tick events: " + seen);
			assertTrue(seen.stream().allMatch(Seen::onMain), "a tick event fired off the main thread");
			assertTrue(seen.stream().allMatch(s -> s.bot() == (s.name().equals(A) ? a : b)),
					"getBot() is not the roster's bot");
			for (int i = 0; i + 1 < seen.size(); i += 2) {
				assertEquals(A, seen.get(i).name(), "events not in spawn order: " + seen);
				assertEquals(B, seen.get(i + 1).name(), "events not in spawn order: " + seen);
				assertEquals(seen.get(i).tick(), seen.get(i + 1).tick(), "A and B did not tick together: " + seen);
			}
			var after = new ArrayList<>(sequence);
			after = new ArrayList<>(after.subList(after.indexOf("teleport") + 1, after.size()));
			int ack = after.indexOf("ack:" + A);
			int event = after.indexOf("event:" + A);
			assertTrue(ack >= 0, "the teleport was not acked: " + after);
			assertTrue(ack < event, "A's tick event fired before its teleport ack: " + after);
			assertLibraryLinesNamed(logs, A, B);
		} finally {
			cleanUp(roster, events, acks);
		}
	}

	@Test
	void removeDuringLoopLeavesAfterIt() throws Exception {
		var roster = roster();
		var events = new TickEvents();
		var players = new PlayerEvents();
		register(players);
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var a = PaperServer.onMain(() -> roster.spawn(A, location));
			var b = PaperServer.onMain(() -> roster.spawn(B, location));
			PaperServer.awaitTicks(3);
			var removedAt = new AtomicInteger(-1);
			var duringLoop = new ConcurrentLinkedQueue<Boolean>();
			events.hook = e -> {
				if (e.getBot() == a && removedAt.get() < 0) {
					removedAt.set(Bukkit.getCurrentTick());
					roster.remove(b);
					duringLoop.add(roster.list().contains(b));
					duringLoop.add(b.player().isOnline());
					duringLoop.add(players.quit.isEmpty());
				}
			};
			register(events);
			PaperServer.awaitTicks(5);
			assertTrue(removedAt.get() >= 0, "A's listener never ran");
			assertEquals(List.of(true, true, true), List.copyOf(duringLoop),
					"B was not still listed, online and not yet quit right after its removal");
			assertTrue(events.of(B).stream().noneMatch(s -> s.tick() >= removedAt.get()),
					"B got a tick event after its removal: " + events.of(B));
			var player = b.player();
			assertEquals(List.of(player), List.copyOf(players.quit), "PlayerQuitEvent not fired once for B");
			PaperServer.onMain(() -> {
				assertEquals(List.of(a), roster.list(), "B is still listed");
				assertFalse(player.isOnline(), "B is still online");
				return null;
			});
			assertLibraryLinesNamed(logs, A, B);
		} finally {
			cleanUp(roster, events, players);
		}
	}

	@Test
	void spawnDuringLoopJoinsAtOnceAndTicksAfterIt() throws Exception {
		var roster = roster();
		var events = new TickEvents();
		var players = new PlayerEvents();
		register(players);
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var a = PaperServer.onMain(() -> roster.spawn(A, location));
			PaperServer.awaitTicks(3);
			var spawnedAt = new AtomicInteger(-1);
			var spawned = new ConcurrentLinkedQueue<Bot>();
			var duringLoop = new ConcurrentLinkedQueue<Boolean>();
			events.hook = e -> {
				if (e.getBot() == a && spawnedAt.get() < 0) {
					spawnedAt.set(Bukkit.getCurrentTick());
					var c = roster.spawn(C, location);
					spawned.add(c);
					duringLoop.add(c.player().isOnline());
					duringLoop.add(players.joined.contains(c.player()));
					duringLoop.add(!roster.list().contains(c));
					boolean refused;
					try {
						roster.spawn(C, location);
						refused = false;
					} catch (IllegalArgumentException duplicate) {
						refused = true;
					}
					duringLoop.add(refused);
				}
			};
			register(events);
			PaperServer.awaitTicks(5);
			var c = spawned.peek();
			assertNotNull(c, "A's listener did not spawn C");
			assertEquals(List.of(true, true, true, true), List.copyOf(duringLoop),
					"C was not online, joined, unlisted and its duplicate refused right after its spawn");
			assertEquals(1, players.joined.stream().filter(p -> p.getName().equals(C)).count(),
					"C did not join exactly once");
			var cTicks = events.of(C);
			assertFalse(cTicks.isEmpty(), "C never ticked");
			assertTrue(cTicks.stream().allMatch(s -> s.tick() > spawnedAt.get()),
					"C ticked in the loop it was spawned in: " + cTicks);
			assertEquals(List.of(a, c), PaperServer.onMain(roster::list), "C is not listed after A");
			assertLibraryLinesNamed(logs, A, C);
		} finally {
			cleanUp(roster, events, players);
		}
	}

	@Test
	void defaultWipeDeletesSavedFiles() throws Exception {
		var roster = roster();
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var a = PaperServer.onMain(() -> roster.spawn(A, location));
			var files = savedFiles(a);
			var dat = files.getFirst();
			var datOld = dat.resolveSibling(dat.getFileName() + "_old");
			PaperServer.awaitTicks(3);
			PaperServer.onMain(() -> {
				// Two saves: the second backs the first up as .dat_old.
				a.player().saveData();
				a.player().saveData();
				return null;
			});
			assertTrue(Files.exists(dat), "no .dat saved before removal");
			assertTrue(Files.exists(datOld), "no .dat_old saved before removal");
			PaperServer.onMain(() -> {
				roster.remove(a);
				return null;
			});
			assertFalse(Files.exists(datOld), ".dat_old left after removal");
			var left = new ArrayList<Path>();
			for (var file : files) {
				if (Files.exists(file) || Files.exists(file.resolveSibling(file.getFileName() + "_old"))) {
					left.add(file);
				}
			}
			assertEquals(List.of(), left, "saved files left after removal");
			assertTrue(logs.lines.stream().noneMatch(l -> l.contains("could not delete")),
					"a missing file was reported");
			assertLibraryLinesNamed(logs, A);
		} finally {
			cleanUp(roster);
		}
	}

	@Test
	void removeOfBotSpawnedInSameLoopLeavesAfterIt() throws Exception {
		var roster = roster();
		var events = new TickEvents();
		var players = new PlayerEvents();
		register(players);
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var a = PaperServer.onMain(() -> roster.spawn(A, location));
			PaperServer.awaitTicks(3);
			var spawned = new ConcurrentLinkedQueue<Bot>();
			var duringLoop = new ConcurrentLinkedQueue<Boolean>();
			events.hook = e -> {
				if (e.getBot() == a && spawned.isEmpty()) {
					var c = roster.spawn(C, location);
					spawned.add(c);
					roster.remove(c);
					duringLoop.add(c.player().isOnline());
					duringLoop.add(!roster.list().contains(c));
					duringLoop.add(players.quit.isEmpty());
				}
			};
			register(events);
			PaperServer.awaitTicks(5);
			var c = spawned.peek();
			assertNotNull(c, "A's listener did not spawn C");
			assertEquals(List.of(true, true, true), List.copyOf(duringLoop),
					"C was not still online, unlisted and not yet quit right after its removal");
			var player = c.player();
			assertEquals(List.of(player), List.copyOf(players.quit), "PlayerQuitEvent not fired once for C");
			assertEquals(List.of(), events.of(C), "C got a tick event");
			PaperServer.onMain(() -> {
				assertEquals(List.of(a), roster.list(), "C is listed");
				assertFalse(player.isOnline(), "C is still online");
				return null;
			});
			for (var file : savedFiles(c)) {
				assertFalse(Files.exists(file), "saved file left after removal: " + file);
			}
			assertLibraryLinesNamed(logs, A, C);
		} finally {
			cleanUp(roster, events, players);
		}
	}

	@Test
	void kickedFailedBotLeaves() throws Exception {
		var roster = roster();
		var players = new PlayerEvents();
		register(players);
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var a = PaperServer.onMain(() -> roster.spawn(A, location));
			var files = savedFiles(a);
			PaperServer.awaitTicks(3);
			PaperServer.onMain(() -> {
				assertInstanceOf(BotBody.class, a).failNextTick(new IllegalStateException("BotTickIT kick fault"));
				return null;
			});
			PaperServer.awaitTicks(3);
			assertEquals(BotState.FAILED, PaperServer.onMain(a::state));
			var player = a.player();
			PaperServer.onMain(() -> {
				player.kick(Component.text("kicked by BotTickIT"));
				return null;
			});
			PaperServer.awaitTicks(5);
			PaperServer.onMain(() -> {
				assertEquals(List.of(), roster.list(), "kicked failed bot is still listed");
				assertFalse(player.isOnline(), "kicked failed bot is still online");
				return null;
			});
			assertEquals(List.of(player), List.copyOf(players.quit), "PlayerQuitEvent not fired once for the bot");
			for (var file : files) {
				assertFalse(Files.exists(file), "saved file left after the kick: " + file);
			}
			assertLibraryLinesNamed(logs, A);
		} finally {
			cleanUp(roster, players);
		}
	}

	@Test
	void keepFlagsKeepSavedFiles() throws Exception {
		var roster = roster();
		List<Path> keptFiles = List.of();
		List<Path> stateOnlyFiles = List.of();
		try {
			var location = spawnLocation();
			var kept = PaperServer.onMain(() -> roster.spawn(A, location, new BotWipe(false, false)));
			var stateOnly = PaperServer.onMain(() -> roster.spawn(B, location, new BotWipe(true, false)));
			keptFiles = savedFiles(kept);
			stateOnlyFiles = savedFiles(stateOnly);
			PaperServer.awaitTicks(3);
			PaperServer.onMain(() -> {
				roster.remove(kept);
				roster.remove(stateOnly);
				return null;
			});
			for (var file : keptFiles) {
				assertTrue(Files.exists(file), "kept file was deleted: " + file);
			}
			assertFalse(Files.exists(stateOnlyFiles.get(0)), "state wipe kept the player data");
			assertTrue(Files.exists(stateOnlyFiles.get(1)), "state-only wipe deleted the stats");
			assertTrue(Files.exists(stateOnlyFiles.get(2)), "state-only wipe deleted the advancements");
		} finally {
			cleanUp(roster);
			deleteAll(keptFiles);
			deleteAll(stateOnlyFiles);
		}
	}

	@Test
	void libraryFailureFailsOnlyThatBot() throws Exception {
		var roster = roster();
		var events = new TickEvents();
		var players = new PlayerEvents();
		register(players);
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var a = PaperServer.onMain(() -> roster.spawn(A, location));
			var b = PaperServer.onMain(() -> roster.spawn(B, location));
			register(events);
			PaperServer.awaitTicks(3);
			var fault = "BotTickIT injected fault";
			PaperServer.onMain(() -> {
				assertInstanceOf(BotBody.class, a).failNextTick(new IllegalStateException(fault));
				return null;
			});
			PaperServer.awaitTicks(3);
			int aEvents = events.of(A).size();
			int bEvents = events.of(B).size();
			assertEquals(BotState.FAILED, PaperServer.onMain(a::state));
			assertEquals(BotState.ACTIVE, PaperServer.onMain(b::state));
			PaperServer.awaitTicks(700);
			PaperServer.onMain(() -> {
				assertTrue(a.player().isOnline(), "failed bot was disconnected");
				assertEquals(List.of(a, b), roster.list(), "failed bot is no longer listed");
				assertEquals(BotState.FAILED, a.state());
				return null;
			});
			assertEquals(aEvents, events.of(A).size(), "failed bot kept getting tick events");
			assertTrue(events.of(B).size() >= bEvents + 600, "B stopped ticking");
			assertTrue(players.quit.isEmpty() && players.kicked.isEmpty(), "a bot quit or was kicked");
			var faultLines = logs.lines.stream().filter(l -> l.contains(fault)).toList();
			assertEquals(1, faultLines.size(), "fault not logged exactly once: " + faultLines);
			assertTrue(faultLines.getFirst().startsWith(A + FAILED_LINE), "unexpected line: " + faultLines);
			var player = a.player();
			PaperServer.onMain(() -> {
				roster.remove(a);
				assertEquals(List.of(player), List.copyOf(players.quit), "removing the failed bot did not quit it");
				assertFalse(player.isOnline(), "removed failed bot is still online");
				assertEquals(List.of(b), roster.list());
				return null;
			});
			assertLibraryLinesNamed(logs, A, B);
		} finally {
			cleanUp(roster, events, players);
		}
	}

	@Test
	void hostListenerFailureDoesNotFailBot() throws Exception {
		var roster = roster();
		var events = new TickEvents();
		var fault = "BotTickIT host listener fault";
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var a = PaperServer.onMain(() -> roster.spawn(A, location));
			events.hook = e -> {
				throw new IllegalStateException(fault);
			};
			register(events);
			PaperServer.awaitTicks(5);
			assertEquals(BotState.ACTIVE, PaperServer.onMain(a::state), "a host listener failure failed the bot");
			assertTrue(events.of(A).size() >= 4, "the bot stopped ticking");
			assertTrue(logs.lines.stream().noneMatch(l -> l.contains(FAILED_LINE)), "library failure logged");
			assertTrue(logs.lines.stream().anyMatch(l -> l.contains(fault)), "Paper did not log the listener error");
			assertLibraryLinesNamed(logs, A);
		} finally {
			cleanUp(roster, events);
		}
	}

	@Test
	void unloadRemovesEveryBot() throws Exception {
		var separate = PaperServer.onMain(BotRoster::new);
		var players = new PlayerEvents();
		register(players);
		try (var logs = new LogCapture()) {
			var location = spawnLocation();
			var a = PaperServer.onMain(() -> separate.spawn(A, location));
			var b = PaperServer.onMain(() -> separate.spawn(B, location));
			var files = new ArrayList<Path>(savedFiles(a));
			files.addAll(savedFiles(b));
			PaperServer.awaitTicks(3);
			// unload() also covers a failed bot.
			PaperServer.onMain(() -> {
				assertInstanceOf(BotBody.class, a).failNextTick(new IllegalStateException("BotTickIT unload fault"));
				return null;
			});
			PaperServer.awaitTicks(3);
			assertEquals(BotState.FAILED, PaperServer.onMain(a::state));
			var aPlayer = a.player();
			var bPlayer = b.player();
			PaperServer.onMain(() -> {
				separate.unload();
				assertEquals(List.of(), separate.list(), "unloaded roster still lists bots");
				assertFalse(aPlayer.isOnline(), "A is still online");
				assertFalse(bPlayer.isOnline(), "B is still online");
				return null;
			});
			assertEquals(List.of(aPlayer, bPlayer), List.copyOf(players.quit), "PlayerQuitEvent not fired once each");
			for (var file : files) {
				assertFalse(Files.exists(file), "saved file left after unload: " + file);
			}
			assertLibraryLinesNamed(logs, A, B);
		} finally {
			PaperServer.onMain(() -> {
				separate.unload();
				return null;
			});
			cleanUp(roster(), players);
		}
	}
}
