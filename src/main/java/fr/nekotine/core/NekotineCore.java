package fr.nekotine.core;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.ioc.IIocProvider;
import fr.nekotine.core.ioc.IocProvider;
import fr.nekotine.core.logging.FormatingRemoteLogger;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.text.Text;

public final class NekotineCore {
	
	public static final IIocProvider IOC = new IocProvider();
	
	public static final ModuleManager MODULES = new ModuleManager();
	
	public static final Logger LOGGER = new FormatingRemoteLogger(Text.namedLoggerFormat("NekotineCore"));
	
	public static JavaPlugin getAttachedPlugin() {
		if (attachedPlugin == null) {
			throw new NullPointerException("Le plugin n'a pas encore été attaché avec NekotineCore.setupFor(JavaPlugin plugin)");
		}
		return attachedPlugin;
	}
	
	public static <T extends JavaPlugin> T getAttachedPluginAs(Class<T> type) {
		if (attachedPlugin == null) {
			throw new NullPointerException("Le plugin n'a pas encore été attaché avec NekotineCore.setupFor(JavaPlugin plugin)");
		}
		return type.cast(attachedPlugin);
	}
	
	private static JavaPlugin attachedPlugin;
	
	public static final void setupFor(JavaPlugin plugin) {
		NekotineCore.attachedPlugin = plugin;
		((FormatingRemoteLogger)LOGGER).setRemote(plugin.getLogger());
		((FormatingRemoteLogger)MODULES.LOGGER).setRemote(LOGGER);
	}
}
