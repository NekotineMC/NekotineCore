package fr.nekotine.core.navigation;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;

/**
 * A {@link PathResult} that also tells how each point is reached. Walkers may
 * use the hints but must work without them, on any {@link PathResult}.
 */
public interface HintedPathResult extends PathResult {

	/**
	 * @param index
	 *            index of a point in {@link #getPoints()}
	 * @return how the body reaches that point from the one before it;
	 *         {@link PathMove#START} for index 0
	 * @throws IndexOutOfBoundsException
	 *             if there is no point at that index
	 */
	PathMove getMove(int index);
}
