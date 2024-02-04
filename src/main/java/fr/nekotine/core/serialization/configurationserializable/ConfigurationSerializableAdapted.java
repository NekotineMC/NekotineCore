package fr.nekotine.core.serialization.configurationserializable;

import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.ioc.Ioc;

public abstract class ConfigurationSerializableAdapted implements ConfigurationSerializable{
	
	public ConfigurationSerializableAdapted(Map<String,Object> map) {
		var deserializerSource = Ioc.resolve(IConfigurationSerializableAdapterContainer.class);
		deserializerSource.getDeserializerApplierFor(getClass()).apply(this, map);
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		var serializerSource = Ioc.resolve(IConfigurationSerializableAdapterContainer.class);
		return serializerSource.getSerializerFor(getClass()).apply(this);
	}
	
}
