package fr.nekotine.core.map.element;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.tuple.Pair;
import fr.nekotine.core.tuple.Triplet;

public class MapBlockLocationElement implements ConfigurationSerializable{

	public MapBlockLocationElement() {}
	
	public MapBlockLocationElement(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public MapBlockLocationElement(Location location) {
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
	
	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Pair<Integer,Integer> getXY(){
		return new Pair<>(x,y);
	}
	
	public void setXZ(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	public Pair<Integer,Integer> getXZ(){
		return new Pair<>(x,z);
	}
	
	public void setYZ(int y, int z) {
		this.y = y;
		this.z = z;
	}
	
	public Pair<Integer,Integer> getYZ(){
		return new Pair<>(y,z);
	}
	
	public void setXYZ(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Triplet<Integer,Integer,Integer> getXYZ(){
		return new Triplet<>(x,y,z);
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		var map = new HashMap<String,Object>();
		map.put("x", x);
		map.put("y", y);
		map.put("z", z);
		return map;
	}
	
	public static MapBlockLocationElement deserialize(Map<String,Object> map) {
		var x = (int)map.get("x");
		var y = (int)map.get("y");
		var z = (int)map.get("z");
		return new MapBlockLocationElement(x, y, z);
	}
	
}
