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
		boolean confPresent = false;
		var confFile = new File(Ioc.resolve(JavaPlugin.class).getDataFolder(), pathInPluginFolder);
		if (confFile.exists()) {
			conf = YamlConfiguration.loadConfiguration(confFile);
			confPresent = true;
		}else {
			conf = new YamlConfiguration();
		}
		var o = conf.options();
		o.copyDefaults(true);
		o.parseComments(true);
		// load defaults
		Configuration defaultConfig;
		var res = Ioc.resolve(JavaPlugin.class).getResource(pathInJar);
		if (res == null) {
			throw new RuntimeException("Aucun fichier de config n'est définit dans le jar pour "+pathInJar);
		}
		try (var defaultReader = new InputStreamReader(res ,"UTF-8")){
			defaultConfig = YamlConfiguration.loadConfiguration(defaultReader);
		}
		conf.setDefaults(defaultConfig);
		// save
		conf.save(confFile);
		return confPresent?conf:defaultConfig;
	}
	
}
