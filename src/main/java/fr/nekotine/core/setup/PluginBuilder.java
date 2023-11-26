package fr.nekotine.core.setup;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.NekotinePlugin;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.map.MapModule;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.reflexion.ReflexionUtil;

public class PluginBuilder {

	private JavaPlugin plugin;
	
	private Set<Class<? extends PluginModule>> preloadModules = new HashSet<>();
	
	private Set<Class<?>> mapTypesForCommand = new HashSet<>();
	
	private final Logger logger;
	
	public PluginBuilder(JavaPlugin plugin) {
		this.plugin = plugin;
		setupIoc();
		logger = new NekotineLogger(PluginBuilder.class);
	}
	
	private void setupIoc() {
		var ioc = Ioc.getProvider(); // Use default IIocProvider
		// Register some defaults
		ioc.registerSingleton(plugin);
		ioc.registerSingletonAs(plugin, JavaPlugin.class);
		ioc.registerSingletonAs(plugin.getLogger(), Logger.class);
	}
	
	@SafeVarargs
	public final void preloadModules(Class<? extends PluginModule> ... modules) {
		for (var module : modules) {
			preloadModules.add(module);
		}
	}
	
	@SafeVarargs
	public final void mapCommandsFor(Class<?> ... mapTypes) {
		for (var mapType : mapTypes) {
			mapTypesForCommand.add(mapType);
		}
	}
	
	public final NekotinePlugin build() {
		setupModules();
		setupMapCommands();
		return new NekotinePlugin();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupModules() {
		try {
			var moduleManager = new ModuleManager();
			Ioc.getProvider().registerSingleton(moduleManager);
			var allModuleClasses = ReflexionUtil.streamJarClasses()
					.filter(c -> PluginModule.class.isAssignableFrom(c))
					.collect(Collectors.toSet());
			for (var mc : allModuleClasses) {
				Ioc.getProvider().registerSingletonAs((Supplier)() -> moduleManager.get((Class<? extends PluginModule>) mc), mc);
			}
			for (var module : preloadModules) {
				moduleManager.tryLoad(module);
			}
		}catch(Exception e) {
			logger.log(Level.SEVERE, "Errur lors de la mise en place des modules", e);
		}
	}
	
	private void setupMapCommands() {
		if (mapTypesForCommand.isEmpty()) {
			return;
		}
		var gen = Ioc.resolve(MapModule.class).getGenerator();
		gen.generateFor(mapTypesForCommand.toArray(Class<?>[]::new));
		gen.register();
	}
	
}
