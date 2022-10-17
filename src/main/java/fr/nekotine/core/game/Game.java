package fr.nekotine.core.game;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.game.event.GameStartEvent;
import fr.nekotine.core.plugin.CorePlugin;
import fr.nekotine.core.util.UtilEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;

/**
 * Représente le mode de jeu actif tout au long de la partie.
 * Cette classe implemente org.bukkit.event.Listener.
 * Elle est apte à recevoir des évenements Bukkit SEULEMENT QUAND LA PARTIE EST EN COURS.
 * 
 * @author XxGoldenbluexX
 *
 */
public abstract class Game implements Listener, ForwardingAudience{
	
	private List<GameTeam> _teams = new LinkedList<>();
	
	private Map<String, GamePhase> _gamePhases = new HashMap<>();
	
	private GamePhase currentGamePhase;
	
	private boolean _isPlaying = false;
	
	private int _playerCap = 10;
	
	public Game() {
		registerTeams(_teams);
		registerGamePhases(_gamePhases);
	}
	
	/**
	 * Cette fonction est appelée à la création de la partie pour mettre en place les équipes.
	 * @param teamList liste des équipes a completer.
	 */
	public abstract void registerTeams(List<GameTeam> teamList);
	
	/**
	 * Cette fonction est appelée à la création de la partie pour mettre en place les différentes phases de jeu.
	 * @param _gamePhasesMap
	 */
	public abstract void registerGamePhases(Map<String, GamePhase> _gamePhasesMap);
	
	/**
	 * Changement de phase pour la phase indiquée par la clef donnée en argument.
	 * @param phaseKey la clef pointant vers la phase voulue
	 */
	public void GotoGamePhase(String phaseKey){
		var phase = _gamePhases.get(phaseKey);
		if (phase != null) {
			if (currentGamePhase != null) {
				try {
					currentGamePhase.end();
				}catch(Exception e) {
					var plugin = CorePlugin.getCorePluginInstance();
					var msg = "Une erreur est survenue lors de la fin de la GamePhase actuelle.";
					for (var key : _gamePhases.keySet()) {
						if (_gamePhases.get(key) == currentGamePhase) {
							msg = "Une erreur est survenue lors de la fin de la GamePhase "+key;
							break;
						}
					}
					plugin.getLogger().log(Level.SEVERE, msg, e);
				}
			}
			currentGamePhase = phase;
			try {
				currentGamePhase.begin();
			}catch(Exception e) {
				var plugin = CorePlugin.getCorePluginInstance();
				var msg = "Une erreur est survenue lors du début de la GamePhase " + phaseKey;
				plugin.getLogger().log(Level.SEVERE, msg, e);
			}
		}
	}
	
	/**
	 * Méthode appelée naturellement au début de la partie.
	 * Elle permet d'aller vers la première phase.
	 */
	public abstract void GotoFirstPhase();
	
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
		if (_isPlaying) {
			Abort();
		}
		for (GameTeam team : _teams) {
			team.clear();
		}
	}
	
	public int getPlayerCap() {
		return _playerCap;
	}
	
	/**
	 * Démarre la partie.
	 * @param plugin Une référence au plugin
	 * @return Si oui ou non le démarrage est un succes.
	 */
	public final boolean Start() {
		if (_isPlaying) return true;
		var plugin = CorePlugin.getCorePluginInstance();
		try {
			setup();
		}catch(Exception e) {
			var msg = "Une erreur est survenue lors du demarrage de la partie";
			plugin.getLogger().log(Level.SEVERE, msg, e);
			return false;
		}
		GotoFirstPhase();
		UtilEvent.Register(plugin, this);
		_isPlaying = true;
		var startEvent = new GameStartEvent(this);
		startEvent.callEvent();
		return true;
	}
	
	/**
	 * Met fin à la partie. Les statistiques/données d'après match sont enregistrées et le traitement
	 * pour un quelconque classement est fait.<br><br>
	 * Cette fonction est utile pour les modes de jeu qui ne peuvent se finir seul (un mode de jeu sans fin par exemple).
	 * Cette fonction fonctionne en plusieurs étapes définies par les fonctions suivantes:<br>
	 * {@link Game#collectGameData computeGameResult}<br>
	 * {@link Game#end end}<br>
	 * {@link Game#asyncManageGameData asyncManageGameData}<br>
	 */
	public final boolean Stop() {
		if (!_isPlaying) return false;
		// collect game data
		var plugin = CorePlugin.getCorePluginInstance();
		try {
			collectGameData();
		}catch(Exception e) {
			var msg = "Une erreur est survenue lors de la recuperation des donnees de la partie";
			plugin.getLogger().log(Level.WARNING, msg, e);
		}
		// end phase
		try {
			end();
		}catch(Exception e) {
			var msg = "Une erreur est survenue lors de l'arret de la partie";
			plugin.getLogger().log(Level.SEVERE, msg, e);
			return false;
		}
		// manage game data
		try {
			final Logger log = plugin.getLogger();
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				try {
					asyncManageGameData();
				}catch(Exception e) {
					log.log(Level.WARNING, "Une erreur est survenue de façon asynchrone lors de la gestion des donnees de la partie", e);
				}
			});
		}catch(Exception e) {
			var msg = "Une erreur est survenue lors de la gestion des donnees de la partie";
			plugin.getLogger().log(Level.WARNING, msg, e);
		}
		UtilEvent.Unregister(this);
		_isPlaying = false;
		return true;
	}
	
	/**
	 * Met fin prématurément à la partie. Aucun traitement n'est fait sur les statistiques/données d'après match,
	 * aucun traitement pour un quelconque classement est fait. La partie "compte pour du beure".
	 */
	public final boolean Abort() {
		if (!_isPlaying) return false;
		var plugin = CorePlugin.getCorePluginInstance();
		try {
			end();
		}catch(Exception e) {
			var msg = "Une erreur est survenue lors de l'arret de la partie";
			plugin.getLogger().log(Level.SEVERE, msg, e);
			return false;
		}
		UtilEvent.Unregister(this);
		_isPlaying = false;
		return true;
	}
	
	/**
	 * Mise en place de la partie, abonnement aux services utilisés (pas besoin de s'abonner aux événements Bukkit) et début de celle-ci.
	 * Le chargement d'une sauvegarde ou de données peut aussi être fait ici (ou lancé de manière asynchrone depuis cette fonction)
	 * <br><br>Cette methode est appelée par {@link Game#Start Start}
	 */
	protected abstract void setup();
	
	/**
	 * Arret des mechaniques de jeu et désabonnement aux services utilisés. Affichage aux joueurs les résultats de la partie.
	 * <br><br>Cette methode est appelée par {@link Game#Stop Stop} et {@link Game#Abort Abort}
	 */
	protected abstract void end();
	
	/**
	 * Détermination d'un éventuel gagnant ou d'un score atteint. Récuperation des données pour les statistiques
	 * et les classements. Snapshot éventuelle de la partie pour la sauvegarder lors de {@link Game#asyncManageGameData asyncManageGameData}.
	 * <br><br>Cette methode est appelée par {@link Game#Stop Stop}
	 */
	protected abstract void collectGameData();
	
	/**
	 * Envoi <strong>asynchrone</strong> des statistiques vers les serveurs et mise à jour des classements.
	 * Cette phase peut aussi être utilisée pour sauvegarder de manière asynchrone la partie pour la relancer plus tard.
	 * <br><br><strong>Cette methode ne doit PAS utiliser l'API Bukkit.</strong>
	 * <br><br>Cette methode est appelée par {@link Game#Stop Stop}
	 */
	protected abstract void asyncManageGameData();

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
	
	public List<Player> getPlayerList(){
		List<Player> list = new LinkedList<>();
		for (GameTeam team : _teams) {
			list.addAll(team.getPlayerList());
		}
		return list;
	}
	
	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return getPlayerList();
	}
	

	
}
