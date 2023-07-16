package fr.nekotine.core.map.save;

import fr.nekotine.core.map.MapMetadata;

public interface IMapSaver {

	public void save(MapMetadata identifier, Object map);
	
	public Object load(MapMetadata identifier);
	
	public boolean delete(MapMetadata identifier);
	
}
