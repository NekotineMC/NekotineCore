package fr.nekotine.core.game;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.game.event.GameStartEvent;
import fr.nekotine.core.plugin.CorePlugin;

/**
 * 
 * @author XxGoldenbluexX
 *
 * @param <GD> Type de GameData
 */
public abstract class GameMode<GD extends GameData> implements Listener
{
	
	private Map<String, GamePhase<GD,? extends GameMode<GD>>> _gamePhases = new HashMap<>();
	
	private GamePhase<GD,? extends GameMode<GD>> currentGamePhase;
	
	private JavaPlugin plugin;
	
	public GameMode(JavaPlugin plugin){
		this.plugin = plugin;
		registerGamePhases(_gamePhases);
	}
	
	public JavaPlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Cette fonction est appelée à la création de la partie pour mettre en place les différentes phases de jeu.
	 * @param _gamePhasesMap
	 */
	public abstract void registerGamePhases(Map<String, GamePhase<GD, ? extends GameMode<GD>>> _gamePhasesMap);
	
	/**
	 * Changement de phase pour la phase indiquée par la clef donnée en argument.
	 * @param phaseKey la clef pointant vers la phase voulue
	 */
	public void GotoGamePhase(Game<GD> game, String phaseKey){
		var phase = _gamePhases.get(phaseKey);
		if (phase != null) {
			if (currentGamePhase != null) {
				try {
					currentGamePhase.End(game);
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
				currentGamePhase.Begin(game);
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
	public abstract void GotoFirstPhase(Game<GD> game);
	
	public GamePhase<GD,? extends GameMode<GD>> getCurrentGamePhase() {
		return currentGamePhase;
	}
	
	/**
	 * Démarre la partie.
	 * @param plugin Une référence au plugin
	 * @return Si oui ou non le démarrage est un succes.
	 */
	public final boolean Start(Game<GD> game) {
		if (game.isPlaying()) return true;
		var plugin = CorePlugin.getCorePluginInstance();
		try {
			globalSetup(game);
			for (var team : game.getTeams()) {
				for (var player : team.getPlayerList()) {
					playerSetup(game, player, team);
				}
			}
		}catch(Exception e) {
			var msg = "Une erreur est survenue lors du demarrage de la partie";
			plugin.getLogger().log(Level.SEVERE, msg, e);
			return false;
		}
		GotoFirstPhase(game);
		game.setIsPlaying(true);
		var startEvent = new GameStartEvent(game);
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
	public final boolean Stop(Game<GD> game) {
		if (!game.isPlaying()) return false;
		// collect game data
		var plugin = CorePlugin.getCorePluginInstance();
		try {
			collectGameData(game);
		}catch(Exception e) {
			var msg = "Une erreur est survenue lors de la recuperation des donnees de la partie";
			plugin.getLogger().log(Level.WARNING, msg, e);
		}
		// end phase
		try {
			currentGamePhase.End(game);
			for (var team : game.getTeams()) {
				for (var player : team.getPlayerList()) {
					playerEnd(game, player, team);
				}
			}
			globalEnd(game);
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
					asyncManageGameData(game);
				}catch(Exception e) {
					log.log(Level.WARNING, "Une erreur est survenue de façon asynchrone lors de la gestion des donnees de la partie", e);
				}
			});
		}catch(Exception e) {
			var msg = "Une erreur est survenue lors de la gestion des donnees de la partie";
			plugin.getLogger().log(Level.WARNING, msg, e);
		}
		game.setIsPlaying(false);
		return true;
	}
	
	/**
	 * Met fin prématurément à la partie. Aucun traitement n'est fait sur les statistiques/données d'après match,
	 * aucun traitement pour un quelconque classement est fait. La partie "compte pour du beure".
	 */
	public final boolean Abort(Game<GD> game) {
		if (!game.isPlaying()) return false;
		var plugin = CorePlugin.getCorePluginInstance();
		try {
			currentGamePhase.End(game);
			for (var team : game.getTeams()) {
				for (var player : team.getPlayerList()) {
					playerEnd(game, player, team);
				}
			}
			globalEnd(game);
		}catch(Exception e) {
			var msg = "Une erreur est survenue lors de l'arret de la partie";
			plugin.getLogger().log(Level.SEVERE, msg, e);
			return false;
		}
		game.setIsPlaying(false);
		return true;
	}
	
	/**
	 * Mise en place de la partie, abonnement aux services utilisés (pas besoin de s'abonner aux événements Bukkit) et début de celle-ci.
	 * Le chargement d'une sauvegarde ou de données peut aussi être fait ici (ou lancé de manière asynchrone depuis cette fonction)
	 * <br><br>Cette methode est appelée par {@link Game#Start Start}
	 */
	protected abstract void globalSetup(Game<GD> game);
	
	/**
	 * Mise en place de chaque joueur avant le début de la partie.
	 * Cette methode est appelée pour chaque joueur.
	 * Le chargement d'une sauvegarde ou de données peut aussi être fait ici (ou lancé de manière asynchrone depuis cette fonction)
	 * <br><br>Cette methode est appelée par {@link Game#Start Start}
	 */
	protected abstract void playerSetup(Game<GD> game, Player player, GameTeam team);
	
	/**
	 * Arret des mechaniques de jeu et désabonnement aux services utilisés.
	 * <br><br>Cette methode est appelée par {@link Game#Stop Stop} et {@link Game#Abort Abort}
	 */
	protected abstract void globalEnd(Game<GD> game);
	
	/**
	 * Arret des mechaniques de jeu pour chaque joueur.
	 * Cette methode est appelée pour chaque joueur.
	 * <br><br>Cette methode est appelée par {@link Game#Stop Stop} et {@link Game#Abort Abort}
	 */
	protected abstract void playerEnd(Game<GD> game, Player player, GameTeam team);
	
	/**
	 * Détermination d'un éventuel gagnant ou d'un score atteint. Récuperation des données pour les statistiques
	 * et les classements. Snapshot éventuelle de la partie pour la sauvegarder lors de {@link Game#asyncManageGameData asyncManageGameData}.
	 * <br><br>Cette methode est appelée par {@link Game#Stop Stop}
	 */
	protected abstract void collectGameData(Game<GD> game);
	
	/**
	 * Envoi <strong>asynchrone</strong> des statistiques vers les serveurs et mise à jour des classements.
	 * Cette phase peut aussi être utilisée pour sauvegarder de manière asynchrone la partie pour la relancer plus tard.
	 * <br><br><strong>Cette methode ne doit PAS utiliser l'API Bukkit.</strong>
	 * <br><br>Cette methode est appelée par {@link Game#Stop Stop}
	 */
	protected abstract void asyncManageGameData(Game<GD> game);
	
	/**
	 * Créée une game pour ce mode de jeu.
	 * 
	 * @return
	 */
	public abstract Game<GD> createGame(@Nullable Map<String, Object> flattenedGameData);

	protected void addPlayerToOptimalTeam(Game<GD> game, Player player) {
		if (!game.containsPlayer(player)) {
			game.getTeams().get(0).addPlayer(player);
		}
	}

	/**
	 * Methode appelée juste après le retrait d'un joueur.
	 * Cette methode est utile pour arrêter la partie ou équilibrer les équipes si la partie est en cours.
	 * @param game la partie.
	 * @param player le joueur ayant quitter.
	 */
	public abstract void onPlayerPostLeave(Game<GD> game, Player player);
	
	/**
	 * Cette fonction est appelée à la création de la partie pour mettre en place les équipes.
	 * @param teamList liste des équipes a completer.
	 */
	public abstract void registerTeams(Game<GD> game);
	
}
