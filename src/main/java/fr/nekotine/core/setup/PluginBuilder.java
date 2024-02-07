package fr.nekotine.core.setup;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.NekotinePlugin;
import fr.nekotine.core.defaut.DefaultProvider;
import fr.nekotine.core.defaut.IDefaultProvider;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.map.OldMapModule;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.reflexion.ReflexionUtil;
import fr.nekotine.core.serialization.configurationserializable.ConfigurationSerializableAdapterSerializer;
import fr.nekotine.core.serialization.configurationserializable.IConfigurationSerializableAdapterContainer;

public class PluginBuilder {

	private JavaPlugin plugin;
	
	private Set<Class<? extends IPluginModule>> preloadModules = new HashSet<>();
	
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
		ioc.registerSingletonAs(new DefaultProvider(), IDefaultProvider.class);
		// Serialization
		ioc.registerSingletonAs((Supplier<ConfigurationSerializableAdapterSerializer>)ConfigurationSerializableAdapterSerializer::new,
				IConfigurationSerializableAdapterContainer.class);
		
	}
	
	@SafeVarargs
	public final void preloadModules(Class<? extends IPluginModule> ... modules) {
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
		setupConfiguration();
		return new NekotinePlugin();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupModules() {
		try {
			var moduleManager = new ModuleManager();
			Ioc.getProvider().registerSingleton(moduleManager);
			// Nekotine Core Modules
			var allCoreModuleClasses = ReflexionUtil.streamClassesFromPackage("fr.nekotine.core")
					.filter(c -> IPluginModule.class.isAssignableFrom(c))
					.collect(Collectors.toSet());
			for (var mc : allCoreModuleClasses) {
				if (mc.isInterface()) {
					var impl = allCoreModuleClasses.stream().filter(c -> mc.isAssignableFrom(c)).findAny();
					if (impl.isPresent()) {
						var implType = impl.get();
						Ioc.getProvider().registerSingletonAs((Supplier)() -> moduleManager.get((Class<? extends IPluginModule>) implType), mc);
						logger.info(String.format("Module %s ajouté dans l'IOC en tant que %s", implType.getSimpleName(),mc.getSimpleName()));
					}
				}else {
					Ioc.getProvider().registerSingletonAs((Supplier)() -> moduleManager.get((Class<? extends IPluginModule>) mc), mc);
					logger.info(String.format("Module %s ajouté dans l'IOC", mc.getSimpleName()));
				}
			}
			for (var module : preloadModules) {
				moduleManager.tryLoad(module);
			}
		}catch(Exception e) {
			logger.log(Level.SEVERE, "Erreur lors de la mise en place des modules", e);
		}
	}
	
	private void setupMapCommands() {
		if (mapTypesForCommand.isEmpty()) {
			return;
		}
		var gen = Ioc.resolve(OldMapModule.class).getGenerator();
		gen.generateFor(mapTypesForCommand.toArray(Class<?>[]::new));
		gen.register();
	}
	
	private void setupConfiguration() {
		plugin.saveDefaultConfig();
		Ioc.getProvider().registerTransientAs(plugin::getConfig, Configuration.class);
	}
	
}
