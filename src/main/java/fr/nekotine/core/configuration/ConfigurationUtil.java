package fr.nekotine.core.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.ioc.Ioc;

public class ConfigurationUtil {

	public static Configuration updateAndLoadYaml(String pathInJar, String pathInPluginFolder) throws IOException {
		YamlConfiguration conf;
		var confFile = new File(Ioc.resolve(JavaPlugin.class).getDataFolder(), pathInPluginFolder);
		if (confFile.exists()) {
			conf = YamlConfiguration.loadConfiguration(confFile);
		}else {
			conf = new YamlConfiguration();
		}
		// load defaults
		Configuration defaultConfig;
		try (var defaultReader = new InputStreamReader(Ioc.resolve(JavaPlugin.class).getResource(pathInJar),"UTF-8")){
			defaultConfig = YamlConfiguration.loadConfiguration(defaultReader);
		}
		conf.setDefaults(defaultConfig);
		// save
		conf.save(confFile);
		return conf;
	}
	
}
