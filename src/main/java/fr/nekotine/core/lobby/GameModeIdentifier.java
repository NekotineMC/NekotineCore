package fr.nekotine.core.lobby;

import java.util.LinkedList;
import java.util.List;

import fr.nekotine.core.minigame.GameMode;
import net.kyori.adventure.text.Component;

public abstract class GameModeIdentifier {

	// Static part
	
	private static final List<GameModeIdentifier> AVAILABLE_GAMEMODES = new LinkedList<>();
	
	public static void registerGameMode(GameModeIdentifier gamemode) {
		if (!AVAILABLE_GAMEMODES.contains(gamemode)) {
			AVAILABLE_GAMEMODES.add(gamemode);
		}
	}
	
	public static List<GameModeIdentifier> getGameModeList(){
		return AVAILABLE_GAMEMODES;
	}
	
	// -----
	
	private final String _id;
	
	private final Component _name;
	
	public GameModeIdentifier(String id, Component name) {
		_id = id;
		_name = name;
	}
	
	public String getId() {
		return _id;
	}
	
	public Component getName() {
		return _name;
	}
	
	public abstract GameMode generateGameMode();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GameModeIdentifier) {
			GameModeIdentifier gm = (GameModeIdentifier)obj;
			return gm._id.equals(_id);
		}
		return false;
	}
}
