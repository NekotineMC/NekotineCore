package fr.nekotine.botstesthost.it;

import static fr.nekotine.botstesthost.it.BotLifecycleIT.cleanUp;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.register;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.roster;
import static fr.nekotine.botstesthost.it.BotMovementIT.FLOOR_Y;
import static fr.nekotine.botstesthost.it.BotMovementIT.build;
import static fr.nekotine.botstesthost.it.BotMovementIT.clear;
import static fr.nekotine.botstesthost.it.BotMovementIT.structure;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import fr.nekotine.botstesthost.it.BotMovementIT.Structure;
import fr.nekotine.core.bot.Bot;
import fr.nekotine.core.bot.BotRoster;
import fr.nekotine.core.bot.BotState;
import fr.nekotine.core.bot.BotTickEvent;
import fr.nekotine.core.bot.internal.BotBody;
import fr.nekotine.core.navigation.Body;
import fr.nekotine.core.navigation.BodyControls;
import fr.nekotine.core.navigation.ControlPriority;
import fr.xxgoldenbluexx.paperjunitserver.PaperServer;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

/**
 * Story 5.1: strategies drive a real bot through its control port; per control
 * group and per tick the highest {@link ControlPriority} wins, whatever the
 * listener order, and port writes rank above direct {@link Bot} setters.
 */
class BotControlPortIT {

	private static final String NAME = "Bot_Port";

	private static final float EPSILON = 0.001f;

	/**
	 * Runs, at each tick event of one bot, the next queued step, then the repeating
	 * one if any. Subclasses fix the listener priority.
	 */
	abstract static class Hook implements Listener {
		final Bot bot;
		final Queue<Consumer<Bot>> steps = new ConcurrentLinkedQueue<>();
		volatile Consumer<Bot> every;

		Hook(Bot bot) {
			this.bot = bot;
		}

		void run(BotTickEvent event) {
			if (event.getBot() != bot) {
				return;
			}
			var step = steps.poll();
			if (step != null) {
				step.accept(bot);
			}
			var repeating = every;
			if (repeating != null) {
				repeating.accept(bot);
			}
		}
	}

	/** Runs before {@link Late} in the same tick. */
	public static final class Early extends Hook {
		Early(Bot bot) {
			super(bot);
		}

		@EventHandler(priority = EventPriority.LOW)
		public void onTick(BotTickEvent event) {
			run(event);
		}
	}

	/** Runs after {@link Early} in the same tick. */
	public static final class Late extends Hook {
		Late(Bot bot) {
			super(bot);
		}

		@EventHandler(priority = EventPriority.HIGH)
		public void onTick(BotTickEvent event) {
			run(event);
		}
	}

	/** Records the hand packets the bot sends. */
	private static final class Packets extends PacketAdapter {
		final Queue<PacketType> seen = new ConcurrentLinkedQueue<>();

		Packets() {
			super(PaperServer.plugin(), PacketType.Play.Client.HELD_ITEM_SLOT, PacketType.Play.Client.USE_ITEM,
					PacketType.Play.Client.ARM_ANIMATION);
		}

		@Override
		public void onPacketReceiving(PacketEvent event) {
			if (event.getPlayer().getName().equals(NAME)) {
				seen.add(event.getPacketType());
			}
		}
	}

	/** Records the bot's interact events. */
	public static final class Interacts implements Listener {
		final Queue<PlayerInteractEvent> seen = new ConcurrentLinkedQueue<>();

		@EventHandler
		public void onInteract(PlayerInteractEvent event) {
			if (event.getPlayer().getName().equals(NAME)) {
				seen.add(event);
			}
		}
	}

	private record Scene(BotRoster roster, Structure structure, Bot bot, Early early, Late late, Packets packets,
			Interacts interacts) {
	}

	/** Builds the platform, spawns the bot facing +Z, lets it settle. */
	private static Scene scene() throws Exception {
		var roster = roster();
		var structure = structure();
		build(structure);
		var bot = PaperServer.onMain(() -> roster.spawn(NAME, structure.at(FLOOR_Y + 1)));
		PaperServer.awaitTicks(10);
		var scene = new Scene(roster, structure, bot, new Early(bot), new Late(bot), new Packets(), new Interacts());
		register(scene.early());
		register(scene.late());
		register(scene.packets());
		register(scene.interacts());
		return scene;
	}

	private static void tearDown(Scene scene) throws Exception {
		if (scene == null) {
			return;
		}
		cleanUp(scene.roster(), scene.early(), scene.late(), scene.packets(), scene.interacts());
		clear(scene.structure());
	}

	private static BodyControls port(Bot bot, ControlPriority priority) {
		return bot.body().controls(priority);
	}

	/** Queues one step on each hook, in one main-thread task, then waits. */
	private static void step(Scene scene, Consumer<Bot> early, Consumer<Bot> late) throws Exception {
		PaperServer.onMain(() -> {
			if (early != null) {
				scene.early().steps.add(early);
			}
			if (late != null) {
				scene.late().steps.add(late);
			}
			return null;
		});
		PaperServer.awaitTicks(3);
	}

	private static void assertLook(Bot bot, float yaw, float pitch) throws Exception {
		PaperServer.onMain(() -> {
			assertEquals(yaw, bot.getLookYaw(), EPSILON, "bot look yaw");
			assertEquals(pitch, bot.getLookPitch(), EPSILON, "bot look pitch");
			assertEquals(yaw, bot.body().getLookYaw(), EPSILON, "port look yaw");
			assertEquals(pitch, bot.body().getLookPitch(), EPSILON, "port look pitch");
			var location = bot.player().getLocation();
			assertEquals(yaw, location.getYaw(), EPSILON, "server yaw");
			assertEquals(pitch, location.getPitch(), EPSILON, "server pitch");
			return null;
		});
	}

	@Test
	void higherAimWinsInEitherListenerOrder() throws Exception {
		Scene scene = null;
		try {
			scene = scene();
			var bot = scene.bot();
			// Walker listens first, melee last.
			step(scene, b -> port(b, ControlPriority.WALKER).setLook(10, 0),
					b -> port(b, ControlPriority.MELEE).setLook(40, 5));
			assertLook(bot, 40, 5);
			// Melee listens first, walker last.
			step(scene, b -> port(b, ControlPriority.MELEE).setLook(60, -5),
					b -> port(b, ControlPriority.WALKER).setLook(20, 0));
			assertLook(bot, 60, -5);
			// Melee stops writing: the walker's look applies.
			step(scene, b -> port(b, ControlPriority.WALKER).setLook(30, 2), null);
			assertLook(bot, 30, 2);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void groupWinDropsTheLowerWritersHandsWrites() throws Exception {
		Scene scene = null;
		try {
			scene = scene();
			var bot = scene.bot();
			step(scene, b -> port(b, ControlPriority.WALKER).leftClick(),
					b -> port(b, ControlPriority.MELEE).setSlot(5));
			assertEquals(5, PaperServer.onMain(bot::getSlot));
			assertEquals(5, PaperServer.onMain(() -> bot.player().getInventory().getHeldItemSlot()));
			assertEquals(List.of(PacketType.Play.Client.HELD_ITEM_SLOT), List.copyOf(scene.packets().seen),
					"the walker's left click was sent");
			assertTrue(scene.interacts().seen.isEmpty(), "the walker's left click fired an interact");
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void loneWriterAppliesAllItsHandsWrites() throws Exception {
		Scene scene = null;
		try {
			scene = scene();
			var bot = scene.bot();
			step(scene, b -> {
				var walker = port(b, ControlPriority.WALKER);
				walker.setSlot(2);
				walker.useItem(EquipmentSlot.HAND);
			}, null);
			assertEquals(2, PaperServer.onMain(bot::getSlot));
			assertEquals(List.of(PacketType.Play.Client.HELD_ITEM_SLOT, PacketType.Play.Client.USE_ITEM),
					List.copyOf(scene.packets().seen));
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void portWritesOverrideDirectWritesOfTheSameTick() throws Exception {
		Scene scene = null;
		try {
			scene = scene();
			var bot = scene.bot();
			var start = PaperServer.onMain(() -> bot.player().getLocation());
			// Direct first, then the port, then the reverse: the port wins both.
			scene.early().every = b -> {
				b.setForward(false);
				port(b, ControlPriority.WALKER).setForward(true);
			};
			PaperServer.awaitTicks(5);
			assertTrue(PaperServer.onMain(bot::isForward), "the port's forward did not hold over a prior direct write");
			scene.early().every = b -> {
				port(b, ControlPriority.WALKER).setForward(true);
				b.setForward(false);
			};
			PaperServer.awaitTicks(5);
			assertTrue(PaperServer.onMain(bot::isForward), "the port's forward did not hold over a later direct write");
			scene.early().every = null;
			PaperServer.awaitTicks(1);
			var moved = PaperServer.onMain(() -> bot.player().getLocation()).getZ() - start.getZ();
			assertTrue(moved > 1, "the bot did not walk forward: " + moved);
			// No port write: a direct write stands.
			PaperServer.onMain(() -> {
				bot.setForward(false);
				return null;
			});
			PaperServer.awaitTicks(3);
			assertFalse(PaperServer.onMain(bot::isForward), "the direct release did not stand");
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void higherMovementWriterKeepsTheLowerPersistedKeys() throws Exception {
		Scene scene = null;
		try {
			scene = scene();
			var bot = scene.bot();
			step(scene, b -> port(b, ControlPriority.WALKER).setForward(true), null);
			// The brain takes the movement group with sneak only: forward persists.
			step(scene, b -> port(b, ControlPriority.WALKER).setForward(false),
					b -> port(b, ControlPriority.BRAIN).setSneak(true));
			PaperServer.onMain(() -> {
				assertTrue(bot.isForward(), "forward was released by a dropped write");
				assertTrue(bot.isSneak(), "the brain's sneak did not apply");
				return null;
			});
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void writeOutsideTheEventAppliesAtTheNextTick() throws Exception {
		Scene scene = null;
		try {
			scene = scene();
			var bot = scene.bot();
			float before = PaperServer.onMain(bot::getLookYaw);
			PaperServer.onMain(() -> {
				port(bot, ControlPriority.WALKER).setLook(77, 0);
				assertEquals(before, bot.getLookYaw(), EPSILON, "a port write applied before the bot's tick");
				return null;
			});
			PaperServer.awaitTicks(2);
			assertLook(bot, 77, 0);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void readsFollowTheBot() throws Exception {
		Scene scene = null;
		try {
			scene = scene();
			var bot = scene.bot();
			Body body = PaperServer.onMain(bot::body);
			assertSame(body, PaperServer.onMain(bot::body));
			PaperServer.onMain(() -> {
				var location = body.getLocation();
				assertEquals(bot.player().getLocation(), location);
				location.add(0, 10, 0);
				assertEquals(FLOOR_Y + 1, body.getLocation().getY(), 0.01, "the location is not a copy");
				assertTrue(body.isOnGround(), "a settled bot is not on ground");
				// Gravity only, cancelled by the floor each tick.
				assertEquals(0, body.getVelocity().getY(), 0.1, "a settled bot moves vertically");
				return null;
			});
			var airborne = new ConcurrentLinkedQueue<Boolean>();
			var velocities = new ConcurrentLinkedQueue<Double>();
			// Consecutive ticks: jump, then read.
			var early = scene.early();
			PaperServer.onMain(() -> {
				early.steps.add(b -> port(b, ControlPriority.WALKER).setJump(true));
				early.steps.add(b -> {
					// The client state after the jump tick.
					airborne.add(!b.body().isOnGround());
					velocities.add(b.body().getVelocity().getY());
					port(b, ControlPriority.WALKER).setJump(false);
				});
				return null;
			});
			PaperServer.awaitTicks(4);
			assertEquals(List.of(true), List.copyOf(airborne), "the bot is on ground after a jump");
			assertTrue(velocities.peek() != null && velocities.peek() > 0,
					"the bot does not rise after a jump: " + velocities);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void badWritesAreRejectedAtCall() throws Exception {
		Scene scene = null;
		Pig pig = null;
		try {
			scene = scene();
			var bot = scene.bot();
			var structure = scene.structure();
			World other = PaperServer.onMain(() -> Bukkit.getWorlds().stream().filter(w -> !w.equals(structure.world))
					.findFirst().orElseThrow());
			pig = PaperServer.onMain(() -> {
				var spawned = (Pig) other.spawnEntity(other.getSpawnLocation(), EntityType.PIG);
				spawned.setAI(false);
				return spawned;
			});
			var otherPig = pig;
			var walker = PaperServer.onMain(() -> port(bot, ControlPriority.WALKER));
			assertThrows(IllegalStateException.class, () -> walker.setForward(true));
			assertThrows(IllegalStateException.class, () -> walker.setLook(0, 0));
			assertThrows(IllegalStateException.class, () -> walker.setSlot(1));
			assertThrows(IllegalStateException.class, walker::leftClick);
			var body = bot.body();
			assertThrows(IllegalStateException.class, body::getLocation);
			assertThrows(IllegalStateException.class, body::getVelocity);
			assertThrows(IllegalStateException.class, body::isOnGround);
			assertThrows(IllegalStateException.class, body::getLookYaw);
			assertThrows(IllegalStateException.class, body::getLookPitch);
			PaperServer.onMain(() -> {
				var ownBlock = structure.world.getBlockAt(structure.cx, FLOOR_Y, structure.cz + 1);
				var otherBlock = other.getBlockAt(0, 64, 0);
				assertThrows(IllegalArgumentException.class, () -> walker.setLook(Float.NaN, 0));
				assertThrows(IllegalArgumentException.class, () -> walker.setLook(0, Float.POSITIVE_INFINITY));
				assertThrows(IllegalArgumentException.class, () -> walker.setHeading(Float.NaN));
				assertThrows(IllegalArgumentException.class, () -> walker.setSlot(9));
				assertThrows(IllegalArgumentException.class, () -> walker.useItem(EquipmentSlot.HEAD));
				assertThrows(IllegalArgumentException.class,
						() -> walker.useItemOn(ownBlock, BlockFace.NORTH_EAST, EquipmentSlot.HAND));
				assertThrows(IllegalArgumentException.class,
						() -> walker.useItemOn(otherBlock, BlockFace.UP, EquipmentSlot.HAND));
				assertThrows(IllegalArgumentException.class, () -> walker.startBreaking(otherBlock, BlockFace.UP));
				assertThrows(IllegalArgumentException.class, () -> walker.attack(otherPig));
				assertThrows(IllegalArgumentException.class, () -> walker.interact(otherPig, EquipmentSlot.HAND));
				return null;
			});
			PaperServer.awaitTicks(3);
			assertEquals(List.of(), List.copyOf(scene.packets().seen), "a rejected write was sent");
			assertEquals(0, PaperServer.onMain(bot::getSlot));
		} finally {
			if (pig != null) {
				var spawned = pig;
				PaperServer.onMain(() -> {
					spawned.remove();
					return null;
				});
			}
			tearDown(scene);
		}
	}

	@Test
	void writesToAFailedOrRemovedBotDoNotThrow() throws Exception {
		Scene scene = null;
		try {
			scene = scene();
			var bot = scene.bot();
			var roster = scene.roster();
			PaperServer.onMain(() -> {
				assertInstanceOf(BotBody.class, bot).fail();
				assertDoesNotThrow(() -> port(bot, ControlPriority.BRAIN).setSlot(6));
				assertDoesNotThrow(() -> port(bot, ControlPriority.BRAIN).leftClick());
				roster.remove(bot);
				assertDoesNotThrow(() -> port(bot, ControlPriority.WALKER).setForward(true));
				assertDoesNotThrow(() -> port(bot, ControlPriority.WALKER).leftClick());
				return null;
			});
		} finally {
			tearDown(scene);
		}
	}

	/** A NoAI pig on the platform, {@code dz} blocks in front of the bot. */
	private static Pig pig(Structure structure, double dz) throws Exception {
		return PaperServer.onMain(() -> {
			var at = structure.at(FLOOR_Y + 1).add(0, 0, dz);
			var pig = (Pig) at.getWorld().spawnEntity(at, EntityType.PIG);
			pig.setAI(false);
			return pig;
		});
	}

	private static void remove(Pig pig) throws Exception {
		if (pig != null) {
			PaperServer.onMain(() -> {
				pig.remove();
				return null;
			});
		}
	}

	@Test
	void portTargetsReachTheBot() throws Exception {
		Scene scene = null;
		Pig pig = null;
		try {
			scene = scene();
			var structure = scene.structure();
			var target = pig(structure, 2);
			pig = target;
			var block = PaperServer.onMain(() -> structure.world.getBlockAt(structure.cx, FLOOR_Y, structure.cz + 1));
			step(scene, b -> port(b, ControlPriority.WALKER).useItemOn(block, BlockFace.UP, EquipmentSlot.HAND), null);
			var interacts = List.copyOf(scene.interacts().seen);
			assertEquals(1, interacts.size(), "useItemOn fired no single interact: " + interacts);
			assertEquals(Action.RIGHT_CLICK_BLOCK, interacts.getFirst().getAction());
			assertEquals(block, interacts.getFirst().getClickedBlock());
			assertEquals(BlockFace.UP, interacts.getFirst().getBlockFace());
			step(scene, b -> port(b, ControlPriority.MELEE).attack(target), null);
			assertEquals(List.of(PacketType.Play.Client.ARM_ANIMATION), List.copyOf(scene.packets().seen),
					"the attack's swing was not sent");
			assertTrue(
					PaperServer.onMain(() -> target.getHealth() < target.getAttribute(Attribute.MAX_HEALTH).getValue()),
					"the pig was not hit");
		} finally {
			remove(pig);
			tearDown(scene);
		}
	}

	@Test
	void targetLeftBehindByAWorldChangeIsDropped() throws Exception {
		Scene scene = null;
		Pig pig = null;
		try {
			scene = scene();
			var bot = scene.bot();
			var structure = scene.structure();
			var target = pig(structure, 2);
			pig = target;
			World other = PaperServer.onMain(() -> Bukkit.getWorlds().stream().filter(w -> !w.equals(structure.world))
					.findFirst().orElseThrow());
			PaperServer.onMain(() -> {
				// Outside the tick event: replayed at the bot's next tick.
				var walker = port(bot, ControlPriority.WALKER);
				walker.useItemOn(structure.world.getBlockAt(structure.cx, FLOOR_Y, structure.cz + 1), BlockFace.UP,
						EquipmentSlot.HAND);
				walker.attack(target);
				walker.interact(target, EquipmentSlot.HAND);
				walker.startBreaking(structure.world.getBlockAt(structure.cx, FLOOR_Y, structure.cz + 1), BlockFace.UP);
				assertTrue(bot.player().teleport(other.getSpawnLocation()), "the teleport was refused");
				return null;
			});
			PaperServer.awaitTicks(5);
			assertEquals(BotState.ACTIVE, PaperServer.onMain(bot::state), "a stale target failed the bot");
			assertEquals(List.of(), List.copyOf(scene.packets().seen), "a stale target was sent");
			assertTrue(scene.interacts().seen.isEmpty(), "a stale target fired an interact");
		} finally {
			remove(pig);
			tearDown(scene);
		}
	}
}
