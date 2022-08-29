package fr.nekotine.core.module;

import java.lang.reflect.Field;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.module.annotation.InheritedModuleAnnotation;
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
	 * Execute le runnable sur le thread server au prochain tick
	 * @param runnable
	 */
	public void RunSync(Runnable runnable) {
		Bukkit.getScheduler().runTask(_plugin, runnable);
	}
	
	/**
	 * Affiche du texte informatif dans la console.
	 * @param text
	 */
	public void log(Level level, String text) {
		_plugin.getLogger().log(level,Text.moduleLog(this,text));
	}
	
	/**
	 * Affiche du texte informatif dans la console.
	 * @param text
	 */
	public void logException(Level level, String text, Exception e) {
		_plugin.getLogger().log(level, Text.moduleLog(this,text), e);
	}
	
	/**
	 * Récupère le nom du module.
	 * @return
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Définit le nom du module pour son affichage dans le chat.
	 * @param name Le nom du module.
	 */
	public void setName(String name) {
		_name = name;
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
		log(Level.FINE,"debut du chargement...");
		try {
			onEnable();
		}catch(Exception e) {
			logException(Level.WARNING, "Une erreur est survenue lors du chargement du module", e);
		}
		log(Level.FINE, String.format("le module est charge! (%d ms)", System.currentTimeMillis()-epoch));
	}
	
	/**
	 * Méthode à appeler pour décharger le module.
	 */
	public final void disable() {
		final long epoch = System.currentTimeMillis();
		log(Level.FINE,"debut du dechargement...");
		try {
			onDisable();
		}catch(Exception e) {
			logException(Level.WARNING, "Une erreur est survenue lors du dechargement du module", e);
		}
		log(Level.FINE, String.format("le module est decharge! (%d ms)", System.currentTimeMillis()-epoch));
	}
	
	/**
	 * Méhode à ne pas appeler, utilisez plutôt {@link #enable()}
	 * Cette méthode est destinée à être surchargée par la classe enfant.
	 */
	protected void onEnable() {
		// loading dependecies
		try {
			for (Field field : getClass().getDeclaredFields()) {
				InheritedModuleAnnotation annotation = field.getAnnotation(InheritedModuleAnnotation.class);
				if (annotation != null) {
					if (PluginModule.class.isAssignableFrom(field.getType())) {
						if (!field.canAccess(this)) {
							field.setAccessible(true);
						}
						try {
							field.set(this, _manager.Get(field.getType()));
						} catch (Exception e) {
						}
					}
				}
			}
		}catch(Exception e) {
			logException(Level.WARNING, "Une erreur est survenue lors de la recuperation des dependances de ce module", e);
		}
		//
		UtilEvent.Register(_plugin, this);
	}
	
	/**
	 * Méhode à ne pas appeler, utilisez plutôt {@link #disbale()}
	 * Cette méthode est destinée à être surchargée par la classe enfant.
	 */
	protected void onDisable() {
		UtilEvent.Unregister(this);
	}
	
}
