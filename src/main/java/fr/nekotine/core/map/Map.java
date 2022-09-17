package fr.nekotine.core.map;

import fr.nekotine.core.map.component.MapComponent;

public abstract class Map extends MapComponent{
	
	private MapIdentifier _id;
	
	public Map(MapModule module, MapIdentifier id, String name) {
		super(module, null, id.name());
		_id = id;
	}
	
	public MapIdentifier getId() {
		return _id;
	}
}
