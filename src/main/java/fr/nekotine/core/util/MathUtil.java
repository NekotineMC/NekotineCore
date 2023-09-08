package fr.nekotine.core.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class MathUtil {
	
	
	
	public static Vector GetTrajectory2d(Entity from, Entity to) {
		return GetTrajectory2d(from.getLocation().toVector(), to.getLocation().toVector());
	}
	public static Vector GetTrajectory2d(Location from, Location to) {
		return GetTrajectory2d(from.toVector(), to.toVector());
	}
	public static Vector GetTrajectory2d(Vector from, Vector to) {
		return to.subtract(from).setY(0).normalize();
	}
	
	public static Vector GetTrajectory3d(Entity from, Entity to) {
		return GetTrajectory3d(from.getLocation().toVector(), to.getLocation().toVector());
	}
	public static Vector GetTrajectory3d(Location from, Location to) {
		return GetTrajectory3d(from.toVector(), to.toVector());
	}
	public static Vector GetTrajectory3d(Vector from, Vector to) {
		return to.subtract(from).normalize();
	}
	
	public static int clamp(int value, int min, int max) {
		if (min > max) throw new IllegalArgumentException();
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}
	
	public static float clamp(float value, float min, float max) {
		if (min > max) throw new IllegalArgumentException();
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}
	
	public static double clamp(double value, double min, double max) {
		if (min > max) throw new IllegalArgumentException();
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}
	
	public static long clamp(long value, long min, long max) {
		if (min > max) throw new IllegalArgumentException();
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}
	
}
