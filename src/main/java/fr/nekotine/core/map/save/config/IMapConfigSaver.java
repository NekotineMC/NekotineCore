package fr.nekotine.core.map.save.config;

import fr.nekotine.core.map.MapHandle;

public interface IMapConfigSaver {

	public <T> void save(MapHandle<T> handle, Object map);
	
	public <T> T load(MapHandle<T> handle);
	
	public <T> boolean delete(MapHandle<T> identifier);
	
}
