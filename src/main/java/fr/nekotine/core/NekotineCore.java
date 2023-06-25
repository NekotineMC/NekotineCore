package fr.nekotine.core;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.ioc.IIocProvider;
import fr.nekotine.core.ioc.IocProvider;
import fr.nekotine.core.logging.FormatingRemoteLogger;
import fr.nekotine.core.map.IMapStorage;

public final class NekotineCore {
	
	public static final IIocProvider IOC = new IocProvider()
			.registerSingletonAs(null, IMapStorage.class);
	
	public static final Logger LOGGER = new FormatingRemoteLogger("[NekotineCore] %s");
	
	public static final void setupFor(JavaPlugin plugin) {
		((FormatingRemoteLogger)LOGGER).setRemote(plugin.getLogger());
	}
	
}
