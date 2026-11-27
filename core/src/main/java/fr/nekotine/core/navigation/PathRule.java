package fr.nekotine.core.navigation;

/**
 * The caller's game rule for one search: which feet positions a path may use,
 * what extra cost each one adds, and, optionally, what single blocks are to the
 * body regardless of the world ({@link #passage}). The {@link PathFinder}
 * decides physical walkability (floor, headroom, steps, drops, doors) from the
 * blocks, overridden by {@link #passage}; {@link #allows} and {@link #cost}
 * then only remove positions and add cost on top.
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
 * position), so a search can cache its answers and stay deterministic. A rule
 * is built per search and per body, so two bodies searching the same area may
 * use different rules (one sees a door open, the other closed) with no shared
 * state.
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

	/**
	 * Overrides what one block is to the body, for this search only. Asked about
	 * any block the search reads that the view covers (never where
	 * {@link BlockView#getBlockData} is {@code null}), whatever the world holds
	 * there: feet, head and floor blocks, and the blocks passed through between two
	 * points. {@link #allows} and {@link #cost} still apply on top. Same purity and
	 * thread rules as {@link #allows}. A {@link PathFinder} that cannot honour
	 * overrides says so in its Javadoc.
	 *
	 * @param view
	 *            the blocks of the search
	 * @param x
	 *            block x
	 * @param y
	 *            block y
	 * @param z
	 *            block z
	 * @return what the block is to the body; {@link Passage#DEFAULT} (the default)
	 *         keeps the finder's own reading of the world
	 */
	default Passage passage(BlockView view, int x, int y, int z) {
		return Passage.DEFAULT;
	}
}
