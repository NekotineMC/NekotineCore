package fr.nekotine.core.minigame;

public interface GameHolder {

	/**
	 * Événement lancé juste après le démarrage de la partie.
	 * 
	 * @param game
	 */
	public void onGameStart(Game game);
	
	/**
	 * Événement lancé quand la partie s'arrête, peut importe le type d'arret.
	 * 
	 * @param game
	 */
	public void onGameStop(Game game);
	
	/**
	 * Retourne le nom du GameHolder utilisé pour représenter la game dans les logs
	 * 
	 * @return nom dans les logs
	 */
	public String logName();
	
}
