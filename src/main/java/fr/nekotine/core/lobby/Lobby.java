package fr.nekotine.core.lobby;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import dev.jorel.commandapi.CommandAPI;
import fr.nekotine.core.minigame.Game;
import fr.nekotine.core.minigame.GameEventListener;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Lobby implements GameEventListener, ForwardingAudience{

	private LobbyModule _module;
	
	private String _name;
	
	private GameModeIdentifier _gamemodeid;
	
	private int _playerCap = 10; //TODO ne pas oublier de le mettre a jour
	
	private List<Player> _players;
	
	private Game _game;
	
	private boolean isRegistered = false;
	
	public Lobby(String name) {
		_name = name;
		_gamemodeid = GameModeIdentifier.getGameModeList().get(0);
	}
	
	public List<Player> getPlayerList(){
		return _players;
	}
	
	public int getPlayerCap() {
		return _playerCap;
	}
	
	public GameModeIdentifier getGameModeId() {
		return _gamemodeid;
	}
	
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
	
	public Game getGame() {
		return _game;
	}
	
	/**
	 * 
	 * @param module
	 */
	public void register(LobbyModule module) {
		_module = module;
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
		if (_game != null && _game.isPlaying()) {
			_game.Abort();
		}
		for (Player player : new LinkedList<Player>(_players)) {
			RemovePlayer(player);
		}
		_module.getLobbyList().remove(this);
		isRegistered = false;
	}
	
	public boolean isFreeToJoin() {
		return (_players.size() < _playerCap && (_game == null || !_game.isPlaying()));
	}
	
	/**
	 * Ajoute le joueur au lobby sans tests au préalable.
	 * Cette fonction notifie les joueurs du lobby et le joueur ayant rejoint
	 * @param player
	 */
	public void AddPlayer(Player player) {
		if (!isRegistered) return;
		sendMessage(Component.text(String.format("▶ %s a rejoint le lobby", player.getName())).color(NamedTextColor.GRAY));
		_players.add(player);
		player.sendMessage(
				Component.text("Vous avez rejoint le lobby ").color(NamedTextColor.YELLOW)
				.append(MiniMessage.miniMessage().deserialize(_name)));
		CommandAPI.updateRequirements(player);
	}
	
	/**
	 * Retire le joueur de la partie, s'il est dedans
	 * @param player
	 */
	public void RemovePlayer(Player player) {
		if (_players.contains(player)) {
			_players.remove(player);
			sendMessage(Component.text(String.format("◀ %s a quitté le lobby", player.getName())).color(NamedTextColor.GRAY));
			player.sendMessage(
					Component.text("Vous avez quitté le lobby ").color(NamedTextColor.YELLOW)
					.append(MiniMessage.miniMessage().deserialize(_name)));
			CommandAPI.updateRequirements(player);
		}
	}

	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return _players;
	}
	
	public Component makeEasyJoinMessage() {
		Component prefix = Component.text("Le lobby ").color(NamedTextColor.GREEN);
		Component name = MiniMessage.miniMessage().deserialize(_name);
		Component suffix = Component.text(String.format(" peut être rejoint [%d/%d]", _players.size(), _playerCap)).color(NamedTextColor.GREEN);
		Component fin = prefix.append(name).append(suffix);
		fin.hoverEvent(HoverEvent.showText(Component.text("Cliquez pour rejoindre le lobby").color(NamedTextColor.GRAY)));
		fin.clickEvent(ClickEvent.runCommand("lobby join " + name));
		return fin;
	}
	
}
