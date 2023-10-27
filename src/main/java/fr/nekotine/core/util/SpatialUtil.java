package fr.nekotine.core.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.util.Vector;

import fr.nekotine.core.util.lambda.TriConsumer;

public class SpatialUtil {

	public static final void circle2DDensity(double radius, double blockDensity, double rotationOffset,BiConsumer<Double, Double> consumer) {
		var perimeter = 2 * Math.PI * radius;
		var span = 1/blockDensity;
		for(double theta = rotationOffset ; theta < perimeter + rotationOffset; theta+=span) {
			double x = (Math.cos(theta) * radius);
			double y = (Math.sin(theta) * radius);
			consumer.accept(x, y);
		}
	}
	
	public static final void circle2DNumber(double radius, int nbPoints, double rotationOffset, BiConsumer<Double, Double> consumer) {
		var fullCircle = 2 * Math.PI;
		var span = fullCircle / nbPoints;
		for(double theta = rotationOffset ; theta < fullCircle + rotationOffset ; theta+=span) {
			double x = (Math.cos(theta) * radius);
			double y = (Math.sin(theta) * radius);
			consumer.accept(x, y);
		}
	}
	
	public static final void sphere3DDensity(double radius, double blockDensity, TriConsumer<Double, Double, Double> consumer) {
		var nbPointsOuter = 2 * Math.PI * radius * blockDensity;
		var deltaTheta = (2 * Math.PI) / nbPointsOuter;
		for(double theta = 0 ; theta < Math.PI ; theta += deltaTheta) {
			
			double y = Math.cos(theta) * radius;
			double outerRadius = Math.sin(theta);
			var nbPointsInner = nbPointsOuter * outerRadius;
			var deltaPhi = (2 * Math.PI) / nbPointsInner;
			for(double phi = 0 ; phi < 2 * Math.PI ; phi += deltaPhi) {
				double x = radius * Math.cos(phi) * outerRadius;
				double z = radius * Math.sin(phi) * outerRadius;
				consumer.accept(x, y, z);
			}
		}
	}

	public static final void sphere3DNumber(double radius, int nbCircles, int nbPoints, TriConsumer<Double, Double, Double> consumer) {
		for(double theta = 0 ; theta < Math.PI ; theta += Math.PI / nbCircles) {
			double y = Math.cos(theta) * radius;
			for(double phi = 0 ; phi < 2 * Math.PI ; phi += (2 * Math.PI) / nbPoints) {
				double x = radius * Math.cos(phi) * Math.sin(theta);
				double z = radius * Math.sin(phi) * Math.sin(theta);
				consumer.accept(x, y, z);
			}
		}
	}
	
	public static final void ball3DDensity(double radius, double blockDensity, TriConsumer<Double, Double, Double> consumer) {
		var span = 1 / blockDensity;
		for(double r = 0 ; r <= radius ; r += span) {
			sphere3DDensity(r, blockDensity, consumer);
		}
	}
	
	public static final void ball3DNumber(double radius, int nbRadius, int nbCircles, int nbPoints, TriConsumer<Double, Double, Double> consumer) {
		var span = radius / nbRadius;
		for(double r = 0 ; r <= radius ; r += span) {
			sphere3DNumber(r, nbCircles, nbPoints, consumer);
		}
	}

	public static final void line3DFromDir(Vector start,
			Vector direction, double distance, double blockDensity,
			TriConsumer<Double, Double, Double> consumer) {
		if (!direction.isNormalized()) {
			direction.normalize();
		}
		var dist = 1/blockDensity;
		direction.multiply(dist);
		for (double theta = 0; theta < distance; theta += dist) {
			consumer.accept(start.getX(), start.getY(), start.getZ());
			start.add(direction);
		}
	}
	
	public static final void line3DFromDir(double startX, double startY, double startZ,
			Vector direction, double distance, double blockDensity,
			TriConsumer<Double, Double, Double> consumer) {
		line3DFromDir(new Vector(startX, startY, startZ), direction, distance, blockDensity, consumer);
	}
	
	public static final void line3DFromDir(double startX, double startY, double startZ,
			double dirx, double diry, double dirz, double distance, double blockDensity,
			TriConsumer<Double, Double, Double> consumer) {
		line3DFromDir(new Vector(startX, startY, startZ), new Vector(dirx, diry, dirz), distance, blockDensity, consumer);
	}
	
	public static final void line3DFromDir(Vector start,
			Vector direction, double distance, double blockDensity,
			Consumer<Vector> consumer) {
		if (!direction.isNormalized()) {
			direction.normalize();
		}
		var dist = 1/blockDensity;
		direction.multiply(dist);
		for (double theta = 0; theta < distance; theta += dist) {
			consumer.accept(start);
			start.add(direction);
		}
	}
	
	public static final void line3DFromDir(double startX, double startY, double startZ,
			Vector direction, double distance, double blockDensity,
			Consumer<Vector> consumer) {
		line3DFromDir(new Vector(startX, startY, startZ), direction, distance, blockDensity, consumer);
	}
	
	public static final void line3DFromDir(double startX, double startY, double startZ,
			double dirx, double diry, double dirz, double distance, double blockDensity,
			Consumer<Vector> consumer) {
		line3DFromDir(new Vector(startX, startY, startZ), new Vector(dirx, diry, dirz), distance, blockDensity, consumer);
	}
	
}
