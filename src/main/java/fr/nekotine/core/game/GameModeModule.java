package fr.nekotine.core.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

/**
 * 
 * @author XxGoldenbluexX
 *
 */
@ModuleNameAnnotation(Name = "GameModeModule")
public class GameModeModule extends PluginModule{

	private Map<String, GameMode<? extends GameData>> registeredGameModes = new HashMap<>();
	
	/**
	 * Ajoute un GameMode à la lste des GameMode disponibles.
	 * @param id
	 * @param gamemode
	 * @return
	 */
	public boolean registerGameMode(String id, GameMode<? extends GameData> gamemode) {
		if (registeredGameModes.containsKey(id)) {
			log(Level.WARNING, String.format("Tentative d'enregistrer un GameMode avec l'id %s plusieurs fois.", id));
			return false;
		}else {
			registeredGameModes.put(id, gamemode);
			log(Level.WARNING, String.format("Un GameMode à été enregistré avec l'id %s.", id));
			return true;
		}
	}
	
	/**
	 * Retourne le GameMode enregistré avec cette clef, null s'il n'y en a pas.
	 * @param key
	 * @return
	 */
	public @Nullable GameMode<? extends GameData> getGameMode(String key){
		return registeredGameModes.get(key);
	}
	
	/**
	 * Retourne un GameMode, null s'il n'y en a pas.
	 * @return
	 */
	public @Nullable GameMode<? extends GameData> getGameMode(){
		return registeredGameModes.values().stream().findAny().orElse(null);
	}
	
	public Set<String> getGameModesKeys(){
		return registeredGameModes.keySet();
	}
	
	/**
	 * Retourne la clef par lequel ce GameMode a été enregistré.
	 * @param gamemode
	 * @return
	 */
	public String getGameModeKey(GameMode<? extends GameData> gamemode) {
		for (var key : registeredGameModes.keySet()) {
			if (registeredGameModes.get(key) == gamemode) {
				return key;
			}
		}
		return "";
	}
	
}
