package fr.nekotine.core.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

/**
 * Story 5.3: the walker against a fake body whose writes go through a real
 * {@link ControlArbiter}, on hand-built paths, with no server.
 */
class PathWalkerTest {

	/** Records every control call it gets, in order. */
	private static final class Recorder implements BodyControls {
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
			calls.add("useItemOn " + block + " " + face + " " + hand);
		}

		@Override
		public void leftClick() {
			calls.add("leftClick");
		}

		@Override
		public void startBreaking(Block block, BlockFace face) {
			calls.add("startBreaking");
		}

		@Override
		public void attack(Entity target) {
			calls.add("attack");
		}

		@Override
		public void interact(Entity target, EquipmentSlot hand) {
			calls.add("interact");
		}

		@Override
		public void swapHands() {
			calls.add("swapHands");
		}
	}

	/** A body that stands where the test puts it. */
	private static final class FakeBody implements Body {
		World world;
		final ControlArbiter arbiter;
		double x;
		double y;
		double z;
		boolean onGround = true;

		FakeBody(World world) {
			this.world = world;
			this.arbiter = new ControlArbiter("fake", () -> {
			}, () -> this.world, () -> true);
		}

		FakeBody at(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		/** @return the writes that won since the last call, in replay order */
		List<String> applied() {
			var recorder = new Recorder();
			arbiter.applyTo(recorder);
			return recorder.calls;
		}

		@Override
		public Location getLocation() {
			return new Location(world, x, y, z);
		}

		@Override
		public org.bukkit.util.Vector getVelocity() {
			return new org.bukkit.util.Vector();
		}

		@Override
		public boolean isOnGround() {
			return onGround;
		}

		@Override
		public float getLookYaw() {
			return 0;
		}

		@Override
		public float getLookPitch() {
			return 0;
		}

		@Override
		public BodyControls controls(ControlPriority priority) {
			return arbiter.writer(priority);
		}
	}

	/** A plain path, no hints. */
	private record Path(List<Location> points, boolean reaches) implements PathResult {

		@Override
		public List<Location> getPoints() {
			return points.stream().map(Location::clone).toList();
		}

		@Override
		public int getNextPointIndex() {
			return 0;
		}

		@Override
		public Location getNextPoint() {
			return points.getFirst().clone();
		}

		@Override
		public Location getFinalPoint() {
			return points.getLast().clone();
		}

		@Override
		public boolean canReachFinalPoint() {
			return reaches;
		}
	}

	/** A finder whose searches the test completes by hand. */
	private static final class ManualFinder implements PathFinder {
		final List<CompletableFuture<PathResult>> searches = new ArrayList<>();
		final List<Location> goals = new ArrayList<>();

		@Override
		public CompletableFuture<PathResult> find(Location from, Location to, PathRule rule) {
			var future = new CompletableFuture<PathResult>();
			searches.add(future);
			goals.add(to);
			return future;
		}
	}

	private static final List<String> NEUTRAL = List.of("heading null", "forward false", "backward false", "left false",
			"right false", "jump false", "sneak false", "sprint false");

	private final World world = world("w");

	private final FakeBody body = new FakeBody(world).at(0.5, 0, 0.5);

	/** Closed doors of the fake world, by "x y z". */
	private final Set<String> closedDoors = new HashSet<>();

	private boolean onMain = true;

	private final PathWalker.Doors doors = (w, x, y,
			z) -> closedDoors.contains(x + " " + y + " " + z) ? block(x + " " + y + " " + z) : null;

	private PathWalker walker(int stuckTicks) {
		return new PathWalker(body, stuckTicks, () -> onMain, doors);
	}

	private PathWalker walker() {
		return walker(PathWalker.DEFAULT_STUCK_TICKS);
	}

	private static World world(String name) {
		return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
				(proxy, method, args) -> switch (method.getName()) {
					case "getName", "toString" -> name;
					case "equals" -> proxy == args[0];
					case "hashCode" -> System.identityHashCode(proxy);
					default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	private Block block(String name) {
		return (Block) Proxy.newProxyInstance(Block.class.getClassLoader(), new Class<?>[]{Block.class},
				(proxy, method, args) -> switch (method.getName()) {
					case "getWorld" -> world;
					case "toString" -> "door(" + name + ")";
					case "equals" -> proxy == args[0];
					case "hashCode" -> System.identityHashCode(proxy);
					default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	/** A path through feet block positions given as x, y, z triples. */
	private Path path(boolean reaches, int... xyz) {
		var points = new ArrayList<Location>();
		for (int i = 0; i < xyz.length; i += 3) {
			points.add(new Location(world, xyz[i], xyz[i + 1], xyz[i + 2]));
		}
		return new Path(points, reaches);
	}

	private Path path(int... xyz) {
		return path(true, xyz);
	}

	/** The movement writes of a walking tick heading at {@code yaw}. */
	private static List<String> walking(float yaw, boolean jump) {
		return List.of("heading " + yaw, "forward true", "backward false", "left false", "right false", "jump " + jump,
				"sneak false", "sprint false");
	}

	private static List<String> movement(List<String> applied) {
		return applied.stream().filter(c -> !c.startsWith("look") && !c.startsWith("useItemOn")).toList();
	}

	@Test
	void cursorAdvancesAndTheBodyArrives() {
		var walker = walker();
		assertEquals(WalkStatus.IDLE, walker.tick());
		assertEquals(List.of(), body.applied(), "an idle walker wrote");
		var path = path(0, 0, 0, 0, 0, 1, 0, 0, 2, 1, 0, 3);
		walker.follow(path);
		assertSame(path, walker.getPath());
		assertEquals(WalkStatus.WALKING, walker.tick());
		var applied = body.applied();
		assertEquals(walking(0f, false), movement(applied), "not toward +Z");
		assertEquals("look 0.0 0.0", applied.getLast());
		// In the second point's block: the cursor moves to the third.
		body.at(0.5, 0, 1.2);
		assertEquals(WalkStatus.WALKING, walker.tick());
		assertEquals(walking(0f, false), movement(body.applied()));
		// In the third point's block: heading for the last one, diagonally.
		body.at(0.5, 0, 2.5);
		assertEquals(WalkStatus.WALKING, walker.tick());
		assertEquals(walking(-45f, false), movement(body.applied()), "not toward +X+Z");
		// Within 0.5 of the last point's centre, at its level.
		body.at(1.3, 0, 3.3);
		assertEquals(WalkStatus.ARRIVED, walker.tick());
		assertEquals(NEUTRAL, body.applied(), "neutral movement not written once");
		assertEquals(WalkStatus.ARRIVED, walker.tick());
		assertEquals(List.of(), body.applied(), "an arrived walker wrote again");
		assertEquals(WalkStatus.ARRIVED, walker.getStatus());
	}

	@Test
	void notAtTheFinalLevelIsNotArrived() {
		var walker = walker();
		walker.follow(path(0, 0, 0, 0, 1, 1));
		body.at(0.5, 0, 1.4);
		assertEquals(WalkStatus.WALKING, walker.tick());
	}

	@Test
	void aStepIsJumpedFromTheGroundWithinReach() {
		var walker = walker();
		walker.follow(path(0, 0, 0, 0, 0, 1, 0, 1, 2, 0, 1, 3));
		// At the start: walking to the second point, level.
		walker.tick();
		assertEquals(walking(0f, false), movement(body.applied()));
		// In the second point's block, 1 block from the step: jump.
		body.at(0.5, 0, 1.5);
		walker.tick();
		var applied = body.applied();
		assertEquals(walking(0f, true), movement(applied));
		assertTrue(applied.getLast().startsWith("look 0.0 -"), "not looking up at the step: " + applied.getLast());
		// In the air: no jump.
		body.onGround = false;
		body.at(0.5, 0.4, 1.6);
		walker.tick();
		assertEquals(walking(0f, false), movement(body.applied()));
		// On the step: the next point is level, no jump.
		body.onGround = true;
		body.at(0.5, 1, 2.5);
		walker.tick();
		assertEquals(walking(0f, false), movement(body.applied()));
	}

	@Test
	void aStepTooFarIsNotJumped() {
		var walker = walker();
		walker.follow(path(0, 0, 0, 0, 1, 2));
		body.at(0.5, 0, 0.5);
		walker.tick();
		assertEquals(walking(0f, false), movement(body.applied()), "jumped 2 blocks from the step");
	}

	@Test
	void aDropIsReachedOnlyOnceLanded() {
		var walker = walker();
		walker.follow(path(0, 2, 0, 0, 2, 1, 0, 0, 2, 0, 0, 3, 0, 0, 4));
		body.at(0.5, 2, 1.5);
		walker.tick();
		body.applied();
		// Falling through the drop point's block: still heading for it.
		body.onGround = false;
		body.at(0.8, 0.5, 2.5);
		walker.tick();
		assertEquals(walking(90f, false), movement(body.applied()), "the drop point was reached in the air");
		// Landed in it: on to the next.
		body.onGround = true;
		body.at(0.8, 0, 2.5);
		walker.tick();
		var movement = movement(body.applied());
		float yaw = Float.parseFloat(movement.getFirst().substring("heading ".length()));
		assertTrue(yaw > 0 && yaw < 45, "not heading for the point after the drop: " + movement);
	}

	@Test
	void aFinalDropPointIsArrivedAtOnlyOnceLanded() {
		var walker = walker();
		walker.follow(path(0, 2, 0, 0, 2, 1, 0, 0, 2));
		body.at(0.5, 2, 1.5);
		assertEquals(WalkStatus.WALKING, walker.tick());
		// Falling through the final point's level.
		body.onGround = false;
		body.at(0.5, 0.5, 2.5);
		assertEquals(WalkStatus.WALKING, walker.tick(), "arrived in the air");
		body.onGround = true;
		body.at(0.5, 0, 2.5);
		assertEquals(WalkStatus.ARRIVED, walker.tick());
	}

	@Test
	void aPassedPointIsSkipped() {
		var walker = walker();
		walker.follow(path(0, 0, 0, 0, 0, 1, 0, 0, 2, 1, 0, 3));
		walker.tick();
		assertEquals(walking(0f, false), movement(body.applied()));
		// From before point 1 straight into point 2's block in one tick.
		body.at(0.5, 0, 2.5);
		assertEquals(WalkStatus.WALKING, walker.tick());
		assertEquals(walking(-45f, false), movement(body.applied()), "not heading for point 3");
	}

	@Test
	void aSearchThatNeverLandsIsStuck() {
		var walker = walker(5);
		walker.walkTo(new Location(world, 0.5, 0, 6.5), new ManualFinder(), PathRule.ANY);
		for (int i = 0; i < 4; i++) {
			assertEquals(WalkStatus.WALKING, walker.tick(), "stuck early at tick " + i);
			assertEquals(NEUTRAL, body.applied(), "not released keys without a look at tick " + i);
		}
		assertEquals(WalkStatus.STUCK, walker.tick());
		assertEquals(NEUTRAL, body.applied());
		assertEquals(WalkStatus.STUCK, walker.tick());
		assertEquals(List.of(), body.applied(), "a stuck walker wrote again");
	}

	@Test
	void aPathSwapKeepsTheStuckTimerRunning() {
		var walker = walker(5);
		var finder = new ManualFinder();
		walker.walkTo(new Location(world, 0.5, 0, 2.5), finder, PathRule.ANY);
		finder.searches.getLast().complete(path(0, 0, 0, 0, 0, 1, 0, 0, 2));
		for (int i = 0; i < 3; i++) {
			assertEquals(WalkStatus.WALKING, walker.tick());
		}
		walker.walkTo(new Location(world, 2.5, 0, 2.5), finder, PathRule.ANY);
		assertEquals(2, finder.searches.size());
		var second = path(0, 0, 0, 1, 0, 1, 2, 0, 2);
		finder.searches.getLast().complete(second);
		assertEquals(WalkStatus.WALKING, walker.tick());
		assertSame(second, walker.getPath());
		assertEquals(WalkStatus.STUCK, walker.tick(), "the swap restarted the stuck timer");
	}

	@Test
	void aBodyInAnotherWorldIsStuck() {
		var walker = walker();
		walker.follow(path(0, 0, 0, 0, 0, 3));
		assertEquals(WalkStatus.WALKING, walker.tick());
		body.applied();
		body.world = world("other");
		assertEquals(WalkStatus.STUCK, walker.tick());
		assertEquals(NEUTRAL, body.applied());
	}

	@Test
	void noProgressForTheStuckDelayIsStuck() {
		var walker = walker(5);
		walker.follow(path(0, 0, 0, 0, 0, 1, 0, 0, 2));
		for (int i = 0; i < 4; i++) {
			assertEquals(WalkStatus.WALKING, walker.tick(), "stuck early at tick " + i);
			assertEquals(walking(0f, false), movement(body.applied()));
		}
		assertEquals(WalkStatus.STUCK, walker.tick());
		assertEquals(NEUTRAL, body.applied(), "neutral movement not written once");
		assertEquals(WalkStatus.STUCK, walker.tick());
		assertEquals(List.of(), body.applied(), "a stuck walker wrote again");
	}

	@Test
	void progressResetsTheStuckDelay() {
		var walker = walker(5);
		walker.follow(path(0, 0, 0, 0, 0, 5));
		// 0.11 closer every 3 ticks: never 5 ticks without a 0.1 gain.
		for (int i = 0; i < 30; i++) {
			if (i % 3 == 2) {
				body.at(0.5, 0, body.z + 0.11);
			}
			assertEquals(WalkStatus.WALKING, walker.tick(), "stuck at tick " + i);
		}
		// Gains under 0.1 do not count.
		for (int i = 0; i < 5; i++) {
			body.at(0.5, 0, body.z + 0.01);
			walker.tick();
		}
		assertEquals(WalkStatus.STUCK, walker.getStatus());
	}

	@Test
	void theEndOfAnUnreachablePathIsStuck() {
		var walker = walker();
		walker.follow(path(false, 0, 0, 0, 0, 0, 1, 0, 0, 2));
		assertEquals(WalkStatus.WALKING, walker.tick());
		body.at(0.5, 0, 1.5);
		assertEquals(WalkStatus.WALKING, walker.tick());
		body.applied();
		body.at(0.5, 0, 2.5);
		assertEquals(WalkStatus.STUCK, walker.tick());
		assertEquals(NEUTRAL, body.applied());
	}

	@Test
	void aSinglePointPathUnderTheBodyArrivesAtOnce() {
		var walker = walker();
		walker.follow(path(0, 0, 0));
		assertEquals(WalkStatus.ARRIVED, walker.tick());
		assertEquals(NEUTRAL, body.applied());
	}

	@Test
	void stopWritesNeutralOnceAndIdles() {
		var walker = walker();
		walker.follow(path(0, 0, 0, 0, 0, 3));
		walker.tick();
		body.applied();
		walker.stop();
		assertEquals(WalkStatus.IDLE, walker.getStatus());
		assertNull(walker.getPath());
		assertEquals(NEUTRAL, body.applied());
		assertEquals(WalkStatus.IDLE, walker.tick());
		walker.stop();
		assertEquals(List.of(), body.applied(), "an idle walker wrote");
	}

	@Test
	void theCursorStartsAfterThePointNearestTheBody() {
		var walker = walker();
		// Nearest the third point: heading for the fourth, diagonally.
		body.at(0.5, 0, 2.4);
		walker.follow(path(0, 0, 0, 0, 0, 1, 0, 0, 2, 1, 0, 3));
		walker.tick();
		var movement = movement(body.applied());
		float yaw = Float.parseFloat(movement.getFirst().substring("heading ".length()));
		assertTrue(yaw < 0, "not heading for the fourth point: " + movement);
	}

	@Test
	void aClosedDoorInTheNextPointIsRightClickedEveryTenTicks() {
		var walker = walker(100);
		closedDoors.add("0 1 2");
		walker.follow(path(0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 3));
		// Walking to the second point: the door is not in it.
		walker.tick();
		assertTrue(body.applied().stream().noneMatch(c -> c.startsWith("useItemOn")), "clicked too early");
		body.at(0.5, 0, 1.5);
		walker.tick();
		var applied = body.applied();
		assertEquals(List.of("useItemOn door(0 1 2) NORTH HAND"),
				applied.stream().filter(c -> c.startsWith("useItemOn")).toList(), "head half not clicked");
		assertEquals(walking(0f, false), movement(applied), "movement stopped for the door");
		for (int i = 1; i < 10; i++) {
			walker.tick();
			assertTrue(body.applied().stream().noneMatch(c -> c.startsWith("useItemOn")), "clicked again at " + i);
		}
		walker.tick();
		assertTrue(body.applied().contains("useItemOn door(0 1 2) NORTH HAND"), "not clicked again after 10 ticks");
		// Open: no more clicks.
		closedDoors.clear();
		for (int i = 0; i < 12; i++) {
			walker.tick();
			assertTrue(body.applied().stream().noneMatch(c -> c.startsWith("useItemOn")), "clicked an open door");
		}
	}

	@Test
	void aBrainAimWinsWhileTheWalkerMovementStillApplies() {
		var walker = walker();
		walker.follow(path(0, 0, 0, 0, 0, 3));
		body.controls(ControlPriority.BRAIN).setLook(90f, 10f);
		walker.tick();
		var applied = body.applied();
		assertEquals(walking(0f, false), movement(applied), "the walker's movement was dropped");
		assertEquals(List.of("look 90.0 10.0"), applied.stream().filter(c -> c.startsWith("look")).toList());
	}

	@Test
	void aMovedGoalSwapsPathsAtTheNextTickWithoutANeutralTick() {
		var walker = walker();
		var finder = new ManualFinder();
		walker.walkTo(new Location(world, 0.5, 0, 6.5), finder, PathRule.ANY);
		assertEquals(1, finder.searches.size());
		assertNull(walker.getPath());
		assertEquals(WalkStatus.WALKING, walker.tick(), "not walking while the first search runs");
		body.applied();
		var first = path(0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 3, 0, 0, 4, 0, 0, 5, 0, 0, 6);
		finder.searches.getFirst().complete(first);
		assertNull(walker.getPath(), "the path was adopted in the callback");
		assertEquals(WalkStatus.WALKING, walker.tick());
		assertSame(first, walker.getPath());
		assertEquals(walking(0f, false), movement(body.applied()));
		body.at(0.5, 0, 1.5);
		walker.tick();
		body.at(0.5, 0, 2.5);
		walker.tick();
		body.applied();

		// Less than a block: no new search.
		walker.walkTo(new Location(world, 0.9, 0, 6.9), finder, PathRule.ANY);
		assertEquals(1, finder.searches.size(), "searched for a goal moved under 1 block");
		// A block or more: a new search, while the old path is still followed.
		walker.walkTo(new Location(world, 4.5, 0, 2.5), finder, PathRule.ANY);
		assertEquals(2, finder.searches.size());
		assertEquals(4.5, finder.goals.getLast().getX());
		// In flight: moving the goal again starts no search.
		walker.walkTo(new Location(world, 6.5, 0, 2.5), finder, PathRule.ANY);
		assertEquals(2, finder.searches.size(), "searched while a search was in flight");
		assertEquals(WalkStatus.WALKING, walker.tick());
		assertSame(first, walker.getPath());
		assertEquals(walking(0f, false), movement(body.applied()), "the old path was not followed meanwhile");

		var second = path(0, 0, 0, 0, 0, 1, 0, 0, 2, 1, 0, 2, 2, 0, 2, 3, 0, 2, 4, 0, 2);
		finder.searches.getLast().complete(second);
		assertSame(first, walker.getPath());
		assertEquals(WalkStatus.WALKING, walker.tick());
		assertSame(second, walker.getPath());
		// Resumed after the point nearest the body (0 0 2): heading for 1 0 2 (-X
		// yaw is 90, +X is -90).
		assertEquals(walking(-90f, false), movement(body.applied()), "no seamless swap");
		// The goal moved again while searching: a search for it starts now.
		assertEquals(3, finder.searches.size(), "the goal moved during the search was not searched");
		assertEquals(6.5, finder.goals.getLast().getX());
		// The end of the second path is not an arrival: a newer goal is searched.
		for (double x = 1.5; x <= 4.5; x++) {
			body.at(x, 0, 2.5);
			assertEquals(WalkStatus.WALKING, walker.tick(), "arrived at a stale goal");
		}
		finder.searches.getLast().complete(path(4, 0, 2, 5, 0, 2, 6, 0, 2));
		walker.tick();
		body.at(6.5, 0, 2.5);
		assertEquals(WalkStatus.ARRIVED, walker.tick());
	}

	@Test
	void aFailedFirstSearchIsStuckAndALaterOneKeepsThePath() {
		var walker = walker();
		var finder = new ManualFinder();
		walker.walkTo(new Location(world, 0.5, 0, 6.5), finder, PathRule.ANY);
		finder.searches.getFirst().completeExceptionally(new IllegalStateException("rule bug"));
		assertEquals(WalkStatus.STUCK, walker.tick());
		assertEquals(NEUTRAL, body.applied());

		walker.walkTo(new Location(world, 0.5, 0, 6.5), finder, PathRule.ANY);
		var first = path(0, 0, 0, 0, 0, 1, 0, 0, 2);
		finder.searches.getLast().complete(first);
		walker.tick();
		walker.walkTo(new Location(world, 5.5, 0, 6.5), finder, PathRule.ANY);
		finder.searches.getLast().completeExceptionally(new IllegalStateException("rule bug"));
		assertEquals(WalkStatus.WALKING, walker.tick());
		assertSame(first, walker.getPath());
	}

	@Test
	void followDropsAPendingSearch() {
		var walker = walker();
		var finder = new ManualFinder();
		walker.walkTo(new Location(world, 0.5, 0, 6.5), finder, PathRule.ANY);
		var plain = path(0, 0, 0, 0, 0, 1);
		walker.follow(plain);
		finder.searches.getFirst().complete(path(0, 0, 0, 0, 0, 6));
		walker.tick();
		assertSame(plain, walker.getPath());
	}

	@Test
	void badCallsAreRejected() {
		var walker = walker();
		assertThrows(IllegalArgumentException.class, () -> new PathWalker(null));
		assertThrows(IllegalArgumentException.class, () -> new PathWalker(body, 0));
		assertThrows(IllegalArgumentException.class, () -> walker.follow(null));
		assertThrows(IllegalArgumentException.class, () -> walker.follow(new Path(List.of(), true)));
		var elsewhere = world("other");
		assertThrows(IllegalArgumentException.class,
				() -> walker.follow(new Path(List.of(new Location(elsewhere, 0, 0, 0)), true)));
		assertThrows(IllegalArgumentException.class,
				() -> walker.follow(new Path(List.of(new Location(null, 0, 0, 0)), true)));
		var finder = new ManualFinder();
		var goal = new Location(world, 0, 0, 5);
		assertThrows(IllegalArgumentException.class, () -> walker.walkTo(null, finder, PathRule.ANY));
		assertThrows(IllegalArgumentException.class, () -> walker.walkTo(goal, null, PathRule.ANY));
		assertThrows(IllegalArgumentException.class, () -> walker.walkTo(goal, finder, null));
		assertThrows(IllegalArgumentException.class,
				() -> walker.walkTo(new Location(elsewhere, 0, 0, 5), finder, PathRule.ANY));
		assertEquals(WalkStatus.IDLE, walker.getStatus(), "a rejected call changed the status");
		assertEquals(List.of(), finder.searches);

		onMain = false;
		var path = path(0, 0, 0, 0, 0, 1);
		assertThrows(IllegalStateException.class, () -> walker.follow(path));
		assertThrows(IllegalStateException.class, () -> walker.walkTo(goal, finder, PathRule.ANY));
		assertThrows(IllegalStateException.class, walker::stop);
		assertThrows(IllegalStateException.class, walker::tick);
		onMain = true;
		assertEquals(List.of(), body.applied());
	}

	@Test
	void theSameInputsGiveTheSameWrites() {
		var runs = new ArrayList<List<String>>();
		for (int run = 0; run < 3; run++) {
			body.at(0.5, 0, 0.5);
			body.applied();
			var walker = walker(10);
			walker.follow(path(0, 0, 0, 0, 0, 1, 0, 1, 2, 1, 1, 3, 1, 0, 4));
			var writes = new ArrayList<String>();
			double[][] positions = {{0.5, 0, 0.8}, {0.5, 0, 1.5}, {0.5, 1, 2.5}, {1.5, 1, 3.5}, {1.5, 0, 4.5}};
			for (var p : positions) {
				body.at(p[0], p[1], p[2]);
				writes.add(walker.tick().name());
				writes.addAll(body.applied());
			}
			runs.add(writes);
		}
		assertEquals(runs.getFirst(), runs.get(1));
		assertEquals(runs.getFirst(), runs.get(2));
		assertEquals("ARRIVED", runs.getFirst().get(runs.getFirst().size() - NEUTRAL.size() - 1));
	}
}
