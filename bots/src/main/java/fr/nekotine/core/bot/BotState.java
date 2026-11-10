package fr.nekotine.core.bot;

/** Lifecycle state of a {@link Bot} as listed by {@link BotRoster#list()}. */
public enum BotState {
	/** Connected and ticked by its roster every server tick. */
	ACTIVE,
	/**
	 * Library work for the bot threw: it stays connected and listed but is no
	 * longer ticked (no connection tick, acks or {@link BotTickEvent}). It can
	 * still be removed.
	 */
	FAILED
}
