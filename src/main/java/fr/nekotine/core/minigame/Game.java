package fr.nekotine.core.minigame;

public class Game {

	private final GameEventListener _holder;
	
	private final GameMode _gamemode;
	
	private boolean _isPlaying = false;
	
	public Game(GameEventListener holder, GameMode gamemode) {
		_holder = holder;
		_gamemode = gamemode;
	}
	
	public Game(GameMode gamemode) {
		this(null, gamemode);
	}
	
	public boolean isPlaying() {
		return _isPlaying;
	}
	
	public void Start() {
		_isPlaying = true;
	}
	
	public void Stop() {
		
	}
	
	public void Abort() {
		
	}
	
}
