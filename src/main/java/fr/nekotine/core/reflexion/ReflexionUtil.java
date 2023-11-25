package fr.nekotine.core.reflexion;

import java.io.IOException;
import java.util.stream.Stream;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.reflect.ClassPath;

import fr.nekotine.core.ioc.Ioc;

public class ReflexionUtil {
	
	public static Stream<Class<?>> streamJarClasses() throws IOException{
		var plugin = Ioc.resolve(JavaPlugin.class);
		return ClassPath.from(plugin.getClass().getClassLoader()).getAllClasses().stream().map(c -> c.load());
	}
}
