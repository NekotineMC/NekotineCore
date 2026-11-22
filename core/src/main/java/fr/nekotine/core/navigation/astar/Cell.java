package fr.nekotine.core.navigation.astar;

/** What one block is to a player-sized body, as the search sees it. */
enum Cell {

	/** A floor to stand on; blocks the body. */
	SOLID,

	/** The body passes through; not a floor (air, grass, a sign...). */
	OPEN,

	/** A wooden door, open or closed: the body passes through; not a floor. */
	DOOR,

	/**
	 * Neither a floor nor passable: outside the snapshot or in an unloaded chunk,
	 * liquids, fences, walls, hazards.
	 */
	BLOCKED;

	/** @return whether the body's feet or head may be in this block. */
	boolean passable() {
		return this == OPEN || this == DOOR;
	}
}
