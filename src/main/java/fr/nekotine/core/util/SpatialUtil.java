package fr.nekotine.core.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import fr.nekotine.core.util.lambda.TriConsumer;

public class SpatialUtil {

	public static final void circle2DDensity(double centerX, double centerY, double centerZ, double radius, double density, TriConsumer<Double, Double, Double> consumer) {
		var perimeter = 2 * Math.PI * radius;
		var delta = perimeter / density;
		for(double theta = 0 ; theta < perimeter ; theta+=delta) {
			double x = centerX + (Math.cos(theta) * radius);
			double z = centerZ + (Math.sin(theta) * radius);
			consumer.accept(x, centerY, z);
		}
	}
	
	public static final void circle2DDensity(Vector center, double radius, double density, TriConsumer<Double, Double, Double> consumer) {
		circle2DDensity(center.getX(), center.getY(), center.getZ(), radius, density, consumer);
	}
	
	public static final void circle2DDensity(Location center, double radius, double density, TriConsumer<Double, Double, Double> consumer) {
		circle2DDensity(center.getX(), center.getY(), center.getZ(), radius, density, consumer);
	}
	
	public static final void circle2DNumber(double centerX, double centerY, double centerZ, double radius, int points, TriConsumer<Double, Double, Double> consumer) {
		var fullCircle = 2 * Math.PI;
		double span = fullCircle / points;
		for(double theta = 0 ; theta < fullCircle ; theta+=span) {
			double x = centerX + (Math.cos(theta) * radius);
			double z = centerZ + (Math.sin(theta) * radius);
			consumer.accept(x, centerY, z);
		}
	}
	
	public static final void circle2DNumber(Vector center, double radius, int points, TriConsumer<Double, Double, Double> consumer) {
		circle2DNumber(center.getX(), center.getY(), center.getZ(), radius, points, consumer);
	}
	
	public static final void circle2DNumber(Location center, double radius, int points, TriConsumer<Double, Double, Double> consumer) {
		circle2DNumber(center.getX(), center.getY(), center.getZ(), radius, points, consumer);
	}
	
}
