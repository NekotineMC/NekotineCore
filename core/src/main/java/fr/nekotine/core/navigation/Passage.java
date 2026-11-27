package fr.nekotine.core.navigation;

/**
 * What one block is to a body for one search, as a {@link PathRule} overrides
 * it (see {@link PathRule#passage}): a game door that opens for this body only,
 * a corridor closed at head height, a temporary bridge.
 */
public enum Passage {

	/** No override: the {@link PathFinder} reads the block as the world has it. */
	DEFAULT,

	/** Passable like air: the body's feet or head may be in it; not a floor. */
	OPEN,

	/** Solid: the body may stand on it, never be in it. */
	FLOOR,

	/** Never passable and not a floor, like a liquid or a fence. */
	BLOCKED
}
