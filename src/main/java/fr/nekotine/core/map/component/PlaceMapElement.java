package fr.nekotine.core.map.component;

import org.bukkit.Location;

import fr.nekotine.core.map.GameMap;

public class PlaceMapElement extends MapComponent implements MapElement {

	public PlaceMapElement(GameMap map, String name) {
		super(map, name);
	}

	private Location value;
	
	public Location getValue() {
		return value;
	}
	
	public void setValue(Location place) {
		value = place;
		System.out.println("Value set to "+ place.toString());
	}

}
