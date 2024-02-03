package fr.nekotine.core.serialization.configurationserializable;

import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class ConfigurationSerializableUtil {

	
	public static void setFromObject(ConfigurationSection section, ConfigurationSerializable object) {
		var map = object.serialize();
		for (var key : map.keySet()) {
			section.set(key, map.get(key));
		}
	}
	
	public static <T extends ConfigurationSerializable> T getObjectFrom(ConfigurationSection section, Class<T> clazz) {
		var map = new HashMap<String,Object>();
		for (var key : section.getKeys(false)) {
			map.put(key, section.get(key));
		}
		return clazz.cast(ConfigurationSerialization.deserializeObject(map, clazz));
	}
	
}
