package fr.nekotine.core.game;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.game.event.GamePlayerLeaveEvent;
import fr.nekotine.core.game.exception.PlayerNotInGameException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;

/**
 * 
 * @author XxGoldenbluexX
 *
 */
public class Game<GD extends GameData> implements ForwardingAudience{
	
	private GameMode<GD> gamemode;
	
	private GD gamedata;
	
	private List<GameTeam> teams = new LinkedList<>();
	
	private boolean isPlaying = false;
	
	private int playerCap = 10;
	
	public Game(GameMode<GD> gamemode, GD gamedata) {
		this.gamemode = gamemode;
		this.gamedata = gamedata;
		gamemode.registerTeams(this);
	}
	
	public List<GameTeam> getTeams(){
		return teams;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}
	
	public void setIsPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}
	
	public GD getGameData() {
		return gamedata;
	}
	
	/**
	 * Une methode plus efficace que de prendre la taille de la liste des joueurs car elle évite la construction de cette liste.
	 * 
	 * @return
	 */
	public int getNumberOfPlayer() {
		int nb = 0;
		for (GameTeam team : teams) {
			nb += team.getPlayerList().size();
		}
		return nb;
	}
	
	public GameMode<GD> getGameMode(){
		return gamemode;
	}
	
	public void destroy() {
		if (isPlaying) {
			gamemode.Abort(this);
		}
		for (GameTeam team : teams) {
			team.clear();
		}
	}
	
	public int getPlayerCap() {
		return playerCap;
	}
	
	/**
	 * Démare la game.
	 */
	public void start() {
		gamemode.Start(this);
	}

	/**
	 * Ajoute le joueur dans la partie.
	 * le joueur rejoint l'équipe la plus optimale pour que la partie soit équilibrée.
	 * 
	 * En générale, l'équipe optimale dépend du mode de jeu.
	 * 
	 * @param player
	 */
	public void addPlayerToOptimalTeam(Player player) {
		gamemode.addPlayerToOptimalTeam(this, player);
	}
	
	public void removePlayer(Player player) {
		if (containsPlayer(player)) {
			for (GameTeam team : teams) {
				team.removePlayer(player);
			}
			gamemode.onPlayerPostLeave(this, player);
			var event = new GamePlayerLeaveEvent(this, player);
			event.callEvent();
		}
	}
	
	/**
	 * Retourne si oui ou non le joueur est dans cette partie.
	 * @param player
	 * @return
	 */
	public boolean containsPlayer(Player player) {
		for (var team : getTeams()) {
			if (team.containsPlayer(player)) {
				return true;
			}
		}
		return false;
	}
	
	public List<Player> getPlayerList(){
		List<Player> list = new LinkedList<>();
		for (GameTeam team : teams) {
			list.addAll(team.getPlayerList());
		}
		return list;
	}
	
	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return getPlayerList();
	}
	
	public GameTeam getPlayerTeam(Player player){
		for (var team : teams) {
			if (team.containsPlayer(player)) {
				return team;
			}
		}
		throw new PlayerNotInGameException();
	}
	
}
