package fr.nekotine.core.navigation;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Drives a {@link Body} along a path, like a vanilla mob follows its own: over
 * 1-block steps, off drops and through wooden doors, reporting a
 * {@link WalkStatus} at each tick.
 *
 * <h2>Tick</h2>
 *
 * <p>
 * The walker has no listener or task of its own: its caller calls
 * {@link #tick()} once per body tick, from the body's tick event (for a bot,
 * {@code BotTickEvent}), so its writes apply that same tick. It writes at
 * {@link ControlPriority#WALKER}, the lowest priority: a melee controller or a
 * brain writing a group the same tick overrides that group only (e.g. a brain's
 * aim wins while the walker's movement still applies).
 *
 * <p>
 * Each tick spent {@link WalkStatus#WALKING} writes the whole movement group
 * (heading toward the next point, forward held, jump on a step, every other key
 * released) and a look toward the next point; to open a door it also writes
 * hands. When it stops ({@link WalkStatus#ARRIVED}, {@link WalkStatus#STUCK} or
 * {@link #stop()}), it writes neutral movement once (every key released,
 * heading {@code null}), then nothing until it gets a new path or goal. The
 * walker never sprints, sneaks, swims, climbs, closes doors, smooths paths,
 * teleports or sets velocity: the body's own physics move it.
 *
 * <h2>Paths</h2>
 *
 * <p>
 * Any {@link PathResult} works, a {@link PathFinder}'s or a vanilla mob's. It
 * is read once and never changed: its points are copied as feet block
 * positions, {@link PathResult#getNextPointIndex()} is ignored and the walker
 * keeps its own cursor, starting at the first point after the one nearest the
 * body. The walker infers each move without hints: a point one block up is a
 * step, jumped when the body is on the ground within {@value #STEP_REACH}
 * blocks of it; a lower point is a drop, walked off with no special input, and
 * reached only once the body landed; a closed wooden door in the next point's
 * feet or head block is right-clicked, at most once every
 * {@value #DOOR_RETRY_TICKS} ticks. A {@link HintedPathResult}'s hints name the
 * same moves, so the walker needs none. Points are aimed at their block centre.
 *
 * <h2>Status</h2>
 *
 * <p>
 * {@link WalkStatus#ARRIVED}: the body stands within {@value #ARRIVAL_RADIUS}
 * blocks horizontally of the final point's block centre, at its level, and the
 * path reaches its goal; at the end of a path whose
 * {@link PathResult#canReachFinalPoint()} is false, the walker reports
 * {@link WalkStatus#STUCK} instead. {@link WalkStatus#STUCK} also when, for the
 * stuck delay, the body reached no new point and got no {@value #PROGRESS_STEP}
 * block closer to the current one.
 *
 * <h2>Goal</h2>
 *
 * <p>
 * {@link #walkTo} walks to a goal through a {@link PathFinder}. Called again
 * while walking, it only moves the goal: a new search starts once the goal is
 * {@value #RESEARCH_DISTANCE} block or more from the last searched goal and no
 * search is in flight. The body keeps following the old path until the new one
 * lands; the walker adopts it at its next tick (never in the search's
 * callback), resuming at the first point after the one nearest the body, with
 * no neutral tick and without restarting its stuck delay. While a newer goal is
 * being searched, the end of the old path is neither an arrival nor stuck. A
 * failed search keeps the old path, or reports {@link WalkStatus#STUCK} when
 * there is none.
 *
 * <p>
 * The same world and inputs give the same walk. Main thread only:
 * {@link #follow}, {@link #walkTo}, {@link #stop} and {@link #tick} throw
 * {@link IllegalStateException} off it. Exceptions thrown while ticking (by the
 * body or the finder) propagate to the caller.
 */
public final class PathWalker {

	/** Default stuck delay, in ticks. */
	public static final int DEFAULT_STUCK_TICKS = 40;

	/** How close, horizontally, the body must stand to the final point. */
	public static final double ARRIVAL_RADIUS = 0.5;

	/** The gain toward the current point that counts as progress, in blocks. */
	public static final double PROGRESS_STEP = 0.1;

	/** How far, horizontally, from a step the body jumps, in blocks. */
	public static final double STEP_REACH = 1.2;

	/** Ticks between two right-clicks on the same closed door. */
	public static final int DOOR_RETRY_TICKS = 10;

	/** How far the goal must move from the last searched goal to search again. */
	public static final double RESEARCH_DISTANCE = 1;

	/** Tolerance on the body's height when telling its block level. */
	private static final double LEVEL_EPSILON = 1e-3;

	/** Reads closed wooden doors in the world; a seam for tests. */
	@FunctionalInterface
	interface Doors {

		/**
		 * @return the block at that position if it is a closed wooden door, otherwise
		 *         {@code null}
		 */
		Block closedDoor(World world, int x, int y, int z);
	}

	/** The live world's closed wooden doors. */
	static final Doors WORLD_DOORS = (world, x, y, z) -> {
		var block = world.getBlockAt(x, y, z);
		if (Tag.WOODEN_DOORS.isTagged(block.getType()) && block.getBlockData() instanceof Door door && !door.isOpen()) {
			return block;
		}
		return null;
	};

	/** A copied path: feet block positions, read-only. */
	private record Track(PathResult source, World world, int[] xs, int[] ys, int[] zs, boolean reaches) {

		int size() {
			return xs.length;
		}

		boolean isDrop(int i) {
			return i > 0 && ys[i] < ys[i - 1];
		}
	}

	private final Body body;

	private final BodyControls controls;

	private final int stuckTicks;

	private final BooleanSupplier mainThread;

	private final Doors doors;

	private WalkStatus status = WalkStatus.IDLE;

	private Track track;

	/** Index of the point the body walks to. */
	private int cursor;

	/** Ticks since the last progress. */
	private int stall;

	/** Closest distance to the current point since the last progress. */
	private double best;

	/** The walker's own tick count. */
	private long ticks;

	/** Tick of the last door click, or a value long ago. */
	private long lastDoorClick = Long.MIN_VALUE / 2;

	// Goal mode.

	private Location goal;

	private PathFinder finder;

	private PathRule rule;

	/** Goal of the last search started. */
	private Location searchedGoal;

	/** Goal of the search the current path came from, null if not from a search. */
	private Location trackGoal;

	private boolean searching;

	/** Bumped to ignore the results of searches started before it. */
	private int generation;

	/** A search result waiting for the next tick. */
	private Landed landed;

	private record Landed(PathResult path, Throwable error, Location goal) {
	}

	/**
	 * A walker with {@link #DEFAULT_STUCK_TICKS}.
	 *
	 * @throws IllegalArgumentException
	 *             if {@code body} is {@code null}
	 */
	public PathWalker(Body body) {
		this(body, DEFAULT_STUCK_TICKS);
	}

	/**
	 * @param body
	 *            the body to drive
	 * @param stuckTicks
	 *            the stuck delay: ticks without progress before
	 *            {@link WalkStatus#STUCK}
	 * @throws IllegalArgumentException
	 *             if {@code body} is {@code null} or {@code stuckTicks} is below 1
	 */
	public PathWalker(Body body, int stuckTicks) {
		this(body, stuckTicks, Bukkit::isPrimaryThread, WORLD_DOORS);
	}

	PathWalker(Body body, int stuckTicks, BooleanSupplier mainThread, Doors doors) {
		if (body == null) {
			throw new IllegalArgumentException("body is null");
		}
		if (stuckTicks < 1) {
			throw new IllegalArgumentException("stuckTicks must be at least 1, got " + stuckTicks);
		}
		this.body = body;
		this.stuckTicks = stuckTicks;
		this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
		this.doors = Objects.requireNonNull(doors, "doors");
		this.controls = body.controls(ControlPriority.WALKER);
	}

	/**
	 * Follows {@code path} from now on, instead of any path or goal it had, from
	 * the first point after the one nearest the body. Writes nothing until the next
	 * {@link #tick()}.
	 *
	 * @throws IllegalStateException
	 *             if not called on the main thread
	 * @throws IllegalArgumentException
	 *             if {@code path} is {@code null} or empty, or one of its points is
	 *             not in the body's world
	 */
	public void follow(PathResult path) {
		requireMainThread();
		if (path == null) {
			throw new IllegalArgumentException("path is null");
		}
		var copy = copy(path, body.getLocation().getWorld());
		leaveGoalMode();
		adopt(copy, null);
		stall = 0;
		status = WalkStatus.WALKING;
	}

	/**
	 * Walks to {@code goal}, searching paths with {@code finder} under
	 * {@code rule}. While {@link WalkStatus#WALKING}, it only moves the goal (and
	 * takes the finder and rule for the next search); otherwise it starts a new
	 * walk with a search now, with no path until that search lands.
	 *
	 * @throws IllegalStateException
	 *             if not called on the main thread, or as the finder throws
	 * @throws IllegalArgumentException
	 *             if an argument is {@code null}, or {@code goal} is not in the
	 *             body's world
	 */
	public void walkTo(Location goal, PathFinder finder, PathRule rule) {
		requireMainThread();
		if (goal == null || finder == null || rule == null) {
			throw new IllegalArgumentException("goal, finder and rule must not be null");
		}
		var world = body.getLocation().getWorld();
		if (goal.getWorld() == null || !goal.getWorld().equals(world)) {
			throw new IllegalArgumentException(
					"goal is in world " + worldName(goal.getWorld()) + ", not in the body's " + worldName(world));
		}
		boolean fresh = status != WalkStatus.WALKING;
		if (fresh) {
			leaveGoalMode();
			track = null;
			cursor = 0;
			stall = 0;
			best = Double.POSITIVE_INFINITY;
			status = WalkStatus.WALKING;
		}
		this.goal = goal.clone();
		this.finder = finder;
		this.rule = rule;
		searchIfMoved();
	}

	/**
	 * Stops walking: writes neutral movement once if it was
	 * {@link WalkStatus#WALKING}, forgets its path and goal, and reports
	 * {@link WalkStatus#IDLE}.
	 *
	 * @throws IllegalStateException
	 *             if not called on the main thread
	 */
	public void stop() {
		requireMainThread();
		if (status == WalkStatus.WALKING) {
			neutral();
		}
		leaveGoalMode();
		track = null;
		status = WalkStatus.IDLE;
	}

	/**
	 * Drives the body for this tick; call it once per body tick, from the body's
	 * tick event.
	 *
	 * @return the walker's status after this tick
	 * @throws IllegalStateException
	 *             if not called on the main thread
	 */
	public WalkStatus tick() {
		requireMainThread();
		ticks++;
		if (status != WalkStatus.WALKING) {
			return status;
		}
		var location = body.getLocation();
		if (landed != null) {
			var result = landed;
			landed = null;
			if (!take(result, location) && track == null) {
				return finish(WalkStatus.STUCK);
			}
		}
		if (track == null) {
			// Waiting for the first path of a goal.
			if (++stall >= stuckTicks) {
				return finish(WalkStatus.STUCK);
			}
			writeMovement(null, false, false);
			return status;
		}
		if (!track.world().equals(location.getWorld())) {
			return finish(WalkStatus.STUCK);
		}
		boolean onGround = body.isOnGround();
		int level = level(location.getY());
		boolean advanced = false;
		int last = track.size() - 1;
		while (cursor < last) {
			if (reached(cursor, location, level, onGround)) {
				cursor++;
			} else if (reached(cursor + 1, location, level, onGround)) {
				// Passed the current point between two ticks.
				cursor = Math.min(cursor + 2, last);
			} else {
				break;
			}
			advanced = true;
		}
		if (cursor == last && arrived(location, level, onGround) && !newerGoalPending()) {
			return finish(track.reaches() ? WalkStatus.ARRIVED : WalkStatus.STUCK);
		}
		double distance = distanceTo(cursor, location);
		if (advanced || distance <= best - PROGRESS_STEP) {
			best = distance;
			stall = 0;
		} else if (++stall >= stuckTicks) {
			return finish(WalkStatus.STUCK);
		}
		drive(location, level, onGround);
		openDoor(location);
		searchIfMoved();
		return status;
	}

	/** @return the status of the last {@link #tick()}, or after the last call */
	public WalkStatus getStatus() {
		return status;
	}

	/**
	 * @return the path being followed, or {@code null} with none (idle, or waiting
	 *         for the first path of a goal); after an arrival or stuck, the last
	 *         path followed
	 */
	public PathResult getPath() {
		return track == null ? null : track.source();
	}

	private void requireMainThread() {
		if (!mainThread.getAsBoolean()) {
			throw new IllegalStateException("PathWalker must be used on the main thread");
		}
	}

	private static String worldName(World world) {
		return world == null ? null : world.getName();
	}

	/** Copies {@code path}'s points, checking they are all in {@code world}. */
	private static Track copy(PathResult path, World world) {
		var points = path.getPoints();
		if (points == null || points.isEmpty()) {
			throw new IllegalArgumentException("path has no points");
		}
		int n = points.size();
		var xs = new int[n];
		var ys = new int[n];
		var zs = new int[n];
		for (int i = 0; i < n; i++) {
			var point = points.get(i);
			if (point == null || point.getWorld() == null || !point.getWorld().equals(world)) {
				throw new IllegalArgumentException(
						"path point " + i + " is in world " + (point == null ? null : worldName(point.getWorld()))
								+ ", not in the body's " + worldName(world));
			}
			xs[i] = point.getBlockX();
			ys[i] = point.getBlockY();
			zs[i] = point.getBlockZ();
		}
		return new Track(path, world, xs, ys, zs, path.canReachFinalPoint());
	}

	/**
	 * Sets the track, its cursor after the point nearest the body, and the gain
	 * reference.
	 */
	private void adopt(Track copy, Location fromGoal) {
		var location = body.getLocation();
		track = copy;
		trackGoal = fromGoal;
		int nearest = 0;
		double nearestDistance = Double.POSITIVE_INFINITY;
		for (int i = 0; i < copy.size(); i++) {
			double d = distanceTo(i, location);
			if (d < nearestDistance) {
				nearest = i;
				nearestDistance = d;
			}
		}
		cursor = Math.min(nearest + 1, copy.size() - 1);
		best = distanceTo(cursor, location);
	}

	/** Adopts a landed search result. @return whether a path was adopted */
	private boolean take(Landed result, Location location) {
		if (result.error() != null || result.path() == null) {
			return false;
		}
		Track copy;
		try {
			copy = copy(result.path(), location.getWorld());
		} catch (IllegalArgumentException e) {
			// An empty path, or the body changed world since the search.
			return false;
		}
		adopt(copy, result.goal());
		return true;
	}

	private void leaveGoalMode() {
		generation++;
		goal = null;
		finder = null;
		rule = null;
		searchedGoal = null;
		trackGoal = null;
		searching = false;
		landed = null;
	}

	/** Starts a search to the current goal. */
	private void search() {
		var target = goal.clone();
		int searchGeneration = generation;
		searchedGoal = target;
		searching = true;
		CompletableFuture<PathResult> future;
		try {
			future = finder.find(body.getLocation(), target.clone(), rule);
		} catch (RuntimeException e) {
			searching = false;
			throw e;
		}
		future.whenComplete((path, error) -> {
			if (searchGeneration != generation) {
				return;
			}
			searching = false;
			landed = new Landed(path, error, target);
		});
	}

	private void searchIfMoved() {
		if (goal == null || searching || status != WalkStatus.WALKING) {
			return;
		}
		if (searchedGoal == null || distance(goal, searchedGoal) >= RESEARCH_DISTANCE) {
			search();
		}
	}

	/**
	 * @return whether the goal moved away from the one the current path was
	 *         searched for.
	 */
	private boolean newerGoalPending() {
		return goal != null && (trackGoal == null || distance(goal, trackGoal) >= RESEARCH_DISTANCE);
	}

	private static double distance(Location a, Location b) {
		double dx = a.getX() - b.getX();
		double dy = a.getY() - b.getY();
		double dz = a.getZ() - b.getZ();
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	private static int level(double y) {
		return (int) Math.floor(y + LEVEL_EPSILON);
	}

	private double distanceTo(int i, Location location) {
		double dx = track.xs()[i] + 0.5 - location.getX();
		double dy = track.ys()[i] - location.getY();
		double dz = track.zs()[i] + 0.5 - location.getZ();
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	private double horizontalDistanceTo(int i, Location location) {
		double dx = track.xs()[i] + 0.5 - location.getX();
		double dz = track.zs()[i] + 0.5 - location.getZ();
		return Math.sqrt(dx * dx + dz * dz);
	}

	/** Whether the body stands in point {@code i}'s block (landed, for a drop). */
	private boolean reached(int i, Location location, int level, boolean onGround) {
		return location.getBlockX() == track.xs()[i] && location.getBlockZ() == track.zs()[i] && level == track.ys()[i]
				&& (onGround || !track.isDrop(i));
	}

	private boolean arrived(Location location, int level, boolean onGround) {
		int last = track.size() - 1;
		return level == track.ys()[last] && horizontalDistanceTo(last, location) <= ARRIVAL_RADIUS
				&& (onGround || !track.isDrop(last));
	}

	private void drive(Location location, int level, boolean onGround) {
		double dx = track.xs()[cursor] + 0.5 - location.getX();
		double dz = track.zs()[cursor] + 0.5 - location.getZ();
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		// + 0f turns -0.0 into 0.0.
		float yaw = horizontal > 1e-6 ? (float) Math.toDegrees(Math.atan2(-dx, dz)) + 0f : body.getLookYaw();
		boolean jump = track.ys()[cursor] > level && onGround && horizontal <= STEP_REACH;
		writeMovement(yaw, true, jump);
		// From the eye to the point's eye level: flat ahead, up toward a step.
		double dy = track.ys()[cursor] - location.getY();
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(horizontal, 1e-6))) + 0f;
		controls.setLook(yaw, Math.clamp(pitch, -90f, 90f));
	}

	/** Right-clicks a closed wooden door in the next point's feet or head block. */
	private void openDoor(Location location) {
		if (ticks - lastDoorClick < DOOR_RETRY_TICKS) {
			return;
		}
		int x = track.xs()[cursor];
		int y = track.ys()[cursor];
		int z = track.zs()[cursor];
		var door = doors.closedDoor(track.world(), x, y, z);
		if (door == null) {
			door = doors.closedDoor(track.world(), x, y + 1, z);
		}
		if (door == null) {
			return;
		}
		double dx = location.getX() - (x + 0.5);
		double dz = location.getZ() - (z + 0.5);
		BlockFace face;
		if (Math.abs(dx) > Math.abs(dz)) {
			face = dx > 0 ? BlockFace.EAST : BlockFace.WEST;
		} else {
			face = dz > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
		}
		controls.useItemOn(door, face, EquipmentSlot.HAND);
		lastDoorClick = ticks;
	}

	private void writeMovement(Float heading, boolean forward, boolean jump) {
		controls.setHeading(heading);
		controls.setForward(forward);
		controls.setBackward(false);
		controls.setLeft(false);
		controls.setRight(false);
		controls.setJump(jump);
		controls.setSneak(false);
		controls.setSprint(false);
	}

	private void neutral() {
		writeMovement(null, false, false);
	}

	private WalkStatus finish(WalkStatus end) {
		neutral();
		status = end;
		return end;
	}
}
