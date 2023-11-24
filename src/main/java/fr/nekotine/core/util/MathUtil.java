package fr.nekotine.core.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.joml.Matrix3f;
import org.joml.Vector3f;

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
	
	/**
	 * 
	 * @param xAngle Angle en radians autour de l'axe x
	 * @param yAngle Angle en radians autour de l'axe y
	 * @param zAngle Angle en radians autour de l'axe z
	 * @return
	 */
	public static Matrix3f getRotationMatrix(float xAngle, float yAngle, float zAngle) {
		var cx = (float)Math.cos(xAngle);
		var sx = (float)Math.sin(xAngle);
		var cy = (float)Math.cos(yAngle);
		var sy = (float)Math.sin(yAngle);
		var cz = (float)Math.cos(zAngle);
		var sz = (float)Math.sin(zAngle);
		var xMat = new Matrix3f(1, 0, 0, 0, cx, sx, 0, -sx, cx);
		var yMat = new Matrix3f(cy, 0, -sy, 0, 1, 0, sy, 0, cy);
		var zMat = new Matrix3f(cz, sz, 0, -sz, cz, 0, 0, 0, 1);
		return zMat.mul(yMat).mul(xMat);
	}
	
	public static Vector3f rotate(Vector3f vector, float xAngle, float yAngle, float zAngle) {
		return vector.mul(getRotationMatrix(xAngle, yAngle, zAngle));
	}
}
