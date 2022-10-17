package fr.nekotine.core.map;

import fr.nekotine.core.map.component.MapComponent;
import fr.nekotine.core.module.ModuleManager;

public abstract class GameMap extends MapComponent{
	
	private MapIdentifier _id;
	
	public GameMap(MapIdentifier type) {
		super(null,type.name());
		_id = type;
	}
	
	public MapIdentifier getId() {
		return _id;
	}
	
	public void save() {
		ModuleManager.GetModule(MapModule.class).saveMapInstance(this);
	}
	
	public void saveAsync() {
		ModuleManager.GetModule(MapModule.class).saveMapInstanceAsync(this);
	}
}
