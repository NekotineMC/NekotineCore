package fr.nekotine.core.logging;

import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.util.ReflexionUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public class NekotineLogger{

	private static final String loggerName(Class<?> clazz) {
		if (clazz.getPackageName().startsWith("fr.nekotine.core")) {
			return "NekotineCore";
		}
		// Le plugin n'est pas accessible lors de la création
		if (JavaPlugin.class.isAssignableFrom(clazz)) {
			var plugin = Ioc.getProvider().tryResolve(JavaPlugin.class);
			if (plugin.isPresent()) {
				return plugin.get().getName();
			}else {
				make().warn("Le NekotineLogger est créé avant que l'Ioc ai une référence au Plugin. Le nom du plugin sera donc incorrect. Si ce logger est créé pour un JavaPlugin, vous pouvez utiliser make(JavaPlugin) pour contourner le problème.");
				return clazz.getSimpleName();
			}
		}
		return Ioc.resolve(JavaPlugin.class).getName();
	}

	public static ComponentLogger make() {
		var clazz = ReflexionUtil.getCallingClass();
		return make(clazz);
	}
	
	public static ComponentLogger make(Class<?> clazz) {
		return make(clazz, clazz.getSimpleName());
	}
	
	public static ComponentLogger make(String name) {
		var clazz = ReflexionUtil.getCallingClass();
		return make(clazz, name);
	}
	
	public static ComponentLogger make(Class<?> clazz, String name) {
		return ComponentLogger.logger(loggerName(clazz) + " > (" + name +')');
	}
	
	public static ComponentLogger make(JavaPlugin plugin) {
		return ComponentLogger.logger(plugin.getName() + " > (" + plugin.getClass().getSimpleName() +')');
	}
	
	public static ComponentLogger make(JavaPlugin plugin, String name) {
		return ComponentLogger.logger(plugin.getName() + " > (" + name +')');
	}
	
}
