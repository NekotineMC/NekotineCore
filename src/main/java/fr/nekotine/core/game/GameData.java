package fr.nekotine.core.game;

import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Classe qui stock les données de la partie.
 * 
 * Dans cette classe se trouve les données sauvegardables.
 * 
 * @author XxGoldenbluexX
 *
 */
public abstract class GameData implements ConfigurationSerializable{
	
	public GameData(Map<String, Object> flattenedGameData) {
	}

}
