package fr.nekotine.core.lobby;

import java.util.LinkedList;
import java.util.List;

import fr.nekotine.core.minigame.Game;
import net.kyori.adventure.text.Component;

public abstract class GameMode {

	// Static part
	
	private static final List<GameMode> AVAILABLE_GAMEMODES = new LinkedList<>();
	
	public static void registerGameMode(GameMode gamemode) {
		if (!AVAILABLE_GAMEMODES.contains(gamemode)) {
			AVAILABLE_GAMEMODES.add(gamemode);
		}
	}
	
	public static List<GameMode> getGameModeList(){
		return AVAILABLE_GAMEMODES;
	}
	
	// -----
	
	private final String _id;
	
	private final Component _name;
	
	public GameMode(String id, Component name) {
		_id = id;
		_name = name;
	}
	
	public String getId() {
		return _id;
	}
	
	public Component getName() {
		return _name;
	}
	
	public abstract Game generateTypedGame();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GameMode) {
			GameMode gm = (GameMode)obj;
			return gm._id.equals(_id);
		}
		return false;
	}
}
