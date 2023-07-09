package fr.nekotine.core.map.element;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Une élément de carte qui représente un point désigné par une valeur sur trois axes (x,y,z).
 * La précision sur chaque axe est celle d'un double.
 * @author XxGoldenbluexX
 *
 */
public class MapPositionElement implements ConfigurationSerializable{

	public MapPositionElement() {}
	
	public MapPositionElement(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public MapPositionElement(Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
	}
	
	public Location toLocation(World world) {
		return new Location(world, x, y, z);
	}
	
	public void fromLocation(Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
	}
	
	private double x;
	
	private double y;
	
	private double z;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		var map = new HashMap<String,Object>();
		map.put("x", x);
		map.put("y", y);
		map.put("z", z);
		return map;
	}
	
	public static MapPositionElement deserialize(Map<String,Object> map) {
		var x = (double)map.get("x");
		var y = (double)map.get("y");
		var z = (double)map.get("z");
		return new MapPositionElement(x, y, z);
	}
	
}
