package fr.nekotine.core.map;

import fr.nekotine.core.map.component.MapComponent;

public abstract class Map extends MapComponent{
	
	private MapIdentifier _id;
	
	public Map(MapIdentifier type) {
		super(null,type.name());
		_id = type;
	}
	
	public MapIdentifier getType() {
		return _id;
	}
	
	public void load() {
		
	}
	
	public void save() {
		
	}
}
