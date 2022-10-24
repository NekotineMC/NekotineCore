package fr.nekotine.core.map.component;

import org.bukkit.Location;

import fr.nekotine.core.map.GameMap;

public class MapPlaceElement extends MapComponent implements MapElement {

	public MapPlaceElement(GameMap map, String name) {
		super(map, name);
	}

	private Location value;
	
	public Location getValue() {
		return value;
	}
	
	public void setValue(Location place) {
		value = place;
	}

}
