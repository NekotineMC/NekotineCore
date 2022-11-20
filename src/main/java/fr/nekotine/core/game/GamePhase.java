package fr.nekotine.core.game;

import org.bukkit.entity.Player;

/**
 * Une phase d'un mode de jeu.
 * 
 * @author XxGoldenbluexX
 *
 * @param <GD>
 * @param <GM>
 */
public abstract class GamePhase<GD extends GameData,GM extends GameMode<GD>>{
	
	private final GM gamemode;
	
	public GamePhase(GM gamemode) {
		this.gamemode = gamemode;
	}
	
	public GM getGameMode() {
		return gamemode;
	}
	
	protected abstract void globalBegin(Game<GD> game);
	
	protected abstract void globalEnd(Game<GD> game);
	
	protected abstract void playerBegin(Game<GD> game, Player player, GameTeam team);
	
	protected abstract void playerEnd(Game<GD> game, Player player, GameTeam team);
	
	public final void Begin(Game<GD> game) {
		globalBegin(game);
		for (var team : game.getTeams()) {
			for (var player : team.getPlayerList()) {
				playerBegin(game, player, team);
			}
		}
	}
	
	public final void End(Game<GD> game) {
		for (var team : game.getTeams()) {
			for (var player : team.getPlayerList()) {
				playerEnd(game, player, team);
			}
		}
		globalEnd(game);
	}
	
}
