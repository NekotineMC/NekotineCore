package fr.nekotine.core.bot;

/**
 * Thrown by every {@link BotRoster} call when the bots library is unavailable:
 * its startup self-test found server internals it relies on missing or changed
 * (usually after a Paper update), so bots are disabled. The error naming what
 * broke was logged when the roster was created. The host and the server keep
 * running.
 */
public class BotsUnavailableException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	public BotsUnavailableException(String message) {
		super(message.startsWith("Bots unavailable") ? message : "Bots unavailable: " + message);
	}
}
