package fr.nekotine.core.navigation.astar;

/**
 * The blocks of a search, classified: what {@link AStarSearch} walks on.
 * Injectable so the search runs without a server.
 */
@FunctionalInterface
interface CellGrid {

	/** @return the cell at that position; {@link Cell#BLOCKED} outside the grid. */
	Cell cell(int x, int y, int z);
}
