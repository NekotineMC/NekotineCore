package fr.nekotine.core.navigation;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;

/**
 * Finds a path a player-sized body can walk, under a caller's {@link PathRule}.
 * Callers depend on this port only, so the algorithm behind it can change
 * without touching them.
 *
 * <h2>Threads</h2>
 *
 * <p>
 * {@link #find} is called on the main thread. An implementation may search off
 * the main thread: it then reads the world only through a snapshot taken on the
 * main thread inside {@code find}, calls the rule only off the main thread and
 * only with that snapshot's {@link BlockView}, and completes the future on the
 * main thread. A synchronous implementation completes it before returning. A
 * search that fails (e.g. the rule throws) completes the future exceptionally.
 * If the implementation's owning plugin is disabled before completion, the
 * future may never complete.
 *
 * <h2>Result</h2>
 *
 * <p>
 * The points are feet block positions (integer coordinates, like Paper's mob
 * paths), in {@code from}'s world, the start first. When the goal cannot be
 * reached, the path leads to the reached point closest to the goal and
 * {@link PathResult#canReachFinalPoint()} is {@code false}. Results are
 * immutable; {@link PathResult#getNextPointIndex()} is 0, since the follower
 * keeps its own cursor. An implementation may return a
 * {@link HintedPathResult}.
 *
 * <p>
 * The same inputs on the same world give the same path.
 */
public interface PathFinder {

	/**
	 * @param from
	 *            where the body stands
	 * @param to
	 *            where it should go, in the same world
	 * @param rule
	 *            the passability and cost rule of this search
	 * @return a future of the path, completed on the main thread
	 * @throws IllegalStateException
	 *             if not called on the main thread, or if the implementation cannot
	 *             search (e.g. its owning plugin is disabled)
	 * @throws IllegalArgumentException
	 *             if an argument or a location's world is {@code null}, or the
	 *             locations are in different worlds
	 */
	CompletableFuture<PathResult> find(Location from, Location to, PathRule rule);
}
