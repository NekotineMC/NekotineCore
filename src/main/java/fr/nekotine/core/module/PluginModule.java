package fr.nekotine.core.module;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.text.Text;

/**
 * Classe abstraite représentant un module utilisable par un plugin.
 * Le module reçois, gère et peut creér de nouveaux évenements Bukkit.
 * 
 * @author XxGoldenbluexX
 *
 */
public abstract class PluginModule implements Listener {

	private String _name;
	
	private JavaPlugin _plugin;
	
	public PluginModule(JavaPlugin plugin, String name) {
		_plugin = plugin;
		_name = name;
	}
	
	public void RunAsync(Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(_plugin, runnable);
	}
	
	public void RunSync(Runnable runnable) {
		Bukkit.getScheduler().runTask(_plugin, runnable);
	}
	
	public void logInfo(String text) {
		_plugin.getLogger().info(Text.moduleLog(this,text));
	}
	
	public String getName() {
		return _name;
	}
	
	/**
	 * Méthode à appeler pour charger le module.
	 */
	public void enable() {
		final long epoch = System.currentTimeMillis();
		logInfo("début du chargement...");
		onEnable();
		logInfo(String.format("le module est chargé! (%d ms)", System.currentTimeMillis()-epoch));
	}
	
	/**
	 * Méthode à appeler pour décharger le module.
	 */
	public void disable() {
		final long epoch = System.currentTimeMillis();
		logInfo("début du déchargement...");
		onEnable();
		logInfo(String.format("le module est déchargé! (%d ms)", System.currentTimeMillis()-epoch));
	}
	
	/**
	 * Méhode à ne pas appeler, utilisez plutôt {@link #enable()}
	 * Cette méthode est destinée à être surchargée par la classe enfant.
	 */
	public void onEnable() {}
	
	/**
	 * Méhode à ne pas appeler, utilisez plutôt {@link #disbale()}
	 * Cette méthode est destinée à être surchargée par la classe enfant.
	 */
	public void onDisable() {}
	
}
