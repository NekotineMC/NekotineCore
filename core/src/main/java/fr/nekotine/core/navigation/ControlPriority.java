package fr.nekotine.core.navigation;

/**
 * The fixed priority of a writer of a {@link Body}'s controls, lowest first.
 *
 * <p>
 * The same order applies to every control group (movement, aim, hands): per
 * group and per tick, only the writes of the highest priority that wrote
 * anything in that group apply; lower writes in that group are dropped. The
 * order is the enum's declaration order, independent of listener order. Direct
 * controls of the body (e.g. a bot's own setters) rank below every priority.
 */
public enum ControlPriority {

	/** A walker following a path: the lowest priority. */
	WALKER,

	/** A melee controller: overrides the walker (e.g. its aim). */
	MELEE,

	/**
	 * The body's brain: the highest priority, so a brain can override its
	 * strategies for a tick without stopping them.
	 */
	BRAIN
}
