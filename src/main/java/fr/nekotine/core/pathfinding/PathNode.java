package fr.nekotine.core.pathfinding;

import org.bukkit.Location;

public class PathNode {

	private final Location location;
	
	private final double cost;
	
	public PathNode(Location location, double cost) {
		this.location = location;
		this.cost = cost;
	}
	
	public PathNode(Location location) {
		this.location = location;
		this.cost = 1;
	}
	
	public PathNode(PathNode other) {
		this.location = other.location.toLocation(other.location.getWorld());
		this.cost = other.cost;
	}

	public Location getLocation() {
		return location;
	}
	
	public double getCost() {
		return cost;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof PathNode other) {
			return location.equals(other.location);
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		if (location == null) {
			return super.hashCode();
		}
		return location.hashCode();
	}
	
}
