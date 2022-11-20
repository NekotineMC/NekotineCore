package fr.nekotine.core.game;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Classe qui stock les données de la partie.
 * 
 * Dans cette classe se trouve les équipes et les données sauvegardables.
 * 
 * @author XxGoldenbluexX
 *
 */
public abstract class GameData implements ConfigurationSerializable{

	private transient List<GameTeam> teams = new LinkedList<>();
	
	public GameData(Map<String, Object> objectMap) {
		registerTeams(teams);
	}
	
	public List<GameTeam> getTeams(){
		return teams;
	}
	
	/**
	 * Cette fonction est appelée à la création de la partie pour mettre en place les équipes.
	 * @param teamList liste des équipes a completer.
	 */
	public abstract void registerTeams(List<GameTeam> teamList);
	
}
