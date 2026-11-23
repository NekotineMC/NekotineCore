package fr.nekotine.core.navigation;

/**
 * What a {@link PathWalker} reports at each of its ticks.
 */
public enum WalkStatus {

	/**
	 * No path or goal: the walker has never been given one, or was
	 * {@link PathWalker#stop() stopped}. It writes nothing.
	 */
	IDLE,

	/**
	 * Following a path, or waiting for the first path of a goal: the walker drives
	 * the body every tick.
	 */
	WALKING,

	/**
	 * The body stands at the final point of a path that reaches its goal. The
	 * walker wrote neutral movement once and writes nothing until it gets a new
	 * path or goal.
	 */
	ARRIVED,

	/**
	 * The body made no progress for the walker's stuck delay, or reached the end of
	 * a path that does not reach its goal, or no path could be found. The walker
	 * wrote neutral movement once and writes nothing until it gets a new path or
	 * goal: what to do next is the caller's decision.
	 */
	STUCK
}
