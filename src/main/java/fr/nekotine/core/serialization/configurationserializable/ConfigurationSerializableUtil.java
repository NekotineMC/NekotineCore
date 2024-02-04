package fr.nekotine.core.serialization.configurationserializable;

import java.util.Map;

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
		return clazz.cast(ConfigurationSerialization.deserializeObject(sectionAsMap(section), clazz));
	}
	
	public static Map<String,Object> sectionAsMap(ConfigurationSection section){
		var map = section.getValues(false);
		for (var entry : map.entrySet()) {
			if (ConfigurationSection.class.isAssignableFrom(entry.getValue().getClass())) {
				entry.setValue(sectionAsMap((ConfigurationSection)entry.getValue()));
			}
		}
		return map;
	}
	
}
