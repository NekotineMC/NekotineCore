package fr.nekotine.core.map.element;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.tuple.Pair;
import fr.nekotine.core.tuple.Triplet;

/**
 * Une élément de carte qui représente un point désigné par une valeur sur trois axes (x,y,z).
 * La précision sur chaque axe est celle d'un double.
 * @author XxGoldenbluexX
 *
 */
public class MapPositionElement implements ConfigurationSerializable{

	public MapPositionElement() {}
	
	public MapPositionElement(double x, double y, double z, float yaw, float pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	public MapPositionElement(Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
	}
	
	public Location toLocation(World world) {
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	public void fromLocation(Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		yaw = location.getYaw();
		pitch = location.getPitch();
	}
	
	private float yaw;
	
	private float pitch;
	
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
	
	public void setXY(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Pair<Double,Double> getXY(){
		return new Pair<>(x,y);
	}
	
	public void setXZ(double x, double z) {
		this.x = x;
		this.z = z;
	}
	
	public Pair<Double,Double> getXZ(){
		return new Pair<>(x,z);
	}
	
	public void setYZ(double y, double z) {
		this.y = y;
		this.z = z;
	}
	
	public Pair<Double,Double> getYZ(){
		return new Pair<>(y,z);
	}
	
	public void setXYZ(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Triplet<Double,Double,Double> getXYZ(){
		return new Triplet<>(x,y,z);
	}
	
	public float getYaw() {
		return yaw;
	}
	
	public void setYaw(float yaw) {
		this.yaw = yaw;
	}
	
	public float getPitch() {
		return pitch;
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		var map = new HashMap<String,Object>();
		map.put("x", x);
		map.put("y", y);
		map.put("z", z);
		map.put("yaw", yaw);
		map.put("pitch", pitch);
		return map;
	}
	
	public static MapPositionElement deserialize(Map<String,Object> map) {
		var x = (double)map.get("x");
		var y = (double)map.get("y");
		var z = (double)map.get("z");
		var yaw = (double)map.get("yaw");
		var pitch = (double)map.get("pitch");
		return new MapPositionElement(x, y, z, (float)yaw, (float)pitch);
	}
	
}
