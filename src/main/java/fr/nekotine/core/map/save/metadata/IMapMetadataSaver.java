package fr.nekotine.core.map.save.metadata;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import fr.nekotine.core.map.MapHandle;
import fr.nekotine.core.map.MapMetadata;

public interface IMapMetadataSaver {

	public <T extends ConfigurationSerializable> void saveMetadata(MapHandle<T> handle, MapMetadata metadata);
	
	public <T extends ConfigurationSerializable> MapMetadata loadMetadata(MapHandle<T> handle);
	
	public <T extends ConfigurationSerializable> boolean delete(MapHandle<T> handle);
	
}
