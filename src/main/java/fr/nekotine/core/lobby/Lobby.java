package fr.nekotine.core.lobby;

import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.minigame.Game;
import fr.nekotine.core.minigame.GameEventListener;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Lobby implements GameEventListener, ForwardingAudience{

	private final LobbyModule _module;
	
	private String _name;
	
	private GameModeIdentifier _gamemodeid;
	
	private int _playerCap = 10; //TODO ne pas oublier de le mettre a jour
	
	private List<Player> _players;
	
	private Game _game;
	
	public Lobby(LobbyModule module, String name) {
		_module = module;
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
	
	public void Remove() {
		if (_game != null && _game.isPlaying()) {
			_game.Abort();
		}
	}
	
	public boolean freeToJoin() {
		return (_players.size() < _playerCap && (_game == null || !_game.isPlaying()));
	}
	
	/**
	 * Ajoute le joueur au lobby sans tests au préalable.
	 * Cette fonction notifie les joueurs du lobby et le joueur ayant rejoin
	 * @param player
	 */
	public void AddPlayer(Player player) {
		sendMessage(Component.text(String.format("%s a rejoint le lobby", player.getName())).color(NamedTextColor.GRAY));
		_players.add(player);
		player.sendMessage(
				Component.text("Vous avez rejoint le lobby ").color(NamedTextColor.YELLOW)
				.append(MiniMessage.miniMessage().deserialize(_name)));
	}

	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return _players;
	}
	
}
