package fr.nekotine.core.game;

import org.bukkit.entity.Player;

public abstract class GamePhase<GameType extends Game>{
	
	public GamePhase(GameType game) {
		_game = game;
	}
	
	private final GameType _game;
	
	public GameType getGame() {
		return _game;
	}
	
	public abstract void globalBegin();
	
	public abstract void globalEnd();
	
	public abstract void playerBegin(Player player, GameTeam team);
	
	public abstract void playerEnd(Player player, GameTeam team);
	
	public final void begin() {
		globalBegin();
		for (var team : _game.getTeams()) {
			for (var player : team.getPlayerList()) {
				playerBegin(player, team);
			}
		}
	}
	
	public final void end() {
		for (var team : _game.getTeams()) {
			for (var player : team.getPlayerList()) {
				playerEnd(player, team);
			}
		}
		globalEnd();
	}
	
}
