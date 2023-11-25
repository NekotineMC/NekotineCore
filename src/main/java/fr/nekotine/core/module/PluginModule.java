package fr.nekotine.core.module;

import java.util.logging.Logger;

import fr.nekotine.core.logging.NekotineLogger;

/**
 * Classe abstraite représentant un module utilisable par un plugin.
 * 
 * @author XxGoldenbluexX
 *
 */
public abstract class PluginModule{
	
	protected Logger logger = new NekotineLogger(getClass());

	/**
	 * Méthode à appeler pour décharger le module.
	 */
	protected void unload() {}
}
