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
	
	protected abstract void globalBegin(GD gamedata);
	
	protected abstract void globalEnd(GD gamedata);
	
	protected abstract void playerBegin(GD gamedata, Player player, GameTeam team);
	
	protected abstract void playerEnd(GD gamedata, Player player, GameTeam team);
	
	public final void Begin(GD gamedata) {
		globalBegin(gamedata);
		for (var team : gamedata.getTeams()) {
			for (var player : team.getPlayerList()) {
				playerBegin(gamedata, player, team);
			}
		}
	}
	
	public final void End(GD gamedata) {
		for (var team : gamedata.getTeams()) {
			for (var player : team.getPlayerList()) {
				playerEnd(gamedata, player, team);
			}
		}
		globalEnd(gamedata);
	}
	
}
