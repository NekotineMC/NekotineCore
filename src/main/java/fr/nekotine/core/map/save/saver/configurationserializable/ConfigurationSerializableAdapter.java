package fr.nekotine.core.map.save.saver.configurationserializable;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

public class ConfigurationSerializableAdapter implements ConfigurationSerializable{
	
	private static final String contentKey = "content";
	
	private final ConfigurationSerializableAdapterSerializer saver = ConfigurationSerializableAdapterSerializer.getInstance();
	
	private Object content;
	
	public ConfigurationSerializableAdapter(Object content) {
		this.content = content;
	}
	
	public ConfigurationSerializableAdapter(Map<String, Object> map) {
		var mapTypeName = (String)map.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY);
		try {
			var mapType = Class.forName(mapTypeName);
			content = saver.getDeserializerFor(mapType).apply(map);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		var map = new HashMap<String, Object>();
		var mapType = content.getClass();
		map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, mapType.getName());
		if (content instanceof ConfigurationSerializable serializable) {
			map.put(contentKey, serializable.serialize());
		}else {
			map.put(contentKey, saver.getSerializerFor(mapType).apply(content));
		}
		return map;
	}
	
	public Object getContent() {
		return content;
	}
}
