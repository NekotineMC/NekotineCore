package fr.nekotine.core.serialization.configurationserializable;

import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.ioc.Ioc;

public interface ConfigurationSerializableAdapted extends ConfigurationSerializable{

	public static ConfigurationSerializableAdapted deserialize(Map<String,Object> map) {
		var deserializerSource = Ioc.resolve(IConfigurationSerializableAdapterContainer.class);
		return (ConfigurationSerializableAdapted)deserializerSource.getDeserializerFor(ConfigurationSerializableAdapted.class).apply(map);
	}
	
	@Override
	default @NotNull Map<String, Object> serialize() {
		var serializerSource = Ioc.resolve(IConfigurationSerializableAdapterContainer.class);
		return serializerSource.getSerializerFor(getClass()).apply(this);
	}
	
}
