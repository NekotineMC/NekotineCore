package fr.nekotine.core.game;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import fr.nekotine.core.util.EventUtil;

/**
 * Une phase d'un mode de jeu.
 * 
 * @author XxGoldenbluexX
 *
 * @param <GD>
 * @param <GM>
 */
public abstract class GamePhase<GD extends GameData,GM extends GameMode<GD>> implements Listener{
	
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
		EventUtil.Register(gamemode.getPlugin(), this);
	}
	
	public final void End(Game<GD> game) {
		EventUtil.Unregister(this);
		for (var team : game.getTeams()) {
			for (var player : team.getPlayerList()) {
				playerEnd(game, player, team);
			}
		}
		globalEnd(game);
	}
	
}
