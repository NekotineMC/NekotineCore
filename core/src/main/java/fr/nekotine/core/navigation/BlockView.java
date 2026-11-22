package fr.nekotine.core.navigation;

import org.bukkit.block.data.BlockData;

/**
 * A read-only view of a world's blocks, by world coordinates, as a
 * {@link PathFinder} sees them: what a {@link PathRule} reads instead of a live
 * world. An implementation that searches off the main thread backs it with a
 * snapshot taken on the main thread, so edits made after the search started are
 * not seen.
 *
 * <p>
 * Thread-safe: it may be read from the thread that searches.
 */
@FunctionalInterface
public interface BlockView {

	/**
	 * @return the block data at that position, or {@code null} when the view does
	 *         not cover it (outside the searched area or the world height, or in a
	 *         chunk that was not loaded)
	 */
	BlockData getBlockData(int x, int y, int z);
}
