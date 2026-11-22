package fr.nekotine.core.navigation;

/**
 * How a body reaches a point of a path from the point before it: a per-point
 * hint of a {@link HintedPathResult}.
 */
public enum PathMove {

	/** The first point: where the body starts. */
	START,

	/** A walk on the same level, straight or diagonal. */
	WALK,

	/** A walk one block up: the body jumps. */
	STEP_UP,

	/** A walk off an edge, landing up to 3 blocks lower. */
	DROP,

	/** A walk into a wooden door, which the body may have to open. */
	DOOR
}
