package fr.nekotine.core.logging;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.util.ReflexionUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public class NekotineLogger extends Logger{

	private final String prefix;

	private NekotineLogger() {
		this(ReflexionUtil.getCallingClass(),ReflexionUtil.getCallingClassName());
	}
	
	private NekotineLogger(Class<?> clazz) {
		this(clazz,nameFromClass(clazz));
	}
	
	private NekotineLogger(Class<?> clazz, String name) {
		super(loggerName(clazz), null);
		prefix = '('+ name + ") > ";
		setParent(Ioc.resolve(Logger.class));
	}
	
	@Override
	public void log(LogRecord record) {
		record.setMessage(prefix + record.getMessage());
		super.log(record);
	}
	
	private static final String loggerName(Class<?> clazz) {
		if (clazz.getPackageName().startsWith("fr.nekotine.core")) {
			return "NekotineCore";
		}
		return Ioc.resolve(JavaPlugin.class).getName();
	}
	
	private static String nameFromClass(Class<?> clazz) {
		
		if (IPluginModule.class.isAssignableFrom(clazz)) {
			return clazz.getTypeName();
		}else {
			return clazz.getSimpleName();
		}
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
	
}
