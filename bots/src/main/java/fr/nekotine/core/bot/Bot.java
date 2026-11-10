package fr.nekotine.core.bot;

import org.bukkit.entity.Player;

/**
 * A server-side fake player spawned by a {@link BotRoster}. The server, other
 * plugins and ProtocolLib see it as a real online player.
 */
public interface Bot {

	/** @return the bot's player name, as given to {@link BotRoster#spawn}. */
	String name();

	/** @return the bot's Bukkit player handle. */
	Player player();

	/** @return the bot's current state. */
	BotState state();
}
