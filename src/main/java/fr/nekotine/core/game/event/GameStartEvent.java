package fr.nekotine.core.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.game.Game;

/**
 * Cet événement est lancé lorsque une partie viens d'être lancée.
 * @author XxGoldenbluexX
 *
 */
public class GameStartEvent extends Event{

	private final Game _game;
	
	public GameStartEvent(Game game) {
		_game = game;
	}
	
	public Game getGame() {
		return _game;
	}
	
	// Event specific
	
	private static final HandlerList HANDLER_LIST = new HandlerList();
	
	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
	
	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}

}
