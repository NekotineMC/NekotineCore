package fr.nekotine.core.map.save.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import fr.nekotine.core.map.MapHandle;

public interface IMapConfigSaver {

	public <T extends ConfigurationSerializable> void save(MapHandle<T> handle, ConfigurationSerializable map);
	
	public <T extends ConfigurationSerializable> T load(MapHandle<T> handle);
	
	public <T extends ConfigurationSerializable> boolean delete(MapHandle<T> identifier);
	
}
