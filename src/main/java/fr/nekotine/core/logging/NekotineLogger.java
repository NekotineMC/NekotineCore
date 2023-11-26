package fr.nekotine.core.logging;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.PluginModule;

public class NekotineLogger extends Logger{

	private final String prefix;

	public NekotineLogger(Class<?> clazz) {
		super(loggerName(clazz), null);
		var pre = clazz.getTypeName();
		if (!PluginModule.class.isAssignableFrom(clazz)) {
			pre = clazz.getSimpleName();
		}
		prefix = '[' + pre + "] > ";
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

}
