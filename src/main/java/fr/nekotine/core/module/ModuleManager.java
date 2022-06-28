package fr.nekotine.core.module;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

public class ModuleManager<PluginType extends JavaPlugin> {

	private Map<String,PluginModule<PluginType>> modules = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public void Load(JavaPlugin plugin, Class<? extends PluginModule<PluginType>>... moduleTypes) {
		for (Class<? extends PluginModule<PluginType>> moduleType : moduleTypes) {
			try {
				PluginModule<PluginType> module = moduleType.getConstructor(JavaPlugin.class).newInstance(plugin);
				modules.put(module.getName(),module);
			} catch (Exception e) {
				plugin.getLogger().warning("Impossible de créer le module de type " + moduleType.getTypeName() + '\n' + e.getMessage());
			}
		}
	}
	
	public PluginModule<PluginType> Get(String name) {
		return modules.get(name);
	}
	
	public void enableAll() {
		for (PluginModule<PluginType> module : modules.values()) {
			module.enable();
		}
	}
	
	public void disableAll() {
		for (PluginModule<PluginType> module : modules.values()) {
			module.disable();
		}
	}
	
}
