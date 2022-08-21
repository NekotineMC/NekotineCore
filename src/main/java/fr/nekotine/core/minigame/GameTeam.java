package fr.nekotine.core.minigame;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GameTeam implements ForwardingAudience{

	private Component _name;
	
	private List<Player> _players = new LinkedList<>();
	
	public List<Player> getPlayerList(){
		return _players;
	}
	
	public Component getName() {
		return _name;
	}
	
	public void setName(Component name) {
		_name = name;
	}
	
	/**
	 * Ajoute le joueur à l'équipe et le notifie.
	 * @param player
	 */
	public void addPlayer(Player player) {
		_players.add(player);
		player.sendMessage(
				Component.text("Vous avez rejoint l'équipe ").color(NamedTextColor.LIGHT_PURPLE)
				.append(_name));
	}
	
	/**
	 * Retire le joueur de l'équipe
	 */
	public void removePlayer(Player player) {
		// EN CAS DE MODIFICATION, PENSER A CHANGER LA METHODE CLEAR /!\
		_players.remove(player);
	}
	
	/**
	 * Vide l'équipe
	 */
	public void clear() {
		_players.clear();
	}

	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return _players;
	}
	
}
