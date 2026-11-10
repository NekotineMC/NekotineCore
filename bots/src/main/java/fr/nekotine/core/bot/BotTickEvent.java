package fr.nekotine.core.bot;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired once per server tick for each live (not failed) bot, synchronously on
 * the main thread, by the bot's {@link BotRoster}. When it fires, the bot's
 * connection has ticked and its client acks for that tick have been received.
 *
 * <p>
 * Exceptions thrown by listeners are the server's to log: they never fail the
 * bot.
 */
public class BotTickEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Bot bot;

	public BotTickEvent(Bot bot) {
		this.bot = bot;
	}

	/** @return the ticking bot. */
	public Bot getBot() {
		return bot;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
