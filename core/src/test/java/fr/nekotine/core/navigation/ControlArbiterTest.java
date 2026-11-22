package fr.nekotine.core.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

/**
 * Story 5.1: the arbitration rules of the control port, against a recording
 * sink, with no server.
 */
class ControlArbiterTest {

	/** Records every control call it gets, in order. */
	private static class Recorder implements BodyControls {
		final List<String> calls = new ArrayList<>();

		@Override
		public void setForward(boolean forward) {
			calls.add("forward " + forward);
		}

		@Override
		public void setBackward(boolean backward) {
			calls.add("backward " + backward);
		}

		@Override
		public void setLeft(boolean left) {
			calls.add("left " + left);
		}

		@Override
		public void setRight(boolean right) {
			calls.add("right " + right);
		}

		@Override
		public void setJump(boolean jump) {
			calls.add("jump " + jump);
		}

		@Override
		public void setSneak(boolean sneak) {
			calls.add("sneak " + sneak);
		}

		@Override
		public void setSprint(boolean sprint) {
			calls.add("sprint " + sprint);
		}

		@Override
		public void setHeading(Float yaw) {
			calls.add("heading " + yaw);
		}

		@Override
		public void setLook(float yaw, float pitch) {
			calls.add("look " + yaw + " " + pitch);
		}

		@Override
		public void setSlot(int slot) {
			calls.add("slot " + slot);
		}

		@Override
		public void useItem(EquipmentSlot hand) {
			calls.add("useItem " + hand);
		}

		@Override
		public void useItemOn(Block block, BlockFace face, EquipmentSlot hand) {
			calls.add("useItemOn " + face + " " + hand);
		}

		@Override
		public void leftClick() {
			calls.add("leftClick");
		}

		@Override
		public void startBreaking(Block block, BlockFace face) {
			calls.add("startBreaking " + face);
		}

		@Override
		public void attack(Entity target) {
			calls.add("attack");
		}

		@Override
		public void interact(Entity target, EquipmentSlot hand) {
			calls.add("interact " + hand);
		}

		@Override
		public void swapHands() {
			calls.add("swapHands");
		}
	}

	private static final World WORLD = world("world");

	private static final World OTHER = world("other");

	private boolean onMain = true;

	private boolean live = true;

	private final ControlArbiter arbiter = new ControlArbiter("Body", () -> {
		if (!onMain) {
			throw new IllegalStateException("off main");
		}
	}, () -> WORLD, () -> live);

	/** A stand-in world: identity equality, a name. */
	private static World world(String name) {
		return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
				(proxy, method, args) -> switch (method.getName()) {
					case "getName", "toString" -> name;
					case "equals" -> proxy == args[0];
					case "hashCode" -> System.identityHashCode(proxy);
					default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	/** A stand-in block or entity in {@code world}. */
	private static <T> T in(Class<T> type, World world) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
				(proxy, method, args) -> switch (method.getName()) {
					case "getWorld" -> world;
					case "toString" -> type.getSimpleName();
					case "equals" -> proxy == args[0];
					case "hashCode" -> System.identityHashCode(proxy);
					default -> throw new UnsupportedOperationException(method.getName());
				}));
	}

	private List<String> apply() {
		var sink = new Recorder();
		arbiter.applyTo(sink);
		return sink.calls;
	}

	private BodyControls walker() {
		return arbiter.writer(ControlPriority.WALKER);
	}

	private BodyControls melee() {
		return arbiter.writer(ControlPriority.MELEE);
	}

	private BodyControls brain() {
		return arbiter.writer(ControlPriority.BRAIN);
	}

	@Test
	void priorityOrderIsWalkerMeleeBrain() {
		assertEquals(List.of(ControlPriority.WALKER, ControlPriority.MELEE, ControlPriority.BRAIN),
				List.of(ControlPriority.values()));
	}

	@Test
	void writerIsStablePerPriority() {
		assertSame(walker(), walker());
	}

	@Test
	void higherAimWinsWhicheverWritesFirst() {
		walker().setLook(10, 0);
		melee().setLook(20, 5);
		assertEquals(List.of("look 20.0 5.0"), apply());
		melee().setLook(20, 5);
		walker().setLook(10, 0);
		assertEquals(List.of("look 20.0 5.0"), apply());
	}

	@Test
	void lowerWriterAppliesOnceTheHigherStops() {
		walker().setLook(10, 0);
		melee().setLook(20, 5);
		apply();
		walker().setLook(30, 0);
		assertEquals(List.of("look 30.0 0.0"), apply());
	}

	@Test
	void winIsPerGroupNotPerControl() {
		walker().leftClick();
		melee().setSlot(5);
		assertEquals(List.of("slot 5"), apply());
	}

	@Test
	void loneWriterAppliesAllItsWritesInCallOrder() {
		walker().setSlot(2);
		walker().useItem(EquipmentSlot.HAND);
		walker().setSlot(3);
		assertEquals(List.of("slot 2", "useItem HAND", "slot 3"), apply());
	}

	@Test
	void groupsArbitrateIndependently() {
		walker().setForward(true);
		walker().setLook(10, 0);
		melee().setLook(20, 0);
		brain().swapHands();
		walker().leftClick();
		assertEquals(List.of("forward true", "look 20.0 0.0", "swapHands"), apply());
	}

	@Test
	void higherWriterSettingOneKeyDropsLowerMovementWrites() {
		walker().setForward(true);
		walker().setHeading(90f);
		brain().setSneak(true);
		assertEquals(List.of("sneak true"), apply());
	}

	@Test
	void nothingWrittenReplaysNothing() {
		assertEquals(List.of(), apply());
		walker().setJump(true);
		apply();
		assertEquals(List.of(), apply());
	}

	@Test
	void writesWhileNotLiveAreDropped() {
		live = false;
		walker().setForward(true);
		live = true;
		assertEquals(List.of(), apply());
	}

	@Test
	void clearForgetsPendingWrites() {
		walker().setForward(true);
		melee().setLook(1, 1);
		arbiter.clear();
		assertEquals(List.of(), apply());
		// A lower writer is no longer shadowed after a clear.
		melee().setLook(1, 1);
		arbiter.clear();
		walker().setLook(2, 2);
		assertEquals(List.of("look 2.0 2.0"), apply());
	}

	@Test
	void nullHeadingFollowsLook() {
		walker().setHeading(null);
		assertEquals(List.of("heading null"), apply());
	}

	@Test
	void badArgumentsAreRejectedAtCall() {
		var w = walker();
		assertThrows(IllegalArgumentException.class, () -> w.setLook(Float.NaN, 0));
		assertThrows(IllegalArgumentException.class, () -> w.setLook(0, Float.POSITIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> w.setHeading(Float.NEGATIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> w.setSlot(9));
		assertThrows(IllegalArgumentException.class, () -> w.setSlot(-1));
		assertThrows(IllegalArgumentException.class, () -> w.useItem(EquipmentSlot.HEAD));
		assertThrows(IllegalArgumentException.class, () -> w.useItem(null));
		var block = in(Block.class, WORLD);
		assertThrows(IllegalArgumentException.class,
				() -> w.useItemOn(block, BlockFace.NORTH_EAST, EquipmentSlot.HAND));
		assertThrows(IllegalArgumentException.class, () -> w.startBreaking(block, BlockFace.SELF));
		assertThrows(IllegalArgumentException.class, () -> w.useItemOn(block, BlockFace.UP, EquipmentSlot.FEET));
		var elsewhere = in(Block.class, OTHER);
		assertThrows(IllegalArgumentException.class, () -> w.startBreaking(elsewhere, BlockFace.UP));
		var stranger = in(Entity.class, OTHER);
		assertThrows(IllegalArgumentException.class, () -> w.attack(stranger));
		assertThrows(IllegalArgumentException.class, () -> w.interact(stranger, EquipmentSlot.HAND));
		assertThrows(IllegalArgumentException.class, () -> w.interact(in(Entity.class, WORLD), EquipmentSlot.CHEST));
		assertEquals(List.of(), apply(), "a rejected write was recorded");
	}

	@Test
	void validTargetsAreRecorded() {
		var block = in(Block.class, WORLD);
		var entity = in(Entity.class, WORLD);
		var w = walker();
		w.useItemOn(block, BlockFace.UP, EquipmentSlot.OFF_HAND);
		w.startBreaking(block, BlockFace.WEST);
		w.attack(entity);
		w.interact(entity, EquipmentSlot.HAND);
		assertEquals(List.of("useItemOn UP OFF_HAND", "startBreaking WEST", "attack", "interact HAND"), apply());
	}

	@Test
	void writesOffTheMainThreadAreRejected() {
		onMain = false;
		assertThrows(IllegalStateException.class, () -> walker().setForward(true));
		assertThrows(IllegalStateException.class, () -> melee().setLook(0, 0));
		assertThrows(IllegalStateException.class, () -> brain().leftClick());
		assertThrows(IllegalStateException.class, () -> brain().setSlot(99));
		onMain = true;
		assertEquals(List.of(), apply());
	}

	@Test
	void writesDuringReplayWaitForTheNextReplay() {
		walker().setForward(true);
		var sink = new Recorder() {
			@Override
			public void setForward(boolean forward) {
				super.setForward(forward);
				walker().setJump(true);
			}
		};
		arbiter.applyTo(sink);
		assertEquals(List.of("forward true"), sink.calls);
		assertEquals(List.of("jump true"), apply());
	}
}
