package fr.nekotine.core.module;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

public class ModuleManager{

	private Map<String,PluginModule> modules = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public void Load(JavaPlugin plugin, Class<? extends PluginModule>... moduleTypes) {
		for (Class<? extends PluginModule> moduleType : moduleTypes) {
			try {
				PluginModule module = moduleType.getConstructor(JavaPlugin.class).newInstance(plugin);
				modules.put(module.getName(),module);
			} catch (Exception e) {
				plugin.getLogger().warning("Impossible de créer le module de type " + moduleType.getTypeName() + '\n' + e.getMessage());
			}
		}
	}
	
	/**
	 * Récupère le module souhaité par son nom.
	 * @param name Nom du module.
	 * @return Le module souhaité.
	 */
	public PluginModule Get(String name) {
		return modules.get(name);
	}
	
	public void enableAll() {
		for (PluginModule module : modules.values()) {
			module.enable();
		}
	}
	
	public void disableAll() {
		for (PluginModule module : modules.values()) {
			module.disable();
		}
	}
	
}
