package fr.nekotine.core.navigation;

/**
 * The caller's game rule for one search: which feet positions a path may use,
 * and what extra cost each one adds. The {@link PathFinder} decides physical
 * walkability (floor, headroom, steps, drops, doors); a rule only removes
 * positions and adds cost on top.
 *
 * <p>
 * A rule is asked about the feet blocks of the path's points, and also about
 * the feet blocks the body passes through between two points (for
 * {@code AStarPathFinder}: a diagonal's two side blocks, a drop's edge block
 * and the blocks it falls through); refusing one of those refuses the move. It
 * is never asked about the start.
 *
 * <p>
 * A rule reads blocks only through the {@link BlockView} it is given, never a
 * live world: a search may call it off the main thread (see
 * {@link PathFinder}). It must be pure (same answer for the same view and
 * position), so a search can cache its answers and stay deterministic.
 */
@FunctionalInterface
public interface PathRule {

	/** A rule that allows every position and adds no cost. */
	PathRule ANY = (view, x, y, z) -> true;

	/**
	 * @param view
	 *            the blocks of the search
	 * @param x
	 *            feet block x
	 * @param y
	 *            feet block y
	 * @param z
	 *            feet block z
	 * @return whether a body's feet may be in, or pass through, that block
	 */
	boolean allows(BlockView view, int x, int y, int z);

	/**
	 * Called only for allowed positions.
	 *
	 * @param view
	 *            the blocks of the search
	 * @param x
	 *            feet block x
	 * @param y
	 *            feet block y
	 * @param z
	 *            feet block z
	 * @return the extra cost of moving into that position, on top of the move's own
	 *         cost (a walk is about 1 per block); finite and {@code >= 0},
	 *         otherwise the search fails; 0 by default
	 */
	default double cost(BlockView view, int x, int y, int z) {
		return 0;
	}
}
