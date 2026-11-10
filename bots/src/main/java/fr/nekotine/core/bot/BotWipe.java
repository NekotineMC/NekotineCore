package fr.nekotine.core.bot;

/**
 * Which of a bot's saved files its roster deletes once it has left, after the
 * server saved them.
 *
 * @param state
 *            delete the player data ({@code players/data/<uuid>.dat} and its
 *            {@code .dat_old} backup)
 * @param statistics
 *            delete the statistics and advancements
 *            ({@code players/stats/<uuid>.json},
 *            {@code players/advancements/<uuid>.json})
 */
public record BotWipe(boolean state, boolean statistics) {

	/** Deletes every file: the bot leaves no trace. The default. */
	public static final BotWipe ALL = new BotWipe(true, true);
}
