package fr.nekotine.core.module;

import java.util.logging.Logger;

import fr.nekotine.core.logging.FormatingRemoteLogger;
import fr.nekotine.core.text.Text;

/**
 * Classe abstraite représentant un module utilisable par un plugin.
 * 
 * @author XxGoldenbluexX
 *
 */
public abstract class PluginModule {

	public Logger LOGGER = new FormatingRemoteLogger(Text.namedLoggerFormat(getClass().getSimpleName()));

	/**
	 * Méthode à appeler pour décharger le module.
	 */
	protected void unload() {};
}
