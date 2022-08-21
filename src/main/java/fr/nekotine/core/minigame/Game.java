package fr.nekotine.core.minigame;

import java.util.List;

import org.bukkit.entity.Player;

/**
 * Représente le mode de jeu actif tout au long de la partie
 * 
 * @author XxGoldenbluexX
 *
 */
public abstract class Game {

	private final GameEventListener _holder;
	
	private final List<GameTeam> _teams;
	
	private boolean _isPlaying = false;
	
	private int _playerCap = 10;
	
	public Game(GameEventListener holder, List<GameTeam> teams) {
		_holder = holder;
		_teams = teams;
	}
	
	public Game(List<GameTeam> teams) {
		this(null, teams);
	}
	
	public List<GameTeam> getTeams(){
		return _teams;
	}
	
	public boolean isPlaying() {
		return _isPlaying;
	}
	
	/**
	 * Une methode plus efficace que de prendre la taille de la liste des joueurs car elle évite la construction de cette liste.
	 * 
	 * @return
	 */
	public int getNumberOfPlayer() {
		int nb = 0;
		for (GameTeam team : _teams) {
			nb += team.getPlayerList().size();
		}
		return nb;
	}
	
	public void destroy() {
		
	}
	
	public int getPlayerCap() {
		return _playerCap;
	}
	
	/**
	 * Démarre la partie
	 */
	public void Start() {
		_isPlaying = true;
		if (_holder != null) {
			_holder.onGameStart(this);
		}
	}
	
	/**
	 * Met fin à la partie. Les statistiques/données d'après match sont enregistrées et le traitement
	 * pour un quelconque classement est fait.<br><br>
	 * Cette fonction est utile pour les modes de jeu qui ne peuvent se finir seul (un mode de jeu sans fin par exemple).
	 */
	public void Stop() {
		_isPlaying = false;
		if (_holder != null) {
			_holder.onGameStop(this);
		}
	}
	
	/**
	 * Met fin prématurément à la partie. Aucun traitement n'est fait sur les statistiques/données d'apès match,
	 * aucun traitement pour un quelconque classement est fait. La partie "compte pour du beure".
	 */
	public void Abort() {
		_isPlaying = false;
		if (_holder != null) {
			_holder.onGameStop(this);
		}
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
		_teams.get(0).addPlayer(player);
	}
	
	public void removePlayer(Player player) {
		for (GameTeam team : _teams) {
			team.removePlayer(player);
		}
	}
	
}
