package fr.nekotine.core.module;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.text.Text;
import fr.nekotine.core.util.UtilEvent;

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
	
	private ModuleManager _manager;
	
	/**
	 * Récupère le module souhaité par son nom.
	 * @param name Nom du module.
	 * @return Le module souhaité.
	 */
	public PluginModule GetPluginModule(String name) {
		return _manager.Get(_name);
	}
	
	/**
	 * Récupère le module souhaité par sa classe.
	 * @param moduleType Type du module.
	 * @return Le module souhaité.
	 */
	public <T extends PluginModule> T GetPluginModule(Class<T> moduleType) {
		return _manager.Get(moduleType);
	}
	
	/**
	 * Execute le runnable sur le prochain tick minecraft.
	 * @param runnable
	 */
	public void RunAsync(Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(_plugin, runnable);
	}
	
	/**
	 * Execute le runnable sur un thread séparé.
	 * @param runnable
	 */
	public void RunSync(Runnable runnable) {
		Bukkit.getScheduler().runTask(_plugin, runnable);
	}
	
	/**
	 * Affiche du texte informatif dans la console.
	 * @param text
	 */
	public void logInfo(String text) {
		_plugin.getLogger().info(Text.moduleLog(this,text));
	}
	
	/**
	 * Récupère le nom du module.
	 * @return
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Récupère le plugin.
	 * @return le plugin utilisant ce module.
	 */
	public JavaPlugin getPlugin() {
		return _plugin;
	}
	
	/**
	 * Met le plugin.
	 * @param plugin
	 */
	public void setPlugin(JavaPlugin plugin) {
		_plugin = plugin;
	}
	
	public ModuleManager getModuleManager() {
		return _manager;
	}
	
	public void setModuleManager(ModuleManager manager) {
		_manager = manager;
	}
	
	/**
	 * Méthode à appeler pour charger le module.
	 */
	public final void enable() {
		final long epoch = System.currentTimeMillis();
		logInfo("début du chargement...");
		onEnable();
		UtilEvent.Register(getPlugin(), this);
		logInfo(String.format("le module est chargé! (%d ms)", System.currentTimeMillis()-epoch));
	}
	
	/**
	 * Méthode à appeler pour décharger le module.
	 */
	public final void disable() {
		final long epoch = System.currentTimeMillis();
		logInfo("début du déchargement...");
		UtilEvent.Unregister(this);
		onDisable();
		logInfo(String.format("le module est déchargé! (%d ms)", System.currentTimeMillis()-epoch));
	}
	
	/**
	 * Méhode à ne pas appeler, utilisez plutôt {@link #enable()}
	 * Cette méthode est destinée à être surchargée par la classe enfant.
	 */
	protected void onEnable() {}
	
	/**
	 * Méhode à ne pas appeler, utilisez plutôt {@link #disbale()}
	 * Cette méthode est destinée à être surchargée par la classe enfant.
	 */
	protected void onDisable() {}
	
}
