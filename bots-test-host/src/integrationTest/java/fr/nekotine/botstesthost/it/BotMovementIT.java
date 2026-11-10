package fr.nekotine.botstesthost.it;

import static fr.nekotine.botstesthost.it.BotLifecycleIT.cleanUp;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.register;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.roster;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.spawnLocation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import fr.nekotine.botstesthost.it.BotLifecycleIT.LogCapture;
import fr.nekotine.botstesthost.it.BotLifecycleIT.PlayerEvents;
import fr.nekotine.core.bot.Bot;
import fr.nekotine.core.bot.BotRoster;
import fr.nekotine.core.bot.BotTickEvent;
import fr.nekotine.core.bot.internal.BotBody;
import fr.xxgoldenbluexx.paperjunitserver.PaperServer;
import io.papermc.paper.event.player.PlayerFailMoveEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.junit.jupiter.api.Test;

/**
 * Story 1.6: bots walk, look, jump, sneak and sprint through real packets, on a
 * stone platform built high above spawn.
 */
class BotMovementIT {

	private static final String NAME = "Bot_Mover";

	/** The platform's block layer: bots stand at {@code FLOOR_Y + 1}. */
	private static final int FLOOR_Y = 199;

	private static final int RADIUS = 10;

	private static final double EPSILON = 0.01;

	/** Stone blocks placed by a test, cleared afterwards. */
	private static final class Structure {
		final World world;
		final int cx;
		final int cz;
		final List<Block> placed = new ArrayList<>();

		Structure(Location spawn) {
			world = spawn.getWorld();
			cx = spawn.getBlockX();
			cz = spawn.getBlockZ();
		}

		/** Fills a box of stone, bounds inclusive, relative to the centre column. */
		Structure fill(int dx1, int y1, int dz1, int dx2, int y2, int dz2) {
			for (int x = dx1; x <= dx2; x++) {
				for (int y = y1; y <= y2; y++) {
					for (int z = dz1; z <= dz2; z++) {
						var block = world.getBlockAt(cx + x, y, cz + z);
						block.setType(Material.STONE, false);
						placed.add(block);
					}
				}
			}
			return this;
		}

		Structure platform() {
			return fill(-RADIUS, FLOOR_Y, -RADIUS, RADIUS, FLOOR_Y, RADIUS);
		}

		/** @return the centre of the block column, at height {@code y}, facing +Z. */
		Location at(double y) {
			return new Location(world, cx + 0.5, y, cz + 0.5, 0, 0);
		}

		void clear() {
			for (var block : placed) {
				block.setType(Material.AIR, false);
			}
		}
	}

	/** Bukkit events of one player; refuses its failed moves if asked. */
	public static final class MoveEvents implements Listener {
		final String name;
		final boolean refuse;
		final Queue<Location> moves = new ConcurrentLinkedQueue<>();
		final Queue<Boolean> sneaks = new ConcurrentLinkedQueue<>();
		final Queue<Boolean> sprints = new ConcurrentLinkedQueue<>();
		final Queue<PlayerFailMoveEvent.FailReason> fails = new ConcurrentLinkedQueue<>();
		final Queue<Location> jumps = new ConcurrentLinkedQueue<>();

		MoveEvents(String name, boolean refuse) {
			this.name = name;
			this.refuse = refuse;
		}

		MoveEvents(String name) {
			this(name, false);
		}

		@EventHandler
		public void onMove(PlayerMoveEvent event) {
			if (event.getPlayer().getName().equals(name)) {
				moves.add(event.getTo());
			}
		}

		@EventHandler
		public void onJump(PlayerJumpEvent event) {
			if (event.getPlayer().getName().equals(name)) {
				jumps.add(event.getTo());
			}
		}

		@EventHandler
		public void onSneak(PlayerToggleSneakEvent event) {
			if (event.getPlayer().getName().equals(name)) {
				sneaks.add(event.isSneaking());
			}
		}

		@EventHandler
		public void onSprint(PlayerToggleSprintEvent event) {
			if (event.getPlayer().getName().equals(name)) {
				sprints.add(event.isSprinting());
			}
		}

		@EventHandler(priority = EventPriority.LOW)
		public void onFailMove(PlayerFailMoveEvent event) {
			if (event.getPlayer().getName().equals(name)) {
				fails.add(event.getFailReason());
				if (refuse) {
					event.setAllowed(false);
					event.setLogWarning(false);
				}
			}
		}
	}

	/** One received packet of the watched player. */
	private record Seen(PacketType type, int tick) {
	}

	/** Records the input, player-command and move packets one player sends. */
	private static final class Packets extends PacketAdapter {
		final String name;
		final Queue<Seen> seen = new ConcurrentLinkedQueue<>();

		Packets(String name) {
			super(PaperServer.plugin(), PacketType.Play.Client.STEER_VEHICLE, PacketType.Play.Client.ENTITY_ACTION,
					PacketType.Play.Client.POSITION_LOOK);
			this.name = name;
		}

		@Override
		public void onPacketReceiving(PacketEvent event) {
			if (event.getPlayer().getName().equals(name)) {
				seen.add(new Seen(event.getPacketType(), Bukkit.getCurrentTick()));
			}
		}

		List<Seen> of(PacketType type) {
			return seen.stream().filter(s -> s.type() == type).toList();
		}
	}

	/** Counts the tick events of one bot, running an optional hook. */
	public static final class Ticks implements Listener {
		final Bot bot;
		final AtomicInteger count = new AtomicInteger();
		volatile Consumer<BotTickEvent> hook = e -> {
		};

		Ticks(Bot bot) {
			this.bot = bot;
		}

		@EventHandler
		public void onTick(BotTickEvent event) {
			if (event.getBot() == bot) {
				count.incrementAndGet();
				hook.accept(event);
			}
		}
	}

	private static Structure structure() throws Exception {
		return new Structure(spawnLocation());
	}

	private static void build(Structure structure) throws Exception {
		PaperServer.onMain(() -> {
			structure.platform();
			return null;
		});
	}

	private static void clear(Structure structure) throws Exception {
		PaperServer.onMain(() -> {
			structure.clear();
			return null;
		});
	}

	private static Bot spawn(BotRoster roster, Location location) throws Exception {
		return PaperServer.onMain(() -> roster.spawn(NAME, location));
	}

	private static Location location(Bot bot) throws Exception {
		return PaperServer.onMain(() -> bot.player().getLocation());
	}

	private static void control(Runnable change) throws Exception {
		PaperServer.onMain(() -> {
			change.run();
			return null;
		});
	}

	private static void assertNoServerMoveWarnings(LogCapture logs) {
		var warnings = logs.lines.stream().filter(l -> l.contains("moved wrongly") || l.contains("moved too quickly"))
				.toList();
		assertEquals(List.of(), warnings, "the server complained about the bot's moves");
	}

	private static void assertOnGroundAt(Bot bot, double y) throws Exception {
		PaperServer.onMain(() -> {
			var location = bot.player().getLocation();
			assertTrue(Math.abs(location.getY() - y) < EPSILON, "expected the bot at y " + y + ", got " + location);
			assertTrue(bot.player().isOnGround(), "bot is not on ground at " + location);
			return null;
		});
	}

	@Test
	void walkMovesAlongHeading() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		try (var logs = new LogCapture()) {
			build(structure);
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(10);
			register(events);
			var start = location(bot);
			control(() -> bot.setForward(true));
			PaperServer.awaitTicks(20);
			control(() -> bot.setForward(false));
			PaperServer.awaitTicks(10);
			var end = location(bot);
			assertTrue(end.getZ() - start.getZ() > 2, "bot did not walk along +Z: " + start + " -> " + end);
			assertTrue(Math.abs(end.getX() - start.getX()) < EPSILON, "bot drifted on X: " + start + " -> " + end);
			assertOnGroundAt(bot, FLOOR_Y + 1);
			assertFalse(events.moves.isEmpty(), "no PlayerMoveEvent for the bot");
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
			assertNoServerMoveWarnings(logs);
		} finally {
			cleanUp(roster, events);
			clear(structure);
		}
	}

	@Test
	void headingSeparateFromLook() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		try (var logs = new LogCapture()) {
			build(structure);
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(10);
			register(events);
			var start = location(bot);
			control(() -> {
				assertNull(bot.getHeading(), "heading does not follow the look by default");
				bot.setLook(0, 0);
				bot.setHeading(90f);
				bot.setForward(true);
			});
			PaperServer.awaitTicks(8);
			var midway = location(bot);
			PaperServer.awaitTicks(8);
			control(() -> bot.setForward(false));
			PaperServer.awaitTicks(10);
			var end = location(bot);
			assertTrue(start.getX() - end.getX() > 2, "bot did not walk toward -X: " + start + " -> " + end);
			assertTrue(Math.abs(end.getZ() - start.getZ()) < EPSILON, "bot drifted on Z: " + start + " -> " + end);
			assertEquals(0f, midway.getYaw(), 0.001f, "the look yaw followed the heading while walking");
			assertEquals(0f, end.getYaw(), 0.001f, "the look yaw followed the heading");
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
			assertNoServerMoveWarnings(logs);
		} finally {
			cleanUp(roster, events);
			clear(structure);
		}
	}

	@Test
	void jumpStepsUpOneBlock() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		try (var logs = new LogCapture()) {
			PaperServer.onMain(() -> structure.platform().fill(-RADIUS, FLOOR_Y + 1, 3, RADIUS, FLOOR_Y + 1, RADIUS));
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(10);
			register(events);
			control(() -> {
				bot.setForward(true);
				bot.setJump(true);
			});
			PaperServer.awaitTicks(25);
			control(() -> {
				bot.setForward(false);
				bot.setJump(false);
			});
			PaperServer.awaitTicks(20);
			var end = location(bot);
			assertTrue(end.getZ() > structure.cz + 3, "bot is not past the step: " + end);
			assertOnGroundAt(bot, FLOOR_Y + 2);
			assertFalse(events.jumps.isEmpty(), "no PlayerJumpEvent for the bot");
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
			assertNoServerMoveWarnings(logs);
		} finally {
			cleanUp(roster, events);
			clear(structure);
		}
	}

	@Test
	void walkOffLedgeLandsBelow() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		try (var logs = new LogCapture()) {
			// A 3-block ledge whose edge is at z = cz + 2.
			PaperServer.onMain(() -> structure.platform().fill(-RADIUS, FLOOR_Y + 1, -RADIUS, RADIUS, FLOOR_Y + 3, 1));
			var bot = spawn(roster, structure.at(FLOOR_Y + 4));
			PaperServer.awaitTicks(10);
			assertOnGroundAt(bot, FLOOR_Y + 4);
			register(events);
			control(() -> bot.setForward(true));
			PaperServer.awaitTicks(15);
			control(() -> bot.setForward(false));
			PaperServer.awaitTicks(30);
			var end = location(bot);
			assertTrue(end.getZ() > structure.cz + 2, "bot did not leave the ledge: " + end);
			assertOnGroundAt(bot, FLOOR_Y + 1);
			PaperServer.onMain(() -> {
				assertEquals(bot.player().getAttribute(Attribute.MAX_HEALTH).getValue(), bot.player().getHealth(),
						"the 3-block drop hurt the bot");
				return null;
			});
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
			assertNoServerMoveWarnings(logs);
		} finally {
			cleanUp(roster, events);
			clear(structure);
		}
	}

	@Test
	void lookReachesServerAndDrivesMovement() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		try (var logs = new LogCapture()) {
			build(structure);
			var spawnAt = structure.at(FLOOR_Y + 1);
			spawnAt.setYaw(45);
			spawnAt.setPitch(-20);
			var bot = spawn(roster, spawnAt);
			PaperServer.onMain(() -> {
				assertEquals(45f, bot.getLookYaw(), "look yaw is not the spawn yaw");
				assertEquals(-20f, bot.getLookPitch(), "look pitch is not the spawn pitch");
				return null;
			});
			PaperServer.awaitTicks(10);
			register(events);
			var start = location(bot);
			control(() -> {
				bot.setLook(90, 30);
				bot.setForward(true);
			});
			PaperServer.awaitTicks(15);
			control(() -> bot.setForward(false));
			PaperServer.awaitTicks(10);
			var end = location(bot);
			assertEquals(90f, end.getYaw(), 0.001f, "the look yaw did not reach the server");
			assertEquals(30f, end.getPitch(), 0.001f, "the look pitch did not reach the server");
			assertTrue(start.getX() - end.getX() > 2, "bot did not walk toward -X: " + start + " -> " + end);
			assertTrue(Math.abs(end.getZ() - start.getZ()) < EPSILON, "bot drifted on Z: " + start + " -> " + end);
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
			assertNoServerMoveWarnings(logs);
		} finally {
			cleanUp(roster, events);
			clear(structure);
		}
	}

	/** Holds {@code key} for 10 ticks, then lets the bot stop. */
	private static Location displacement(Bot bot, Consumer<Boolean> key) throws Exception {
		var start = location(bot);
		control(() -> key.accept(true));
		PaperServer.awaitTicks(10);
		control(() -> key.accept(false));
		PaperServer.awaitTicks(10);
		return location(bot).subtract(start);
	}

	@Test
	void strafeAndBackwardMoveAlongTheirAxes() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		try (var logs = new LogCapture()) {
			build(structure);
			// Facing +Z: left is +X, right is -X, backward is -Z.
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(10);
			register(events);
			var left = displacement(bot, bot::setLeft);
			var right = displacement(bot, bot::setRight);
			var backward = displacement(bot, bot::setBackward);
			assertTrue(left.getX() > 1 && Math.abs(left.getZ()) < EPSILON, "left did not move along +X: " + left);
			assertTrue(right.getX() < -1 && Math.abs(right.getZ()) < EPSILON, "right did not move along -X: " + right);
			assertTrue(backward.getZ() < -1 && Math.abs(backward.getX()) < EPSILON,
					"backward did not move along -Z: " + backward);
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
			assertNoServerMoveWarnings(logs);
		} finally {
			cleanUp(roster, events);
			clear(structure);
		}
	}

	/** Spawns a fresh bot, holds forward plus {@code extra} for 15 ticks. */
	private static double forwardDistance(BotRoster roster, Structure structure, Consumer<Bot> extra) throws Exception {
		var bot = spawn(roster, structure.at(FLOOR_Y + 1));
		try {
			PaperServer.awaitTicks(10);
			var start = location(bot);
			control(() -> {
				bot.setForward(true);
				extra.accept(bot);
			});
			PaperServer.awaitTicks(15);
			return location(bot).getZ() - start.getZ();
		} finally {
			PaperServer.onMain(() -> {
				roster.remove(bot);
				return null;
			});
		}
	}

	@Test
	void sneakSlowsAndSprintSpeedsWalking() throws Exception {
		var roster = roster();
		var structure = structure();
		try (var logs = new LogCapture()) {
			build(structure);
			double plain = forwardDistance(roster, structure, bot -> {
			});
			double sneaking = forwardDistance(roster, structure, bot -> bot.setSneak(true));
			double sprinting = forwardDistance(roster, structure, bot -> bot.setSprint(true));
			assertTrue(plain > 1, "plain walking barely moved: " + plain);
			assertTrue(sneaking < plain * 0.6, "sneaking is not clearly slower: " + sneaking + " vs " + plain);
			assertTrue(sprinting > plain * 1.15, "sprinting is not clearly faster: " + sprinting + " vs " + plain);
			assertNoServerMoveWarnings(logs);
		} finally {
			cleanUp(roster);
			clear(structure);
		}
	}

	@Test
	void teleportWhileWalkingStaysAtTarget() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		try (var logs = new LogCapture()) {
			build(structure);
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(10);
			register(events);
			control(() -> bot.setForward(true));
			PaperServer.awaitTicks(5);
			var target = structure.at(FLOOR_Y + 1).add(-5, 0, -5);
			PaperServer.onMain(() -> {
				bot.setForward(false);
				assertTrue(bot.player().teleport(target), "teleport refused");
				return null;
			});
			PaperServer.awaitTicks(15);
			var end = location(bot);
			assertTrue(end.distance(target) < EPSILON, "bot did not stay at the teleport target: " + end);
			assertOnGroundAt(bot, FLOOR_Y + 1);
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
			assertNoServerMoveWarnings(logs);
		} finally {
			cleanUp(roster, events);
			clear(structure);
		}
	}

	@Test
	void sneakAndSprintSendOnePacketPerChange() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		var packets = new Packets(NAME);
		try {
			build(structure);
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(10);
			register(events);
			register(packets);
			control(() -> bot.setSneak(true));
			PaperServer.awaitTicks(5);
			assertTrue(PaperServer.onMain(() -> bot.isSneak() && bot.player().isSneaking()), "bot is not sneaking");
			control(() -> bot.setSneak(false));
			PaperServer.awaitTicks(5);
			assertFalse(PaperServer.onMain(() -> bot.player().isSneaking()), "bot is still sneaking");
			control(() -> bot.setSprint(true));
			PaperServer.awaitTicks(5);
			control(() -> bot.setSprint(false));
			PaperServer.awaitTicks(5);
			assertEquals(4, packets.of(PacketType.Play.Client.STEER_VEHICLE).size(),
					"not one input packet per change: " + packets.seen);
			assertEquals(2, packets.of(PacketType.Play.Client.ENTITY_ACTION).size(),
					"not one sprint command per sprint change: " + packets.seen);
			assertEquals(List.of(true, false), List.copyOf(events.sneaks),
					"PlayerToggleSneakEvent not once per change");
			assertEquals(List.of(true, false), List.copyOf(events.sprints),
					"PlayerToggleSprintEvent not once per change");
		} finally {
			cleanUp(roster, events, packets);
			clear(structure);
		}
	}

	@Test
	void controlSetInTickEventAppliesThatTick() throws Exception {
		var roster = roster();
		var structure = structure();
		var packets = new Packets(NAME);
		Ticks ticks = null;
		try {
			build(structure);
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(10);
			var setAt = new AtomicInteger(-1);
			ticks = new Ticks(bot);
			ticks.hook = e -> {
				if (setAt.get() < 0) {
					setAt.set(Bukkit.getCurrentTick());
					e.getBot().setForward(true);
				}
			};
			register(packets);
			register(ticks);
			PaperServer.awaitTicks(5);
			control(() -> bot.setForward(false));
			var inputs = packets.of(PacketType.Play.Client.STEER_VEHICLE);
			assertTrue(setAt.get() >= 0, "the tick listener never ran");
			assertFalse(inputs.isEmpty(), "no input packet");
			assertEquals(setAt.get(), inputs.getFirst().tick(), "the input was not sent in the tick it was set");
		} finally {
			cleanUp(roster, packets, ticks);
			clear(structure);
		}
	}

	@Test
	void unchangedControlsSendOnlyMoves() throws Exception {
		var roster = roster();
		var structure = structure();
		var packets = new Packets(NAME);
		Ticks ticks = null;
		try {
			build(structure);
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(10);
			var counter = new Ticks(bot);
			ticks = counter;
			PaperServer.onMain(() -> {
				Bukkit.getPluginManager().registerEvents(counter, PaperServer.plugin());
				ProtocolLibrary.getProtocolManager().addPacketListener(packets);
				return null;
			});
			PaperServer.awaitTicks(20);
			// Freeze the counts: stop listening, in one go.
			PaperServer.onMain(() -> {
				HandlerList.unregisterAll(counter);
				ProtocolLibrary.getProtocolManager().removePacketListener(packets);
				return null;
			});
			int tickCount = counter.count.get();
			assertTrue(tickCount >= 20, "too few ticks: " + tickCount);
			assertEquals(List.of(), packets.of(PacketType.Play.Client.STEER_VEHICLE), "input sent without change");
			assertEquals(List.of(), packets.of(PacketType.Play.Client.ENTITY_ACTION), "command sent without change");
			assertEquals(tickCount, packets.of(PacketType.Play.Client.POSITION_LOOK).size(),
					"not one move packet per tick");
		} finally {
			cleanUp(roster, packets, ticks);
			clear(structure);
		}
	}

	@Test
	void refusedMoveIsLoggedOnce() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME, true);
		try (var logs = new LogCapture()) {
			build(structure);
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(10);
			register(events);
			var start = location(bot);
			PaperServer.onMain(() -> {
				// As a cheating client would: a move far beyond the bot's reach.
				assertInstanceOf(BotBody.class, bot).writeInbound(new ServerboundMovePlayerPacket.Pos(start.getX(),
						start.getY() + 100, start.getZ(), false, false));
				return null;
			});
			PaperServer.awaitTicks(5);
			assertEquals(1, events.fails.size(), "not one PlayerFailMoveEvent: " + events.fails);
			var lines = logs.byLogger.stream().filter(e -> e.getKey().startsWith("NekotineCore")).map(e -> e.getValue())
					.filter(l -> l.contains(" move ")).toList();
			assertEquals(1, lines.size(), "not one fail-move line: " + lines);
			assertTrue(lines.getFirst().startsWith(NAME + " move refused"), "unexpected line: " + lines);
			assertTrue(lines.getFirst().contains(String.valueOf(events.fails.peek())), "reason missing: " + lines);
			assertOnGroundAt(bot, FLOOR_Y + 1);
			assertTrue(PaperServer.onMain(() -> bot.player().isOnline()), "bot was disconnected");
		} finally {
			cleanUp(roster, events);
			clear(structure);
		}
	}

	@Test
	void idleBotStaysOnPlatform() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		var players = new PlayerEvents();
		try (var logs = new LogCapture()) {
			build(structure);
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			PaperServer.awaitTicks(5);
			register(events);
			register(players);
			var start = location(bot);
			PaperServer.awaitTicks(200);
			var end = location(bot);
			assertTrue(PaperServer.onMain(() -> bot.player().isOnline()), "idle bot was disconnected");
			assertTrue(players.kicked.isEmpty(), "idle bot was kicked");
			assertTrue(start.distance(end) < EPSILON, "idle bot moved: " + start + " -> " + end);
			assertOnGroundAt(bot, FLOOR_Y + 1);
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
			assertNoServerMoveWarnings(logs);
		} finally {
			cleanUp(roster, events, players);
			clear(structure);
		}
	}

	@Test
	void controlsAreMainThreadOnlyAndNeutralOnFailure() throws Exception {
		var roster = roster();
		var structure = structure();
		try {
			build(structure);
			var bot = spawn(roster, structure.at(FLOOR_Y + 1));
			assertThrows(IllegalStateException.class, () -> bot.setForward(true));
			assertThrows(IllegalStateException.class, () -> bot.setLook(0, 0));
			PaperServer.onMain(() -> {
				assertEquals(0f, bot.getLookYaw());
				assertThrows(IllegalArgumentException.class, () -> bot.setLook(Float.NaN, 0));
				assertThrows(IllegalArgumentException.class, () -> bot.setHeading(Float.POSITIVE_INFINITY));
				bot.setForward(true);
				bot.setSneak(true);
				bot.setHeading(45f);
				assertInstanceOf(BotBody.class, bot).fail();
				assertFalse(bot.isForward() || bot.isSneak(), "keys still held after failure");
				assertNull(bot.getHeading(), "heading not back to the look after failure");
				return null;
			});
		} finally {
			cleanUp(roster);
			clear(structure);
		}
	}
}
