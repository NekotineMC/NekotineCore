package fr.nekotine.botstesthost.it;

import static fr.nekotine.botstesthost.it.BotMovementIT.FLOOR_Y;
import static fr.nekotine.botstesthost.it.BotMovementIT.clear;
import static fr.nekotine.botstesthost.it.BotMovementIT.structure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import fr.nekotine.botstesthost.it.BotMovementIT.Structure;
import fr.nekotine.core.navigation.HintedPathResult;
import fr.nekotine.core.navigation.PathFinder;
import fr.nekotine.core.navigation.PathMove;
import fr.nekotine.core.navigation.PathRule;
import fr.nekotine.core.navigation.astar.AStarPathFinder;
import fr.xxgoldenbluexx.paperjunitserver.PaperServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Door;
import org.junit.jupiter.api.Test;

/**
 * Story 5.2: the A* pathfinder on a real stone structure built high above spawn
 * (walls, a 1-block step, a drop and a door), completing on the main thread,
 * isolated from edits by its snapshot, rejecting bad calls.
 */
class PathFinderIT {

	/** Feet level on the platform. */
	private static final int FEET_Y = FLOOR_Y + 1;

	private record Point(int x, int y, int z) {
		static Point of(Location location) {
			return new Point(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		}
	}

	/** A search and what was seen of it. */
	private record Search(CompletableFuture<PathResult> future, CompletableFuture<Boolean> completedOnMain) {
		PathResult result() throws Exception {
			return future.get(10, TimeUnit.SECONDS);
		}
	}

	private static PathFinder finder() {
		return new AStarPathFinder(Bukkit.getPluginManager().getPlugin("BotsTestHost"));
	}

	/**
	 * In one main-thread call: builds the platform plus {@code build}, finds a path
	 * between two feet positions relative to the centre column, then runs
	 * {@code after}.
	 */
	private static Search find(Structure structure, Consumer<Structure> build, int fx, int fy, int fz, int tx, int ty,
			int tz, PathRule rule, Consumer<Structure> after) throws Exception {
		return PaperServer.onMain(() -> {
			structure.platform();
			build.accept(structure);
			var future = finder().find(at(structure, fx, fy, fz), at(structure, tx, ty, tz), rule);
			var onMain = future.handle((path, error) -> Bukkit.isPrimaryThread());
			after.accept(structure);
			return new Search(future, onMain);
		});
	}

	private static Search find(Structure structure, Consumer<Structure> build, int fx, int fz, int tx, int tz,
			PathRule rule) throws Exception {
		return find(structure, build, fx, FEET_Y, fz, tx, FEET_Y, tz, rule, s -> {
		});
	}

	/** @return a location in the middle of the block, as a body would stand. */
	private static Location at(Structure structure, int dx, int y, int dz) {
		return new Location(structure.world, structure.cx + dx + 0.5, y, structure.cz + dz + 0.5);
	}

	private static Point point(Structure structure, int dx, int y, int dz) {
		return new Point(structure.cx + dx, y, structure.cz + dz);
	}

	private static List<Point> points(PathResult path) {
		return path.getPoints().stream().map(Point::of).toList();
	}

	private static void assertAdjacent(PathResult path) {
		var points = points(path);
		for (int i = 1; i < points.size(); i++) {
			var a = points.get(i - 1);
			var b = points.get(i);
			assertTrue(Math.abs(a.x - b.x) <= 1 && Math.abs(a.z - b.z) <= 1 && (a.x != b.x || a.z != b.z)
					&& b.y - a.y <= 1 && a.y - b.y <= 3, "not adjacent: " + a + " -> " + b);
		}
	}

	/**
	 * A 3-high wall along dz = 0 across the platform, with gaps at the given dx.
	 */
	static Consumer<Structure> wallWithGaps(int... gaps) {
		return structure -> {
			int start = -10;
			for (int gap : gaps) {
				if (gap > start) {
					structure.fill(start, FEET_Y, 0, gap - 1, FEET_Y + 2, 0);
				}
				start = gap + 1;
			}
			if (start <= 10) {
				structure.fill(start, FEET_Y, 0, 10, FEET_Y + 2, 0);
			}
		};
	}

	@Test
	void walksAroundAWallAndCompletesOnTheMainThread() throws Exception {
		var structure = structure();
		try {
			var search = find(structure, wallWithGaps(10), 0, -5, 0, 5, PathRule.ANY);
			var path = search.result();
			assertTrue(search.completedOnMain().get(10, TimeUnit.SECONDS), "the future completed off the main thread");
			assertTrue(path.canReachFinalPoint(), "goal not reached: " + points(path));
			assertEquals(point(structure, 0, FEET_Y, -5), points(path).getFirst());
			assertEquals(point(structure, 0, FEET_Y, 5), Point.of(path.getFinalPoint()));
			assertTrue(points(path).contains(point(structure, 10, FEET_Y, 0)), "not through the gap: " + points(path));
			assertAdjacent(path);
			assertEquals(0, path.getNextPointIndex());
			assertEquals(points(path).getFirst(), Point.of(path.getNextPoint()));
			for (var location : path.getPoints()) {
				assertEquals(structure.world, location.getWorld());
				assertEquals(location.getBlockX(), location.getX(), "not a block position: " + location);
				assertEquals(location.getBlockY(), location.getY(), "not a block position: " + location);
				assertEquals(location.getBlockZ(), location.getZ(), "not a block position: " + location);
			}
			// Immutable: changing a returned point changes nothing.
			path.getPoints().getFirst().add(100, 100, 100);
			path.getFinalPoint().add(100, 100, 100);
			assertEquals(point(structure, 0, FEET_Y, -5), points(path).getFirst());
			assertEquals(point(structure, 0, FEET_Y, 5), Point.of(path.getFinalPoint()));
			assertThrows(UnsupportedOperationException.class, () -> path.getPoints().clear());
		} finally {
			clear(structure);
		}
	}

	/**
	 * A sealed 1-wide corridor along +Z: a step up at dz -5, another at -2, a
	 * 2-block drop at 1, a closed oak door at 4.
	 */
	static void course(Structure structure) {
		structure.fill(-1, FEET_Y, -10, -1, FEET_Y + 5, 10).fill(1, FEET_Y, -10, 1, FEET_Y + 5, 10)
				.fill(0, FEET_Y, -10, 0, FEET_Y + 5, -10).fill(0, FEET_Y, 10, 0, FEET_Y + 5, 10)
				.fill(0, FEET_Y, -5, 0, FEET_Y, 0).fill(0, FEET_Y + 1, -2, 0, FEET_Y + 1, 0);
		var lower = structure.world.getBlockAt(structure.cx, FEET_Y, structure.cz + 4);
		var upper = lower.getRelative(0, 1, 0);
		var door = (Door) Material.OAK_DOOR.createBlockData();
		door.setOpen(false);
		door.setHalf(Bisected.Half.BOTTOM);
		lower.setBlockData(door, false);
		door.setHalf(Bisected.Half.TOP);
		upper.setBlockData(door, false);
		structure.placed.add(lower);
		structure.placed.add(upper);
	}

	@Test
	void stepDropAndDoorAreReachedAndHinted() throws Exception {
		var structure = structure();
		try {
			var path = find(structure, PathFinderIT::course, 0, -9, 0, 8, PathRule.ANY).result();
			assertTrue(path.canReachFinalPoint(), "goal not reached: " + points(path));
			assertAdjacent(path);
			var hinted = assertInstanceOf(HintedPathResult.class, path);
			var points = points(path);
			assertEquals(PathMove.START, hinted.getMove(0));
			assertEquals(PathMove.STEP_UP, hinted.getMove(points.indexOf(point(structure, 0, FEET_Y + 1, -5))));
			assertEquals(PathMove.STEP_UP, hinted.getMove(points.indexOf(point(structure, 0, FEET_Y + 2, -2))));
			assertEquals(PathMove.DROP, hinted.getMove(points.indexOf(point(structure, 0, FEET_Y, 1))));
			assertEquals(PathMove.DOOR, hinted.getMove(points.indexOf(point(structure, 0, FEET_Y, 4))));
			var moves = new ArrayList<PathMove>();
			for (int i = 0; i < points.size(); i++) {
				moves.add(hinted.getMove(i));
			}
			assertEquals(1, moves.stream().filter(m -> m == PathMove.DOOR).count(), "moves: " + moves);
			assertEquals(1, moves.stream().filter(m -> m == PathMove.DROP).count(), "moves: " + moves);
			assertEquals(2, moves.stream().filter(m -> m == PathMove.STEP_UP).count(), "moves: " + moves);
			assertThrows(IndexOutOfBoundsException.class, () -> hinted.getMove(points.size()));
		} finally {
			clear(structure);
		}
	}

	@Test
	void ruleKeepsThePathOutOfARegionAndRunsOffThreadOnTheSnapshot() throws Exception {
		var structure = structure();
		var onMain = new AtomicBoolean();
		Queue<Object> views = new ConcurrentLinkedQueue<>();
		Queue<Material> floors = new ConcurrentLinkedQueue<>();
		try {
			int cx = structure.cx;
			int cz = structure.cz;
			PathRule rule = (view, x, y, z) -> {
				onMain.compareAndSet(false, Bukkit.isPrimaryThread());
				views.add(view);
				var floor = view.getBlockData(x, y - 1, z);
				if (floor != null) {
					floors.add(floor.getMaterial());
				}
				return !(x >= cx + 7 && x <= cx + 9 && z >= cz - 1 && z <= cz + 1);
			};
			var path = find(structure, wallWithGaps(-8, 8), 6, -5, 6, 5, rule).result();
			assertTrue(path.canReachFinalPoint(), "goal not reached: " + points(path));
			assertAdjacent(path);
			for (var point : points(path)) {
				assertFalse(point.x >= cx + 7 && point.x <= cx + 9 && point.z >= cz - 1 && point.z <= cz + 1,
						"point in the forbidden region: " + point);
			}
			assertTrue(points(path).contains(point(structure, -8, FEET_Y, 0)), "not through the far gap");
			assertFalse(onMain.get(), "the rule was called on the main thread");
			assertFalse(views.isEmpty(), "the rule was never called");
			for (var view : views) {
				assertFalse(view instanceof World, "the rule got a live world");
			}
			assertTrue(floors.contains(Material.STONE), "the rule's view does not show the platform");
		} finally {
			clear(structure);
		}
	}

	@Test
	void enclosedGoalGivesTheClosestReachedPoint() throws Exception {
		var structure = structure();
		try {
			Consumer<Structure> enclosure = s -> s.fill(-1, FEET_Y, 4, 1, FEET_Y + 2, 4)
					.fill(-1, FEET_Y, 6, 1, FEET_Y + 2, 6).fill(-1, FEET_Y, 5, -1, FEET_Y + 2, 5)
					.fill(1, FEET_Y, 5, 1, FEET_Y + 2, 5);
			var path = find(structure, enclosure, 0, -5, 0, 5, PathRule.ANY).result();
			assertFalse(path.canReachFinalPoint(), "an enclosed goal was reached");
			assertEquals(point(structure, 0, FEET_Y, 3), Point.of(path.getFinalPoint()));
			assertAdjacent(path);
		} finally {
			clear(structure);
		}
	}

	@Test
	void editsAfterFindAreNotSeen() throws Exception {
		var structure = structure();
		try {
			var search = find(structure, s -> {
			}, 0, FEET_Y, -5, 0, FEET_Y, 5, PathRule.ANY, s -> s.fill(-10, FEET_Y, 0, 10, FEET_Y + 2, 0));
			var path = search.result();
			assertTrue(path.canReachFinalPoint(), "the wall placed after find was seen");
			assertTrue(points(path).contains(point(structure, 0, FEET_Y, 0)), "not straight: " + points(path));
			assertTrue(PaperServer.onMain(
					() -> structure.world.getBlockAt(structure.cx, FEET_Y, structure.cz).getType() == Material.STONE),
					"the wall was not placed");
		} finally {
			clear(structure);
		}
	}

	@Test
	void badCallsAreRejected() throws Exception {
		var structure = structure();
		var from = at(structure, 0, FEET_Y, 0);
		var to = at(structure, 0, FEET_Y, 3);
		assertFalse(Bukkit.isPrimaryThread());
		var finder = PaperServer.onMain(PathFinderIT::finder);
		assertThrows(IllegalStateException.class, () -> finder.find(from, to, PathRule.ANY));
		PaperServer.onMain(() -> {
			assertThrows(IllegalArgumentException.class, () -> finder.find(null, to, PathRule.ANY));
			assertThrows(IllegalArgumentException.class, () -> finder.find(from, null, PathRule.ANY));
			assertThrows(IllegalArgumentException.class, () -> finder.find(from, to, null));
			var noWorld = new Location(null, 0, FEET_Y, 0);
			assertThrows(IllegalArgumentException.class, () -> finder.find(noWorld, to, PathRule.ANY));
			var other = Bukkit.getWorlds().stream().filter(w -> !w.equals(structure.world)).findFirst();
			assertTrue(other.isPresent(), "the test server has a single world: " + Bukkit.getWorlds());
			var elsewhere = new Location(other.get(), 0, 64, 0);
			assertThrows(IllegalArgumentException.class, () -> finder.find(from, elsewhere, PathRule.ANY));
			assertNotNull(finder.find(from, to, PathRule.ANY), "a good call failed");
			return null;
		});
	}

	/**
	 * Whether a path crosses a 3-high wall along dz = 0 through its one gap at dx
	 * 0, whose feet block is {@code feet} (and, for a door, its head block the
	 * upper half), under a stone ceiling at head + 1, over {@code floor} (stone if
	 * null).
	 */
	private static boolean crossesGap(Supplier<BlockData> feet, Supplier<BlockData> head, Supplier<BlockData> floor)
			throws Exception {
		var structure = structure();
		try {
			Consumer<Structure> gap = s -> {
				s.fill(-10, FEET_Y, 0, 10, FEET_Y + 2, 0);
				var feetBlock = s.world.getBlockAt(s.cx, FEET_Y, s.cz);
				feetBlock.setBlockData(feet.get(), false);
				feetBlock.getRelative(0, 1, 0).setBlockData(head.get(), false);
				s.placed.add(feetBlock.getRelative(0, 1, 0));
				feetBlock.getRelative(0, -1, 0).setBlockData(floor.get(), false);
			};
			return find(structure, gap, 0, -5, 0, 5, PathRule.ANY).result().canReachFinalPoint();
		} finally {
			clear(structure);
		}
	}

	private static boolean crossesGap(Supplier<BlockData> feet) throws Exception {
		return crossesGap(feet, Material.AIR::createBlockData, Material.STONE::createBlockData);
	}

	@Test
	void blocksAreClassifiedForAPlayerSizedBody() throws Exception {
		assertTrue(crossesGap(Material.AIR::createBlockData), "air is not passable");
		assertTrue(crossesGap(Material.WHITE_CARPET::createBlockData), "a carpet is not passable");
		assertTrue(crossesGap(() -> door(Bisected.Half.BOTTOM), () -> door(Bisected.Half.TOP),
				Material.STONE::createBlockData), "an oak door is not passable");
		assertFalse(crossesGap(Material.STONE::createBlockData), "stone is passable");
		assertFalse(crossesGap(Material.OAK_FENCE::createBlockData), "an oak fence is passable");
		assertFalse(crossesGap(Material.COBBLESTONE_WALL::createBlockData), "a cobblestone wall is passable");
		assertFalse(crossesGap(Material.OAK_FENCE_GATE::createBlockData), "an oak fence gate is passable");
		assertFalse(crossesGap(Material.WATER::createBlockData), "water is passable");
		assertFalse(crossesGap(() -> Material.OAK_SLAB.createBlockData(d -> ((Waterlogged) d).setWaterlogged(true))),
				"a waterlogged slab is passable");
		assertFalse(crossesGap(Material.FIRE::createBlockData), "fire is passable");
		assertFalse(crossesGap(Material.MAGMA_BLOCK::createBlockData), "magma is passable");
		// As floors: stone carries the body, magma does not.
		assertTrue(crossesGap(Material.AIR::createBlockData, Material.AIR::createBlockData,
				Material.STONE::createBlockData), "a stone floor is not a floor");
		assertFalse(crossesGap(Material.AIR::createBlockData, Material.AIR::createBlockData,
				Material.MAGMA_BLOCK::createBlockData), "a magma floor is a floor");
	}

	private static BlockData door(Bisected.Half half) {
		var door = (Door) Material.OAK_DOOR.createBlockData();
		door.setOpen(false);
		door.setHalf(half);
		return door;
	}

	@Test
	void aFailingRuleFailsTheFutureOnTheMainThread() throws Exception {
		var structure = structure();
		var boom = new IllegalStateException("rule bug");
		try {
			var search = find(structure, s -> {
			}, 0, -5, 0, 5, (view, x, y, z) -> {
				throw boom;
			});
			var error = assertThrows(ExecutionException.class, () -> search.future().get(10, TimeUnit.SECONDS));
			assertSame(boom, error.getCause());
			assertTrue(search.completedOnMain().get(10, TimeUnit.SECONDS), "the future failed off the main thread");
		} finally {
			clear(structure);
		}
	}

	@Test
	void unloadedChunksAreImpassableAndStayUnloaded() throws Exception {
		var structure = structure();
		int far = 4000;
		int chunkX = (structure.cx + far) >> 4;
		int chunkZ = structure.cz >> 4;
		try {
			assertFalse(PaperServer.onMain(() -> structure.world.isChunkLoaded(chunkX, chunkZ)),
					"the far chunk is already loaded");
			var path = find(structure, s -> {
			}, 0, 0, far, 0, PathRule.ANY).result();
			assertFalse(path.canReachFinalPoint(), "a goal in an unloaded chunk was reached");
			assertFalse(PaperServer.onMain(() -> structure.world.isChunkLoaded(chunkX, chunkZ)),
					"the search loaded the far chunk");
		} finally {
			clear(structure);
		}
	}
}
