package fr.nekotine.botstesthost;

import fr.nekotine.core.NekotinePlugin;
import fr.nekotine.core.bot.BotRoster;
import fr.nekotine.core.ioc.Ioc;

/**
 * Minimal Paper plugin hosting the bots library for NekotineCore's integration
 * tests. Lives outside {@code fr.nekotine.core} so Core's module scan never
 * sees it.
 */
public class BotsTestHost extends NekotinePlugin {

	@Override
	public void onEnable() {
		super.onEnable();
		// Eager: runs the bots self-test at enable (Core loads modules lazily).
		Ioc.resolve(BotRoster.class);
	}
}
