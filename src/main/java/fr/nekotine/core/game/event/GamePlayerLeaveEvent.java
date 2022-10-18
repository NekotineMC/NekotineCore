package fr.nekotine.core.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.game.Game;

/**
 * Cet événement est lancé lorsque un joueur viens d'être retiré de la partie.
 * @author XxGoldenbluexX
 *
 */
public class GamePlayerLeaveEvent extends Event{

	private final Game _game;
	
	private final Player _player;
	
	public GamePlayerLeaveEvent(Game game, Player player) {
		_game = game;
		_player = player;
	}
	
	public Game getGame() {
		return _game;
	}
	
	public Player getPlayer() {
		return _player;
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
