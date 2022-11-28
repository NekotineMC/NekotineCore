package fr.nekotine.core.game;

import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.Nullable;

/**
 * Classe qui stock les données de la partie.
 * 
 * Dans cette classe se trouve les données sauvegardables.
 * 
 * @author XxGoldenbluexX
 *
 */
public abstract class GameData implements ConfigurationSerializable{
	
	public GameData(@Nullable Map<String, Object> flattenedgameData){
		
	}
	
}
