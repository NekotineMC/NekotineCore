package fr.nekotine.core.lobby;

import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import dev.jorel.commandapi.CommandAPI;
import fr.nekotine.core.game.Game;
import fr.nekotine.core.game.GameData;
import fr.nekotine.core.game.GameMode;
import fr.nekotine.core.game.GameModeModule;
import fr.nekotine.core.game.GameTeam;
import fr.nekotine.core.module.ModuleManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Un lobby englobant une Game.
 * 
 * On peut voir le lobby comme un "GameBuilder" car il permet aux joueurs de configurer la partie avant de la lancer.
 * 
 * Le lobby est un outil pour configurer une game et la lancer, il ne reçois de la game que
 * les événements spécifiés dans {@link fr.nekotine.core.minigame.GameHolder GameEventListener}
 * 
 * @author XxGoldenbluexX
 *
 */
public class Lobby implements ForwardingAudience{

	private LobbyModule _module;
	
	private boolean isRegistered = false;
	
	private String _name;
		
	@Nonnull
	private Game<? extends GameData> _game;
	
	public Lobby(String name) {
		_name = name;
		var gamemode = ModuleManager.GetModule(GameModeModule.class).getGameMode();
		if (gamemode == null) {
			Bukkit.getLogger().log(Level.WARNING, "Aucun mode de jeu n'a ete enregistre lors de la creation du lobby " + name);
		}else {
			setGameMode(gamemode);
		}
	}
	
	public Game<? extends GameData> getGame(){
		return _game;
	}
	
	public void setGameMode(GameMode<? extends GameData> gamemode) {
		if (_game != null && _game.isPlaying()) return;
		if (_game != null) {
			if (_game.isPlaying()) return;
			_game.destroy();
		}
		_game = gamemode.createGame(null);
	}
	
	public List<Player> getPlayerList(){
		return _game.getPlayerList();
	}
	
	/**
	 * Le nom du lobby peut être sous un format MiniMessage
	 * @param name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Le nom du lobby peut être sous un format MiniMessage
	 * @param name
	 */
	public void setName(String name) {
		_name = name;
	}
	
	public int getPlayerCap() {
		return _game.getPlayerCap();
	}
	
	/**
	 * Cette fonction est plus efficace que prendre la taille de la liste de joueur,
	 * car elle évite la construction d'une liste représentant tous les joueurs du lobby (la Game stock les joueurs séparément).
	 * 
	 * @return le nombre de joueurs dans le lobby
	 */
	public int getNumberOfPlayer() {
		return _game.getNumberOfPlayer();
	}
	
	public boolean isGameLaunched() {
		return _game.isPlaying();
	}
	
	/**
	 * 
	 * @param module
	 */
	public void register(LobbyModule module) {
		_module = module;
		_module.getLobbyList().add(this);
		Component msg = makeEasyJoinMessage();
		for (Player player : _module.getPlayersWithNoLobby()) {
			player.sendMessage(msg);
		}
		isRegistered = true;
	}
	
	/**
	 * Supprime le lobby de la liste des lobby après l'avoir vidé
	 */
	public void unregister() {
		_game.destroy();
		for (GameTeam team : _game.getTeams()) {
			team.clear();
		}
		_module.getLobbyList().remove(this);
		isRegistered = false;
	}
	
	public boolean isFreeToJoin() {
		return (getNumberOfPlayer() < getPlayerCap() && !_game.isPlaying());
	}
	
	/**
	 * Ajoute le joueur au lobby sans tests au préalable.
	 * Cette fonction notifie les joueurs du lobby et le joueur ayant rejoint
	 * @param player
	 */
	public void AddPlayer(Player player) {
		if (!isRegistered) return;
		// Notify players
		sendMessage(Component.text(String.format("▶ %s a rejoint le lobby", player.getName())).color(NamedTextColor.GRAY));
		player.sendMessage(
				Component.text("Vous avez rejoint le lobby ").color(NamedTextColor.YELLOW)
				.append(MiniMessage.miniMessage().deserialize(_name)));
		// Add player
		_game.addPlayerToOptimalTeam(player);
		// Update commands
		CommandAPI.updateRequirements(player);
	}
	
	/**
	 * Retire le joueur de la partie, s'il est dedans
	 * @param player
	 */
	public void RemovePlayer(Player player) {
		if (getPlayerList().contains(player)) {
			// Remove player
			_game.removePlayer(player);
			// Notify players
			sendMessage(Component.text(String.format("◀ %s a quitté le lobby", player.getName())).color(NamedTextColor.GRAY));
			player.sendMessage(
					Component.text("Vous avez quitté le lobby ").color(NamedTextColor.YELLOW)
					.append(MiniMessage.miniMessage().deserialize(_name)));
			// Update commands
			CommandAPI.updateRequirements(player);
		}
	}

	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return getPlayerList();
	}
	
	public Component makeEasyJoinMessage() {
		var prefix = Component.text(String.format("Le lobby (%s) ", _game.getGameMode().getRegisteringKey())).color(NamedTextColor.GREEN);
		var name = MiniMessage.miniMessage().deserialize(_name).clickEvent(null);
		var suffix = Component.text(String.format(" peut être rejoint [%d/%d]", getNumberOfPlayer(), getPlayerCap())).color(NamedTextColor.GREEN);
		var fin = prefix.append(name).append(suffix)
				.hoverEvent(HoverEvent.showText(Component.text("Cliquez pour rejoindre le lobby").color(NamedTextColor.GRAY)))
				.clickEvent(ClickEvent.runCommand(String.format("/lobby join \"%s\"", MiniMessage.miniMessage().stripTags(_name))));
		return fin;
	}
	
}
