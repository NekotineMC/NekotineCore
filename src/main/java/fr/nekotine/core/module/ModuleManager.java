package fr.nekotine.core.module;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

public class ModuleManager{

	private Map<Class<? extends PluginModule>,PluginModule> modules = new HashMap<>();
	private Map<String, Class<? extends PluginModule>> nameMappings = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public void Load(JavaPlugin plugin, Class<? extends PluginModule>... moduleTypes) {
		for (Class<? extends PluginModule> moduleType : moduleTypes) {
			try {
				PluginModule module = moduleType.getConstructor().newInstance();
				module.setModuleManager(this);
				module.setPlugin(plugin);
				modules.put(moduleType,module);
				ModuleNameAnnotation annotation = moduleType.getAnnotation(ModuleNameAnnotation.class);
				if (annotation != null) {
					nameMappings.put(annotation.Name(), moduleType);
				}
			} catch (Exception e) {
				plugin.getLogger().warning("Impossible de créer le module de type " + moduleType.getTypeName() + '\n' + e.getMessage());
			}
		}
	}
	
	/**
	 * Récupère un module à partir de sa classe.
	 * @param <T> Type du module à récuperer.
	 * @param moduleType Type du module à récuperer.
	 * @return Le module désiré.
	 */
	@SuppressWarnings("unchecked")
	public <T extends PluginModule> T Get(Class<T> moduleType) {
		return (T) modules.get(moduleType);
	}
	/**
	 * Récupère le module souhaité par son nom.
	 * @param name Nom du module.
	 * @return Le module souhaité.
	 */
	public PluginModule Get(String name) {
		Class<? extends PluginModule> clazz = nameMappings.get(name);
		if (clazz != null) {
			return modules.get(clazz);
		}
		return null;
	}
	
	/**
	 * Active tous les modules.
	 */
	public void enableAll() {
		for (PluginModule module : modules.values()) {
			module.enable();
		}
	}
	
	/**
	 * Désactive tous les modules.
	 */
	public void disableAll() {
		for (PluginModule module : modules.values()) {
			module.disable();
		}
	}
	
}
