package fr.nekotine.core.map.save;

import fr.nekotine.core.map.MapIdentifier;

public interface IMapSaver {

	public void save(MapIdentifier identifier, Object map);
	
	public Object load(MapIdentifier identifier);
	
}
