package fr.nekotine.core.reflexion;

import java.util.stream.Stream;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.reflect.ClassPath;

import fr.nekotine.core.ioc.Ioc;

public class ReflexionUtil {
	
	public static Stream<Class<?>> streamClassesFromPackage(String packageName) throws Exception{
		var pluginClass = Ioc.resolve(JavaPlugin.class).getClass();
		return ClassPath.from(pluginClass.getClassLoader()).getAllClasses().stream()
				.filter(i -> i.getName().toLowerCase().startsWith(packageName))
				.map(c -> c.load());
	}
}
