package fr.nekotine.core.map.save.metadata;

import fr.nekotine.core.map.MapHandle;
import fr.nekotine.core.map.MapMetadata;

public interface IMapMetadataSaver {

	public <T> void saveMetadata(MapHandle<T> handle, MapMetadata metadata);
	
	public <T> MapMetadata loadMetadata(MapHandle<T> handle);
	
	public <T> boolean delete(MapHandle<T> handle);
	
}
