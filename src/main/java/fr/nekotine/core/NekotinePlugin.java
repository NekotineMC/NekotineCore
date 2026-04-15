package fr.nekotine.core;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import fr.nekotine.core.defaut.DefaultProvider;
import fr.nekotine.core.defaut.IDefaultProvider;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.map.command.IMapCommandGenerator;
import fr.nekotine.core.map.command.MapCommandGenerator;
import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.reflexion.ReflexionUtil;
import fr.nekotine.core.serialization.configurationserializable.ConfigurationSerializableAdapterSerializer;
import fr.nekotine.core.serialization.configurationserializable.IConfigurationSerializableAdapterContainer;
import fr.nekotine.core.util.DebugUtil;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

public class NekotinePlugin extends JavaPlugin {

	private final ComponentLogger nekotinePluginLogger = NekotineLogger.make(NekotinePlugin.class);

	@Override
	public void onLoad() {
		super.onLoad();
		CommandAPI.onLoad(new CommandAPIPaperConfig(this).setNamespace("vi6"));
		setupIoc();
		setupConfiguration();
		setupModules();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		CommandAPI.onEnable();
	}

	@Override
	public void onDisable() {
		DebugUtil.clearDebugEntities();
		Ioc.resolve(ModuleManager.class).unloadAll();
		CommandAPI.onDisable();
		super.onDisable();
	}

	private void setupIoc() {
		var ioc = Ioc.getProvider(); // Use default IIocProvider
		// Register some defaults
		ioc.registerSingleton(this);
		ioc.registerSingletonInstanceAs(this, JavaPlugin.class);
		ioc.registerSingletonInstanceAs(this.getLogger(), Logger.class);
		ioc.registerSingletonInstanceAs(this.getComponentLogger(), ComponentLogger.class);
		ioc.registerSingletonInstanceAs(new DefaultProvider(), IDefaultProvider.class);
		// Serialization
		ioc.registerSingletonAs(ConfigurationSerializableAdapterSerializer::new,
				IConfigurationSerializableAdapterContainer.class);
		// Some services
		ioc.registerSingletonAs(MapCommandGenerator::new, IMapCommandGenerator.class);
	}

	@SuppressWarnings("unchecked")
	private void setupModules() {
		try {
			var moduleManager = new ModuleManager();
			Ioc.getProvider().registerSingleton(moduleManager);
			// Nekotine Core Modules
			var allCoreModuleClasses = ReflexionUtil.streamClassesFromPackage("fr.nekotine.core")
					.filter(c -> IPluginModule.class.isAssignableFrom(c) && !c.equals(IPluginModule.class))
					.collect(Collectors.toSet());
			for (var mc : allCoreModuleClasses) {
				if (mc.isInterface()) {
					var impl = allCoreModuleClasses.stream().filter(c -> mc.isAssignableFrom(c) && !c.isInterface())
							.findAny();
					if (impl.isPresent()) {
						var implType = impl.get();
						Ioc.getProvider().registerSingletonAs(
								(Supplier) () -> moduleManager.get((Class<? extends IPluginModule>) implType), mc);
						nekotinePluginLogger.info(String.format("Module %s ajouté dans l'IOC en tant que %s",
								implType.getSimpleName(), mc.getSimpleName()));
					}
				} else {
					Ioc.getProvider().registerSingletonAs(
							(Supplier) () -> moduleManager.get((Class<? extends IPluginModule>) mc), mc);
					nekotinePluginLogger.info(String.format("Module %s ajouté dans l'IOC", mc.getSimpleName()));
				}
			}
		} catch (Exception e) {
			nekotinePluginLogger.error("Erreur lors de la mise en place des modules", e);
		}
	}

	@SafeVarargs
	public final void loadModules(Class<? extends IPluginModule>... modules) {
		var moduleManager = Ioc.resolve(ModuleManager.class);
		for (var module : modules) {
			moduleManager.tryLoad(module);
		}
	}

	public final void mapCommandsFor(Class<?>... mapTypes) {
		var gen = Ioc.resolve(IMapCommandGenerator.class);
		gen.generateFor(mapTypes);
		gen.register();
	}

	private void setupConfiguration() {
		saveDefaultConfig();
		Ioc.getProvider().registerTransientAs(this::getConfig, Configuration.class);
	}
}
