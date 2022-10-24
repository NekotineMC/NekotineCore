package fr.nekotine.core.map.component;

import org.bukkit.Location;

import fr.nekotine.core.map.GameMap;

public class MapRectangleAreaElement extends MapComponent implements MapElement {

	public MapRectangleAreaElement(GameMap map, String name) {
		super(map, name);
	}

	private Location corner1;
	
	private Location corner2;
	
	public Location getCorner1() {
		return corner1;
	}
	
	public Location getCorner2() {
		return corner2;
	}
	
	public void setCorner1(Location loc) {
		corner1 = loc;
	}
	
	public void setCorner2(Location loc) {
		corner2 = loc;
	}

}
