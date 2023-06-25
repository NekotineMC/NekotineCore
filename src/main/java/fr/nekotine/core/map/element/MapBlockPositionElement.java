package fr.nekotine.core.map.element;

import org.bukkit.Location;
import org.bukkit.World;

public class MapBlockPositionElement{

	public MapBlockPositionElement() {}
	
	public MapBlockPositionElement(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public MapBlockPositionElement(Location location) {
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();
	}
	
	public Location toLocation(World world) {
		return new Location(world, x, y, z);
	}
	
	public void fromLocation(Location location) {
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();
	}
	
	private int x;
	
	private int y;
	
	private int z;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}
	
}
