package fr.nekotine.core.navigation.astar;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.nekotine.core.navigation.BlockView;
import fr.nekotine.core.navigation.PathMove;
import fr.nekotine.core.navigation.PathRule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Story 5.2: the pure A* search over hand-built cell grids, with no server: the
 * walking model, hints, the caller's rule, unreachable goals, the node budget
 * and determinism.
 */
class AStarSearchTest {

	private static final BlockView NO_BLOCKS = (x, y, z) -> null;

	private static final int BUDGET = 100_000;

	/**
	 * A box of cells, bounds inclusive: stone at y 0, open above, blocked outside
	 * (as outside a snapshot).
	 */
	private static final class Grid implements CellGrid {
		final int maxX;
		final int maxZ;
		final int maxY = 10;
		final Map<Long, Cell> cells = new HashMap<>();

		Grid(int maxX, int maxZ) {
			this.maxX = maxX;
			this.maxZ = maxZ;
		}

		Grid set(Cell cell, int x1, int y1, int z1, int x2, int y2, int z2) {
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					for (int z = z1; z <= z2; z++) {
						cells.put(AStarSearch.key(x, y, z), cell);
					}
				}
			}
			return this;
		}

		Grid solid(int x1, int y1, int z1, int x2, int y2, int z2) {
			return set(Cell.SOLID, x1, y1, z1, x2, y2, z2);
		}

		@Override
		public Cell cell(int x, int y, int z) {
			if (x < 0 || x > maxX || z < 0 || z > maxZ || y < 0 || y > maxY) {
				return Cell.BLOCKED;
			}
			return cells.getOrDefault(AStarSearch.key(x, y, z), y == 0 ? Cell.SOLID : Cell.OPEN);
		}
	}

	private record Point(int x, int y, int z) {
	}

	private static AStarSearch.Result search(CellGrid grid, PathRule rule, Point from, Point to, int budget) {
		return AStarSearch.search(grid, NO_BLOCKS, rule, from.x, from.y, from.z, to.x, to.y, to.z, budget);
	}

	private static AStarSearch.Result search(CellGrid grid, PathRule rule, Point from, Point to) {
		return search(grid, rule, from, to, BUDGET);
	}

	private static List<Point> points(AStarSearch.Result result) {
		var points = new ArrayList<Point>();
		for (int i = 0; i < result.size(); i++) {
			points.add(new Point(result.xs()[i], result.ys()[i], result.zs()[i]));
		}
		return points;
	}

	private static Point last(AStarSearch.Result result) {
		return points(result).getLast();
	}

	private static void assertAdjacent(AStarSearch.Result result) {
		var points = points(result);
		for (int i = 1; i < points.size(); i++) {
			var a = points.get(i - 1);
			var b = points.get(i);
			assertTrue(Math.abs(a.x - b.x) <= 1 && Math.abs(a.z - b.z) <= 1 && (a.x != b.x || a.z != b.z)
					&& b.y - a.y <= 1 && a.y - b.y <= AStarSearch.MAX_DROP, "not adjacent: " + a + " -> " + b);
		}
	}

	/** A wall along z = 5, 3 high, with gaps at the given x. */
	private static Grid wallWithGaps(int... gaps) {
		var grid = new Grid(10, 10).solid(0, 1, 5, 10, 3, 5);
		for (int gap : gaps) {
			grid.set(Cell.OPEN, gap, 1, 5, gap, 3, 5);
		}
		return grid;
	}

	@Test
	void walksAroundAWallThroughItsGap() {
		var result = search(wallWithGaps(10), PathRule.ANY, new Point(2, 1, 2), new Point(2, 1, 8));
		assertTrue(result.reached(), "goal not reached");
		assertEquals(new Point(2, 1, 2), points(result).getFirst());
		assertEquals(new Point(2, 1, 8), last(result));
		assertAdjacent(result);
		assertTrue(points(result).contains(new Point(10, 1, 5)), "path does not go through the gap: " + points(result));
		assertEquals(PathMove.START, result.moves()[0]);
		for (int i = 1; i < result.size(); i++) {
			assertEquals(PathMove.WALK, result.moves()[i], "unexpected move at " + i);
		}
	}

	/**
	 * A 1-wide corridor along z: a step up at z 3, another at z 5, a 2-block drop
	 * at z 7, a wooden door at z 9.
	 */
	static Grid course() {
		return new Grid(0, 12).solid(0, 1, 3, 0, 1, 6).solid(0, 2, 5, 0, 2, 6).set(Cell.DOOR, 0, 1, 9, 0, 2, 9);
	}

	@Test
	void hintsStepDropAndDoor() {
		var result = search(course(), PathRule.ANY, new Point(0, 1, 0), new Point(0, 1, 11));
		assertTrue(result.reached(), "goal not reached: " + points(result));
		assertAdjacent(result);
		var expected = List.of(new Point(0, 1, 0), new Point(0, 1, 1), new Point(0, 1, 2), new Point(0, 2, 3),
				new Point(0, 2, 4), new Point(0, 3, 5), new Point(0, 3, 6), new Point(0, 1, 7), new Point(0, 1, 8),
				new Point(0, 1, 9), new Point(0, 1, 10), new Point(0, 1, 11));
		assertEquals(expected, points(result));
		assertEquals(
				List.of(PathMove.START, PathMove.WALK, PathMove.WALK, PathMove.STEP_UP, PathMove.WALK, PathMove.STEP_UP,
						PathMove.WALK, PathMove.DROP, PathMove.WALK, PathMove.DOOR, PathMove.WALK, PathMove.WALK),
				List.of(result.moves()));
	}

	@Test
	void noStepWithoutHeadroomNorTwoBlockStepNorFourBlockDrop() {
		// Headroom: a ceiling right above the head at the foot of the step.
		var lowCeiling = course().solid(0, 3, 2, 0, 3, 2);
		assertFalse(search(lowCeiling, PathRule.ANY, new Point(0, 1, 0), new Point(0, 1, 11)).reached());
		// A 2-block step.
		var tall = new Grid(0, 6).solid(0, 1, 3, 0, 2, 6);
		assertFalse(search(tall, PathRule.ANY, new Point(0, 1, 0), new Point(0, 3, 5)).reached());
		// A 4-block drop: from a 4-high ledge.
		var ledge = new Grid(0, 6).solid(0, 1, 0, 0, 4, 2);
		assertFalse(search(ledge, PathRule.ANY, new Point(0, 5, 0), new Point(0, 1, 5)).reached());
		// A 3-block drop is fine.
		var lower = new Grid(0, 6).solid(0, 1, 0, 0, 3, 2);
		var result = search(lower, PathRule.ANY, new Point(0, 4, 0), new Point(0, 1, 5));
		assertTrue(result.reached());
		assertEquals(PathMove.DROP, result.moves()[3]);
	}

	@Test
	void noDiagonalPastACorner() {
		// A single pillar at (1, z 1): going from (0,0) to (1,2)... the diagonal
		// (0,1)->(1,2) has (1,1) as a side, so it is refused.
		var grid = new Grid(2, 2).solid(1, 1, 1, 1, 2, 1);
		var result = search(grid, PathRule.ANY, new Point(0, 1, 1), new Point(1, 1, 2));
		assertTrue(result.reached());
		assertEquals(List.of(new Point(0, 1, 1), new Point(0, 1, 2), new Point(1, 1, 2)), points(result));
		var open = search(new Grid(2, 2), PathRule.ANY, new Point(0, 1, 1), new Point(1, 1, 2));
		assertEquals(List.of(new Point(0, 1, 1), new Point(1, 1, 2)), points(open), "the diagonal is not used");
	}

	@Test
	void blockedCellsAreNeitherFloorNorPassable() {
		// A blocked strip across the field (water, an unloaded chunk): no way over.
		var grid = new Grid(10, 10).set(Cell.BLOCKED, 0, 0, 5, 10, 0, 5);
		var result = search(grid, PathRule.ANY, new Point(2, 1, 2), new Point(2, 1, 8));
		assertFalse(result.reached());
		assertEquals(new Point(2, 1, 4), last(result));
	}

	@Test
	void ruleKeepsThePathOutOfAForbiddenRegion() {
		var grid = wallWithGaps(0, 10);
		var from = new Point(7, 1, 2);
		var to = new Point(7, 1, 8);
		var free = search(grid, PathRule.ANY, from, to);
		assertTrue(points(free).contains(new Point(10, 1, 5)), "without the rule, the near gap is not used");
		PathRule rule = (view, x, y, z) -> !(x >= 9 && z >= 4 && z <= 6);
		var result = search(grid, rule, from, to);
		assertTrue(result.reached(), "goal not reached");
		assertAdjacent(result);
		for (var point : points(result)) {
			assertFalse(point.x >= 9 && point.z >= 4 && point.z <= 6, "point in the forbidden region: " + point);
		}
		assertTrue(points(result).contains(new Point(0, 1, 5)), "path does not use the far gap");
	}

	@Test
	void ruleCostDivertsThePathAndIsNeverAskedAboutTheStart() {
		var grid = wallWithGaps(0, 10);
		var from = new Point(7, 1, 2);
		var to = new Point(7, 1, 8);
		var asked = new ArrayList<Point>();
		var rule = new PathRule() {
			@Override
			public boolean allows(BlockView view, int x, int y, int z) {
				asked.add(new Point(x, y, z));
				return true;
			}

			@Override
			public double cost(BlockView view, int x, int y, int z) {
				return x == 10 && z == 5 ? 50 : 0;
			}
		};
		var result = search(grid, rule, from, to);
		assertTrue(result.reached());
		assertTrue(points(result).contains(new Point(0, 1, 5)), "the costly gap is used: " + points(result));
		assertFalse(asked.contains(from), "the rule was asked about the start");
		assertEquals(asked.size(), asked.stream().distinct().count(), "the rule was asked twice about a position");
	}

	@Test
	void badRuleCostFailsTheSearch() {
		var negative = new PathRule() {
			@Override
			public boolean allows(BlockView view, int x, int y, int z) {
				return true;
			}

			@Override
			public double cost(BlockView view, int x, int y, int z) {
				return -1;
			}
		};
		assertThrows(IllegalStateException.class,
				() -> search(new Grid(3, 3), negative, new Point(0, 1, 0), new Point(3, 1, 3)));
	}

	@Test
	void enclosedGoalGivesTheClosestReachedPoint() {
		// Walls 3 high around (5, 1, 8).
		var grid = new Grid(10, 10).solid(4, 1, 7, 6, 3, 9).set(Cell.OPEN, 5, 1, 8, 5, 3, 8);
		var result = search(grid, PathRule.ANY, new Point(5, 1, 0), new Point(5, 1, 8));
		assertFalse(result.reached());
		assertEquals(new Point(5, 1, 6), last(result));
		assertAdjacent(result);
	}

	@Test
	void runningOutOfBudgetCountsAsUnreachable() {
		var grid = new Grid(30, 30);
		var result = search(grid, PathRule.ANY, new Point(0, 1, 0), new Point(30, 1, 30), 5);
		assertFalse(result.reached());
		assertTrue(result.size() >= 2, "no progress toward the goal");
		assertAdjacent(result);
		assertTrue(search(grid, PathRule.ANY, new Point(0, 1, 0), new Point(30, 1, 30)).reached());
		assertThrows(IllegalArgumentException.class,
				() -> search(grid, PathRule.ANY, new Point(0, 1, 0), new Point(1, 1, 1), 0));
	}

	@Test
	void sameInputsGiveTheSamePath() {
		// An open field with pillars: many paths of equal cost.
		var grid = new Grid(20, 20);
		for (int i = 2; i < 20; i += 4) {
			grid.solid(i, 1, i - 1, i, 2, i - 1);
		}
		var first = search(grid, PathRule.ANY, new Point(0, 1, 3), new Point(19, 1, 15));
		for (int run = 0; run < 3; run++) {
			var again = search(grid, PathRule.ANY, new Point(0, 1, 3), new Point(19, 1, 15));
			assertArrayEquals(first.xs(), again.xs());
			assertArrayEquals(first.ys(), again.ys());
			assertArrayEquals(first.zs(), again.zs());
			assertArrayEquals(first.moves(), again.moves());
		}
		assertTrue(first.reached());
	}

	@Test
	void startOnTheGoalAndStartInsideAFloor() {
		var same = search(new Grid(3, 3), PathRule.ANY, new Point(1, 1, 1), new Point(1, 1, 1));
		assertTrue(same.reached());
		assertEquals(List.of(new Point(1, 1, 1)), points(same));
		// Feet block y 0 is the floor itself (standing on a slab): counts from y 1.
		var lifted = search(new Grid(3, 3), PathRule.ANY, new Point(0, 0, 0), new Point(2, 0, 0));
		assertTrue(lifted.reached());
		assertEquals(new Point(0, 1, 0), points(lifted).getFirst());
		assertEquals(new Point(2, 1, 0), last(lifted));
	}
}
