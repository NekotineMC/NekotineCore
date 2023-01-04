package fr.nekotine.core.game.exception;

public class GamePhaseException extends RuntimeException{

	public GamePhaseException(String msg) {
		super(msg);
	}
	
	public GamePhaseException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
