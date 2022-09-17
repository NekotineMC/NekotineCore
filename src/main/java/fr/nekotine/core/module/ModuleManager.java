package fr.nekotine.core.module;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.module.annotation.InheritedModuleAnnotation;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

/**
 * Ioc pour les modules (des genres de service quoi).
 * 
 * @author XxGoldenbluexX
 *
 */
public class ModuleManager{

	private static ModuleManager instance = new ModuleManager();
	
	public static ModuleManager getInstance() {
		if (instance == null) {
			instance = new ModuleManager();
		}
		return instance;
	}
	
	private ModuleManager() {};
	
	private Map<Class<? extends PluginModule>,PluginModule> modules = new HashMap<>();
	private Map<String, Class<? extends PluginModule>> nameMappings = new HashMap<>();
	
	public static void Load(JavaPlugin plugin, @SuppressWarnings("unchecked") Class<? extends PluginModule>... moduleTypes) {
		getInstance().load(plugin, moduleTypes);
	}
	
	@SuppressWarnings("unchecked")
	public void load(JavaPlugin plugin, Class<? extends PluginModule>... moduleTypes) {
		for (Class<? extends PluginModule> moduleType : moduleTypes) {
			for (Field field : moduleType.getDeclaredFields()) {
				InheritedModuleAnnotation annotation = field.getAnnotation(InheritedModuleAnnotation.class);
				if (annotation != null) {
					Class<?> fieldType = field.getType();
					if (PluginModule.class.isAssignableFrom(fieldType)) {
						if (!modules.containsKey(fieldType)) {
							tryLoadModule(plugin, (Class<? extends PluginModule>) fieldType);
						}
					}
				}
			}
			if (!modules.containsKey(moduleType)) {
				tryLoadModule(plugin, (Class<? extends PluginModule>) moduleType);
			}
		}
	}
	
	private void tryLoadModule(JavaPlugin plugin, Class<? extends PluginModule> moduleType) {
		try {
			PluginModule module = moduleType.getConstructor().newInstance();
			module.setModuleManager(this);
			module.setPlugin(plugin);
			modules.put(moduleType,module);
			ModuleNameAnnotation annotation = moduleType.getAnnotation(ModuleNameAnnotation.class);
			if (annotation != null) {
				module.setName(annotation.Name());
				nameMappings.put(annotation.Name(), moduleType);
			}
		} catch (Exception e) {
			plugin.getLogger().log(Level.WARNING,"Impossible de créer le module de type " + moduleType.getTypeName(), e);
		}
	}
	
	/**
	 * Récupère un module à partir de sa classe.
	 * @param <T> Type du module à récuperer.
	 * @param moduleType Type du module à récuperer.
	 * @return Le module désiré.
	 */
	public static <T extends PluginModule> T GetModule(Class<T> moduleType) {
		return getInstance().getModule(moduleType);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends PluginModule> T getModule(Class<T> moduleType) {
		return (T) modules.get(moduleType);
	}
	
	public PluginModule getAbstractModule(Class<? extends PluginModule> moduleType) {
		return modules.get(moduleType);
	}
	
	/**
	 * Récupère le module souhaité par son nom.
	 * @param name Nom du module.
	 * @return Le module souhaité.
	 */
	public static PluginModule GetModule(String name) {
		return getInstance().getModule(name);
	}
	
	/**
	 * Récupère le module souhaité par son nom.
	 * @param name Nom du module.
	 * @return Le module souhaité.
	 */
	public PluginModule getModule(String name) {
		Class<? extends PluginModule> clazz = nameMappings.get(name);
		if (clazz != null) {
			return modules.get(clazz);
		}
		return null;
	}
	
	/**
	 * Active tous les modules.
	 */
	public static void EnableAll() {
		getInstance().enableAll();
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
	public static void DisableAll() {
		getInstance().disableAll();
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
