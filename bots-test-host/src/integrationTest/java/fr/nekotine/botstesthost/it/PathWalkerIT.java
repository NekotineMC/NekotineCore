package fr.nekotine.botstesthost.it;

import static fr.nekotine.botstesthost.it.BotLifecycleIT.cleanUp;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.register;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.roster;
import static fr.nekotine.botstesthost.it.BotMovementIT.FLOOR_Y;
import static fr.nekotine.botstesthost.it.BotMovementIT.clear;
import static fr.nekotine.botstesthost.it.BotMovementIT.structure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import fr.nekotine.botstesthost.it.BotMovementIT.MoveEvents;
import fr.nekotine.botstesthost.it.BotMovementIT.Structure;
import fr.nekotine.core.bot.Bot;
import fr.nekotine.core.bot.BotRoster;
import fr.nekotine.core.bot.BotTickEvent;
import fr.nekotine.core.navigation.HintedPathResult;
import fr.nekotine.core.navigation.PathFinder;
import fr.nekotine.core.navigation.PathRule;
import fr.nekotine.core.navigation.PathWalker;
import fr.nekotine.core.navigation.WalkStatus;
import fr.nekotine.core.navigation.astar.AStarPathFinder;
import fr.xxgoldenbluexx.paperjunitserver.PaperServer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;

/**
 * Story 5.3: the walker drives a real bot along A* and vanilla paths over
 * steps, a drop and a door, reports stuck and arrival, follows a moving goal,
 * and walks the same course the same way every time.
 */
class PathWalkerIT {

	private static final String NAME = "Bot_Walker";

	/** Feet level on the platform. */
	private static final int FEET_Y = FLOOR_Y + 1;

	/** Longest walk a test waits for, in ticks. */
	private static final int TIMEOUT_TICKS = 600;

	private record Point(int x, int y, int z) {
		static Point of(Location location) {
			return new Point(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		}
	}

	/** A plain {@link PathResult}: the same points, no hints. */
	private record Plain(List<Location> points, boolean reaches) implements PathResult {

		static Plain of(PathResult path) {
			return new Plain(path.getPoints(), path.canReachFinalPoint());
		}

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

	/**
	 * Ticks a walker from one bot's tick event, recording, per tick, what the bot
	 * had before the walker ran and the walker's status after, until the first
	 * {@link WalkStatus#ARRIVED} or {@link WalkStatus#STUCK}. Main thread only.
	 */
	public static final class Walk implements Listener {
		final Bot bot;
		final PathWalker walker;
		final List<WalkStatus> statuses = new ArrayList<>();
		final List<Location> locations = new ArrayList<>();
		final List<Boolean> forwardHeld = new ArrayList<>();
		final List<Boolean> hadPath = new ArrayList<>();
		final List<Point> feet = new ArrayList<>();
		volatile Consumer<Walk> before = w -> {
		};

		Walk(Bot bot, PathWalker walker) {
			this.bot = bot;
			this.walker = walker;
		}

		@EventHandler
		public void onTick(BotTickEvent event) {
			if (event.getBot() != bot || done()) {
				return;
			}
			before.accept(this);
			var location = bot.player().getLocation();
			locations.add(location);
			forwardHeld.add(bot.isForward());
			var point = Point.of(location);
			if (feet.isEmpty() || !feet.getLast().equals(point)) {
				feet.add(point);
			}
			statuses.add(walker.tick());
			hadPath.add(walker.getPath() != null);
		}

		boolean done() {
			return !statuses.isEmpty()
					&& (statuses.getLast() == WalkStatus.ARRIVED || statuses.getLast() == WalkStatus.STUCK);
		}

		/** @return the statuses without consecutive repeats */
		List<WalkStatus> phases() {
			var phases = new ArrayList<WalkStatus>();
			for (var status : statuses) {
				if (phases.isEmpty() || phases.getLast() != status) {
					phases.add(status);
				}
			}
			return phases;
		}
	}

	private static PathFinder finder() {
		return new AStarPathFinder(Bukkit.getPluginManager().getPlugin("BotsTestHost"));
	}

	/**
	 * @return a location in the middle of the block, relative to the centre column.
	 */
	private static Location at(Structure structure, int dx, int y, int dz) {
		return new Location(structure.world, structure.cx + dx + 0.5, y, structure.cz + dz + 0.5, 0, 0);
	}

	/** Spawns the bot at a feet position and lets it settle. */
	private static Bot spawn(BotRoster roster, Structure structure, int dx, int dz) throws Exception {
		var bot = PaperServer.onMain(() -> roster.spawn(NAME, at(structure, dx, FEET_Y, dz)));
		PaperServer.awaitTicks(10);
		return bot;
	}

	private static PathResult find(Bot bot, Location to) throws Exception {
		return PaperServer.onMain(() -> finder().find(bot.body().getLocation(), to, PathRule.ANY)).get(10,
				TimeUnit.SECONDS);
	}

	/**
	 * Builds a walker on the bot, gives it its first order and starts ticking it,
	 * running {@code before} at each tick before the walker.
	 */
	private static Walk start(Bot bot, Consumer<PathWalker> order, Consumer<Walk> before) throws Exception {
		return PaperServer.onMain(() -> {
			var walk = new Walk(bot, new PathWalker(bot.body()));
			walk.before = before;
			order.accept(walk.walker);
			Bukkit.getPluginManager().registerEvents(walk, PaperServer.plugin());
			return walk;
		});
	}

	private static Walk start(Bot bot, Consumer<PathWalker> order) throws Exception {
		return start(bot, order, w -> {
		});
	}

	private static void awaitDone(Walk walk) throws Exception {
		PaperServer.awaitUntil(walk::done, TIMEOUT_TICKS);
		PaperServer.onMain(() -> {
			HandlerList.unregisterAll(walk);
			return null;
		});
	}

	private static double horizontalDistance(Location location, Location target) {
		return Math.hypot(location.getX() - target.getX(), location.getZ() - target.getZ());
	}

	private record Run(List<Point> feet, List<WalkStatus> phases) {
	}

	/**
	 * Walks the 5.2 course (two steps, a 2-block drop, a closed oak door) from dz
	 * -9 to 8 along an A* path, plain or hinted.
	 */
	private static Run walkCourse(boolean plain) throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		Walk walk = null;
		try {
			PaperServer.onMain(() -> {
				structure.platform();
				PathFinderIT.course(structure);
				return null;
			});
			var bot = spawn(roster, structure, 0, -9);
			register(events);
			var goal = at(structure, 0, FEET_Y, 8);
			var path = find(bot, goal);
			assertTrue(path.canReachFinalPoint(), "the course has no path");
			assertTrue(path instanceof HintedPathResult, "the A* path has no hints");
			PathResult followed = plain ? Plain.of(path) : path;
			assertEquals(plain, !(followed instanceof HintedPathResult));
			walk = start(bot, w -> w.follow(followed));
			awaitDone(walk);
			var w = walk;
			var run = PaperServer.onMain(() -> new Run(List.copyOf(w.feet), w.phases()));
			assertEquals(WalkStatus.ARRIVED, run.phases().getLast(), "not arrived: " + run);
			assertTrue(PaperServer.onMain(() -> horizontalDistance(bot.player().getLocation(), goal)) <= 0.5,
					"arrived away from the goal");
			assertTrue(PaperServer.onMain(() -> structure.world.getBlockAt(structure.cx, FEET_Y, structure.cz + 4)
					.getBlockData() instanceof Door door && door.isOpen()), "the door is not open");
			assertTrue(run.feet().contains(new Point(structure.cx, FEET_Y + 2, structure.cz - 1)),
					"not over both steps: " + run.feet());
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
			return run;
		} finally {
			cleanUp(roster, events, walk);
			clear(structure);
		}
	}

	@Test
	void theCourseIsWalkedTheSameWayThreeTimes() throws Exception {
		var first = walkCourse(false);
		assertEquals(List.of(WalkStatus.WALKING, WalkStatus.ARRIVED), first.phases());
		assertEquals(first, walkCourse(false), "the second walk differs");
		assertEquals(first, walkCourse(false), "the third walk differs");
	}

	@Test
	void aPlainPathIsWalkedWithoutHints() throws Exception {
		assertEquals(List.of(WalkStatus.WALKING, WalkStatus.ARRIVED), walkCourse(true).phases());
	}

	@Test
	void aVanillaZombiePathIsWalked() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		Walk walk = null;
		try {
			var goal = at(structure, 0, FEET_Y + 1, 4);
			PaperServer.onMain(() -> {
				structure.platform();
				PathFinderIT.wallWithGaps(4).accept(structure);
				// A 1-block step up to the goal's side.
				return structure.fill(-10, FEET_Y, 2, 10, FEET_Y, 10);
			});
			// The bot first, so a player keeps the zombie's chunk ticking.
			var bot = spawn(roster, structure, 0, -3);
			var zombie = PaperServer
					.onMain(() -> structure.world.spawn(at(structure, 0, FEET_Y, -4), Zombie.class, z -> {
						z.setAdult();
						// No goals, so it does not go for the bot; physics still apply.
						z.setAware(false);
						z.setShouldBurnInDay(false);
						z.setSilent(true);
					}));
			// A mob plans paths only from the ground.
			PaperServer.awaitUntil(zombie::isOnGround, 40);
			var path = PaperServer.onMain(() -> {
				var found = zombie.getPathfinder().findPath(goal);
				zombie.remove();
				return found;
			});
			assertNotNull(path, "the zombie found no path");
			assertTrue(path.canReachFinalPoint(),
					"the zombie's path does not reach the goal: " + path.getPoints().stream().map(Point::of).toList());
			assertFalse(path instanceof HintedPathResult);
			register(events);
			walk = start(bot, w -> w.follow(path));
			awaitDone(walk);
			var w = walk;
			assertEquals(List.of(WalkStatus.WALKING, WalkStatus.ARRIVED), PaperServer.onMain(w::phases));
			var end = PaperServer.onMain(() -> bot.player().getLocation());
			assertTrue(horizontalDistance(end, goal) <= 0.5, "arrived away from the goal: " + end);
			assertEquals(FEET_Y + 1, end.getBlockY(), "not up the step: " + end);
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
		} finally {
			cleanUp(roster, events, walk);
			clear(structure);
		}
	}

	@Test
	void aWallPlacedAfterPlanningIsStuckAndTheBotStops() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		Walk walk = null;
		try {
			PaperServer.onMain(() -> structure.platform());
			var bot = spawn(roster, structure, 0, -5);
			register(events);
			var path = find(bot, at(structure, 0, FEET_Y, 5));
			assertTrue(path.canReachFinalPoint());
			walk = start(bot, w -> {
				structure.fill(-10, FEET_Y, 0, 10, FEET_Y + 2, 0);
				w.follow(path);
			});
			awaitDone(walk);
			var w = walk;
			assertEquals(List.of(WalkStatus.WALKING, WalkStatus.STUCK), PaperServer.onMain(w::phases));
			var locations = PaperServer.onMain(() -> List.copyOf(w.locations));
			// The first tick from which the bot no longer moves.
			var last = locations.getLast();
			int blocked = locations.size() - 1;
			while (blocked > 0 && locations.get(blocked - 1).distance(last) < 0.01) {
				blocked--;
			}
			int stuckAfter = locations.size() - 1 - blocked;
			assertTrue(stuckAfter <= PathWalker.DEFAULT_STUCK_TICKS + 10,
					"stuck " + stuckAfter + " ticks after being blocked");
			assertTrue(last.getZ() < structure.cz, "went through the wall: " + last);
			PaperServer.onMain(() -> {
				assertFalse(bot.isForward(), "forward still held");
				assertFalse(bot.isJump(), "jump still held");
				assertNull(bot.getHeading(), "heading not released");
				return null;
			});
			var stopped = PaperServer.onMain(() -> bot.player().getLocation());
			PaperServer.awaitTicks(20);
			var later = PaperServer.onMain(() -> bot.player().getLocation());
			assertTrue(stopped.distance(later) < 0.01, "the bot moved after stuck: " + stopped + " -> " + later);
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
		} finally {
			cleanUp(roster, events, walk);
			clear(structure);
		}
	}

	@Test
	void theEndOfAnUnreachablePathIsStuck() throws Exception {
		var roster = roster();
		var structure = structure();
		Walk walk = null;
		try {
			PaperServer.onMain(() -> structure.platform().fill(-1, FEET_Y, 4, 1, FEET_Y + 2, 4)
					.fill(-1, FEET_Y, 6, 1, FEET_Y + 2, 6).fill(-1, FEET_Y, 5, -1, FEET_Y + 2, 5)
					.fill(1, FEET_Y, 5, 1, FEET_Y + 2, 5));
			var bot = spawn(roster, structure, 0, -5);
			var path = find(bot, at(structure, 0, FEET_Y, 5));
			assertFalse(path.canReachFinalPoint(), "the enclosed goal was reached");
			walk = start(bot, w -> w.follow(path));
			awaitDone(walk);
			var w = walk;
			assertEquals(List.of(WalkStatus.WALKING, WalkStatus.STUCK), PaperServer.onMain(w::phases));
			var end = PaperServer.onMain(() -> bot.player().getLocation());
			assertTrue(horizontalDistance(end, at(structure, 0, FEET_Y, 3)) <= 0.5,
					"stuck before the path's end: " + end);
		} finally {
			cleanUp(roster, walk);
			clear(structure);
		}
	}

	@Test
	void aMovedGoalIsFollowedWithoutStopping() throws Exception {
		var roster = roster();
		var structure = structure();
		var events = new MoveEvents(NAME);
		Walk walk = null;
		try {
			PaperServer.onMain(() -> structure.platform());
			var bot = spawn(roster, structure, 0, -8);
			register(events);
			var finder = PaperServer.onMain(PathWalkerIT::finder);
			var a = at(structure, -6, FEET_Y, 8);
			var b = at(structure, 8, FEET_Y, 8);
			walk = start(bot, w -> w.walkTo(a, finder, PathRule.ANY), w -> {
				if (w.statuses.size() == 20) {
					w.walker.walkTo(b, finder, PathRule.ANY);
				}
			});
			awaitDone(walk);
			var w = walk;
			var statuses = PaperServer.onMain(() -> List.copyOf(w.statuses));
			assertTrue(statuses.size() > 20, "arrived before the goal moved");
			assertEquals(List.of(WalkStatus.WALKING, WalkStatus.ARRIVED), PaperServer.onMain(w::phases));
			var end = PaperServer.onMain(() -> bot.player().getLocation());
			assertTrue(horizontalDistance(end, b) <= 0.5, "not at the moved goal: " + end);
			assertEquals(Point.of(b), Point.of(PaperServer.onMain(() -> w.walker.getPath().getFinalPoint())));
			var forward = PaperServer.onMain(() -> List.copyOf(w.forwardHeld));
			var hadPath = PaperServer.onMain(() -> List.copyOf(w.hadPath));
			int firstPath = hadPath.indexOf(true);
			assertTrue(firstPath >= 0 && firstPath < 20, "the first path landed late: " + firstPath);
			for (int i = firstPath + 1; i < statuses.size(); i++) {
				assertTrue(forward.get(i), "forward released at tick " + i + " before arrival");
			}
			assertEquals(List.of(), List.copyOf(events.fails), "PlayerFailMoveEvent fired");
		} finally {
			cleanUp(roster, events, walk);
			clear(structure);
		}
	}

	@Test
	void badCallsAreRejected() throws Exception {
		var roster = roster();
		var structure = structure();
		try {
			PaperServer.onMain(() -> structure.platform());
			var bot = spawn(roster, structure, 0, 0);
			var walker = PaperServer.onMain(() -> new PathWalker(bot.body()));
			assertThrows(IllegalStateException.class, walker::tick);
			assertThrows(IllegalStateException.class, walker::stop);
			var here = at(structure, 0, FEET_Y, 0);
			assertThrows(IllegalStateException.class, () -> walker.follow(new Plain(List.of(here), true)));
			PaperServer.onMain(() -> {
				assertThrows(IllegalArgumentException.class, () -> walker.follow(null));
				assertThrows(IllegalArgumentException.class, () -> walker.follow(new Plain(List.of(), true)));
				var other = Bukkit.getWorlds().stream().filter(w -> !w.equals(structure.world)).findFirst();
				assertTrue(other.isPresent(), "the test server has a single world: " + Bukkit.getWorlds());
				var elsewhere = new Location(other.get(), 0, 64, 0);
				assertThrows(IllegalArgumentException.class, () -> walker.follow(new Plain(List.of(elsewhere), true)));
				assertThrows(IllegalArgumentException.class, () -> walker.walkTo(elsewhere, finder(), PathRule.ANY));
				assertEquals(WalkStatus.IDLE, walker.tick());
				walker.follow(new Plain(List.of(here), true));
				assertEquals(WalkStatus.ARRIVED, walker.tick());
				return null;
			});
		} finally {
			cleanUp(roster);
			clear(structure);
		}
	}
}
