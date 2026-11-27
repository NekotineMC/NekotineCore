package fr.nekotine.core.navigation.astar;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import fr.nekotine.core.navigation.HintedPathResult;
import fr.nekotine.core.navigation.PathFinder;
import fr.nekotine.core.navigation.PathRule;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

/**
 * A grid A* {@link PathFinder} for a player-sized body, searching off the main
 * thread.
 *
 * <p>
 * {@link #find} snapshots, on the main thread, the loaded chunks over the box
 * of {@code from} and {@code to} widened by a margin on every side, and always
 * by at least 4 blocks below and 2 above so floors and heads near both ends are
 * covered (clamped to the world height); it never loads a chunk, and blocks
 * outside the box or in an unloaded chunk are impassable. The search then runs
 * on a Bukkit async task, calling the rule there with the snapshot's view only,
 * and the future completes on the main thread in a later tick. A failed search
 * is logged once at WARNING through the plugin's logger, then completes the
 * future exceptionally. If the plugin is disabled before completion, Bukkit
 * cancels the pending tasks and the future may never complete.
 *
 * <p>
 * Walking model: a solid floor below, feet and head in non-solid blocks, a
 * carpet or a wooden door; 8-way walks (diagonals only when both sides are
 * clear), a 1-block step up, drops of up to 3 blocks. Other partial blocks
 * (slabs, stairs, snow layers) count as full blocks. No swimming, ladders or
 * jumps over gaps; liquids, fences, walls and hazards block. A start or goal
 * inside a non-passable block (a slab, a dirt path) counts from the block
 * above; one in the air, from the first floor up to 3 blocks below. The rule's
 * {@link PathRule#passage} overrides are honoured inside the snapshot (outside
 * it, blocks stay impassable and the rule is not asked). Results are
 * {@link HintedPathResult}s. A search expands at most its node budget of
 * positions; running out counts as unreachable. No caching: each call snapshots
 * and searches anew.
 */
public final class AStarPathFinder implements PathFinder {

	/** Default margin around the from/to box, in blocks. */
	public static final int DEFAULT_MARGIN = 16;

	/** Default node budget per search. */
	public static final int DEFAULT_NODE_BUDGET = 100_000;

	private final Plugin plugin;

	private final int margin;

	private final int nodeBudget;

	/** A finder with {@link #DEFAULT_MARGIN} and {@link #DEFAULT_NODE_BUDGET}. */
	public AStarPathFinder(Plugin plugin) {
		this(plugin, DEFAULT_MARGIN, DEFAULT_NODE_BUDGET);
	}

	/**
	 * @param plugin
	 *            owner of the search tasks
	 * @param margin
	 *            how far, in blocks, a path may leave the box of its two ends
	 * @param nodeBudget
	 *            how many positions a search may expand
	 * @throws IllegalArgumentException
	 *             if the plugin is null, the margin negative or the budget below 1
	 */
	public AStarPathFinder(Plugin plugin, int margin, int nodeBudget) {
		if (plugin == null) {
			throw new IllegalArgumentException("plugin is null");
		}
		if (margin < 0) {
			throw new IllegalArgumentException("margin must be >= 0, got " + margin);
		}
		if (nodeBudget < 1) {
			throw new IllegalArgumentException("node budget must be at least 1, got " + nodeBudget);
		}
		this.plugin = plugin;
		this.margin = margin;
		this.nodeBudget = nodeBudget;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalStateException
	 *             also if the owning plugin is disabled
	 */
	@Override
	public CompletableFuture<PathResult> find(Location from, Location to, PathRule rule) {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("PathFinder.find must be called on the main thread");
		}
		if (from == null || to == null || rule == null) {
			throw new IllegalArgumentException("from, to and rule must not be null");
		}
		var world = from.getWorld();
		var toWorld = to.getWorld();
		if (world == null || toWorld == null) {
			throw new IllegalArgumentException("from and to must have a world");
		}
		if (!world.equals(toWorld)) {
			throw new IllegalArgumentException(
					"from and to are in different worlds: " + world.getName() + ", " + toWorld.getName());
		}
		if (!plugin.isEnabled()) {
			throw new IllegalStateException("the pathfinder's plugin " + plugin.getName() + " is disabled");
		}
		int fx = from.getBlockX();
		int fy = from.getBlockY();
		int fz = from.getBlockZ();
		int tx = to.getBlockX();
		int ty = to.getBlockY();
		int tz = to.getBlockZ();
		var view = SnapshotView.take(world, Math.min(fx, tx) - margin,
				Math.min(fy, ty) - Math.max(margin, AStarSearch.MAX_DROP + 1), Math.min(fz, tz) - margin,
				Math.max(fx, tx) + margin, Math.max(fy, ty) + Math.max(margin, 2), Math.max(fz, tz) + margin);
		var future = new CompletableFuture<PathResult>();
		var scheduler = plugin.getServer().getScheduler();
		Runnable search = () -> {
			PathResult result = null;
			Throwable error = null;
			try {
				result = new AStarPath(world, AStarSearch.search(view, view, rule, fx, fy, fz, tx, ty, tz, nodeBudget));
			} catch (Throwable t) {
				error = t;
				plugin.getLogger().log(Level.WARNING, "path search from " + fx + " " + fy + " " + fz + " to " + tx + " "
						+ ty + " " + tz + " in " + world.getName() + " failed", t);
			}
			var path = result;
			var failure = error;
			try {
				scheduler.runTask(plugin, () -> complete(future, path, failure));
			} catch (IllegalPluginAccessException e) {
				if (failure != null) {
					e.addSuppressed(failure);
				}
				future.completeExceptionally(e);
			}
		};
		try {
			scheduler.runTaskAsynchronously(plugin, search);
		} catch (IllegalPluginAccessException e) {
			throw new IllegalStateException("the pathfinder's plugin " + plugin.getName() + " is disabled", e);
		}
		return future;
	}

	private static void complete(CompletableFuture<PathResult> future, PathResult path, Throwable failure) {
		if (failure != null) {
			future.completeExceptionally(failure);
		} else {
			future.complete(path);
		}
	}
}
