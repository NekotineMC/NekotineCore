package fr.nekotine.core.map.command;

public interface IMapCommandGenerator {

	/**
	 * Génère les commandes d'édition pour les types de map donné.
	 * Génère aussi les commandes de map générales
	 * @param mapTypes
	 */
	public void generateFor(Class<?> ...mapTypes);
	
	public void register();
	
}
