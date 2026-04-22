package fr.nekotine.core.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import fr.nekotine.core.util.lambda.TriConsumer;

public class SpatialUtil {
	public enum SphereAlgorithm {
		UNIFORM, FIBONACCI
	}

	//

	public static final void circle2DDensity(double radius, double blockDensity, double rotationOffset,
			BiConsumer<Double, Double> consumer) {
		var nbPoints = Math.round(2 * Math.PI * radius * blockDensity);
		var deltaTheta = (2 * Math.PI) / nbPoints;
		for (double theta = rotationOffset; theta < rotationOffset + 2 * Math.PI; theta += deltaTheta) {
			double x = Math.cos(theta) * radius;
			double y = Math.sin(theta) * radius;
			consumer.accept(x, y);
		}
	}

	public static final void circle2DNumber(double radius, int nbPoints, double rotationOffset,
			BiConsumer<Double, Double> consumer) {
		var fullCircle = 2 * Math.PI;
		var span = fullCircle / nbPoints;
		for (double theta = rotationOffset; theta < fullCircle + rotationOffset; theta += span) {
			double x = (Math.cos(theta) * radius);
			double y = (Math.sin(theta) * radius);
			consumer.accept(x, y);
		}
	}

	public static final void disk2DDensity(double radius, double blockDensity, BiConsumer<Double, Double> consumer) {
		var rand = new Random();
		var dens = Math.PI * radius * radius * blockDensity;
		for (var step = 0; step <= dens; step++) {
			var dist = Math.sqrt(rand.nextDouble());
			var angle = radius * rand.nextDouble();
			consumer.accept(Math.cos(angle) * dist, Math.sin(angle) * dist);
		}
	}

	//

	public static final void sphere3DDensity(double radius, double blockDensity, SphereAlgorithm algorithm,
			TriConsumer<Double, Double, Double> consumer) {
		switch (algorithm) {
			case UNIFORM :
				sphere3DDensityUniform(radius, blockDensity, consumer);
			case FIBONACCI :
				sphere3DDensityFibonacci(radius, blockDensity, consumer);
			default :
				return;
		}
	}

	private static final void sphere3DDensityUniform(double radius, double blockDensity,
			TriConsumer<Double, Double, Double> consumer) {
		var nbPointsOuter = Math.round(2 * Math.PI * radius * blockDensity);
		var deltaTheta = (2 * Math.PI) / nbPointsOuter;
		for (double theta = 0; theta < Math.PI; theta += deltaTheta) {

			double y = Math.cos(theta) * radius;
			double outerRadius = Math.sin(theta);
			var nbPointsInner = Math.round(nbPointsOuter * outerRadius);
			var deltaPhi = (2 * Math.PI) / nbPointsInner;
			for (double phi = 0; phi < 2 * Math.PI; phi += deltaPhi) {
				double x = radius * Math.cos(phi) * outerRadius;
				double z = radius * Math.sin(phi) * outerRadius;
				consumer.accept(x, y, z);
			}
		}
	}

	private static final void sphere3DDensityFibonacci(double radius, double blockDensity,
			TriConsumer<Double, Double, Double> consumer) {
		double surface = 4 * Math.PI * radius * radius;
		long nbPoints = Math.round(surface * blockDensity);
		double phi = Math.PI * (Math.sqrt(5) - 1);
		for (int i = 0; i < nbPoints; i++) {
			double y = 1 - (i / (float) (nbPoints - 1)) * 2;
			double innerRadius = Math.sqrt(1 - y * y);
			double theta = phi * i;
			double x = Math.cos(theta) * innerRadius;
			double z = Math.sin(theta) * innerRadius;
			consumer.accept(x * radius, y * radius, z * radius);
		}
	}

	/**
	 * While Sphere is only the surface, ball also takes the inside
	 *
	 * @param radius
	 * @param blockDensity
	 * @param algorithm
	 * @param consumer
	 */
	public static final void ball3DDensity(double radius, double blockDensity, SphereAlgorithm algorithm,
			TriConsumer<Double, Double, Double> consumer) {
		switch (algorithm) {
			case UNIFORM :
				ball3DDensityUniform(radius, blockDensity, consumer);
			case FIBONACCI :
				ball3DDensityFibonacci(radius, blockDensity, consumer);
			default :
				return;
		}
	}

	private static final void ball3DDensityUniform(double radius, double blockDensity,
			TriConsumer<Double, Double, Double> consumer) {
		var span = 1 / blockDensity;
		for (double r = 0; r <= radius; r += span) {
			sphere3DDensity(r, blockDensity, SphereAlgorithm.UNIFORM, consumer);
		}
	}

	private static final void ball3DDensityFibonacci(double radius, double blockDensity,
			TriConsumer<Double, Double, Double> consumer) {
		var span = 1 / blockDensity;
		for (double r = 0; r <= radius; r += span) {
			sphere3DDensity(r, blockDensity, SphereAlgorithm.FIBONACCI, consumer);
		}
	}

	//

	public static final void sphere3DCircle(double radius, int nbCircles, int nbPoints,
			TriConsumer<Double, Double, Double> consumer) {
		for (double theta = 0; theta < Math.PI; theta += Math.PI / nbCircles) {
			double y = Math.cos(theta) * radius;
			for (double phi = 0; phi < 2 * Math.PI; phi += (2 * Math.PI) / nbPoints) {
				double x = radius * Math.cos(phi) * Math.sin(theta);
				double z = radius * Math.sin(phi) * Math.sin(theta);
				consumer.accept(x, y, z);
			}
		}
	}

	public static final void ball3DVolume(double radius, int nbSpheres, int nbCircles, int nbPoints,
			TriConsumer<Double, Double, Double> consumer) {
		var span = radius / nbSpheres;
		for (double r = 0; r <= radius; r += span) {
			sphere3DCircle(r, nbCircles, nbPoints, consumer);
		}
	}

	public static final void sphere3DNumber(double radius, int nbPoints, TriConsumer<Double, Double, Double> consumer) {
		double phi = Math.PI * (Math.sqrt(5) - 1);
		for (int i = 0; i < nbPoints; i++) {
			double y = 1 - (i / (float) (nbPoints - 1)) * 2;
			double innerRadius = Math.sqrt(1 - y * y);
			double theta = phi * i;
			double x = Math.cos(theta) * innerRadius;
			double z = Math.sin(theta) * innerRadius;
			consumer.accept(x * radius, y * radius, z * radius);
		}
	}

	//

	public static final void line3DFromDir(Vector start, Vector direction, double distance, double blockDensity,
			TriConsumer<Double, Double, Double> consumer) {
		if (!direction.isNormalized()) {
			direction.normalize();
		}
		var dist = 1 / blockDensity;
		direction.multiply(dist);
		for (double theta = 0; theta < distance; theta += dist) {
			consumer.accept(start.getX(), start.getY(), start.getZ());
			start.add(direction);
		}
	}

	public static final void line3DFromDir(double startX, double startY, double startZ, Vector direction,
			double distance, double blockDensity, TriConsumer<Double, Double, Double> consumer) {
		line3DFromDir(new Vector(startX, startY, startZ), direction, distance, blockDensity, consumer);
	}

	public static final void line3DFromDir(double startX, double startY, double startZ, double dirx, double diry,
			double dirz, double distance, double blockDensity, TriConsumer<Double, Double, Double> consumer) {
		line3DFromDir(new Vector(startX, startY, startZ), new Vector(dirx, diry, dirz), distance, blockDensity,
				consumer);
	}

	public static final void line3DFromDir(Vector start, Vector direction, double distance, double blockDensity,
			Consumer<Vector> consumer) {
		if (!direction.isNormalized()) {
			direction.normalize();
		}
		var dist = 1 / blockDensity;
		direction.multiply(dist);
		for (double theta = 0; theta <= distance; theta += dist) {
			consumer.accept(start);
			start.add(direction);
		}
	}

	public static final void line3DFromDir(double startX, double startY, double startZ, Vector direction,
			double distance, double blockDensity, Consumer<Vector> consumer) {
		line3DFromDir(new Vector(startX, startY, startZ), direction, distance, blockDensity, consumer);
	}

	public static final void line3DFromDir(double startX, double startY, double startZ, double dirx, double diry,
			double dirz, double distance, double blockDensity, Consumer<Vector> consumer) {
		line3DFromDir(new Vector(startX, startY, startZ), new Vector(dirx, diry, dirz), distance, blockDensity,
				consumer);
	}

	public static final void line3DFromPoints(Vector start, Vector end, double blockDensity,
			Consumer<Vector> consumer) {
		line3DFromDir(start, end.clone().subtract(start), start.distance(end), blockDensity, consumer);
	}

	public static final void line3DFromPoints(double startX1, double startY1, double startZ1, double startX2,
			double startY2, double startZ2, double blockDensity, Consumer<Vector> consumer) {
		line3DFromPoints(new Vector(startX1, startY1, startZ1), new Vector(startX2, startY2, startZ2), blockDensity,
				consumer);
	}

	public static final void rectangle3DFromPoints(Vector corner1, Vector corner2, double blockDensity,
			Consumer<Vector> consumer) {
		var minX = Math.min(corner1.getX(), corner2.getX());
		var minY = Math.min(corner1.getY(), corner2.getY());
		var minZ = Math.min(corner1.getZ(), corner2.getZ());
		var maxX = Math.max(corner1.getX(), corner2.getX());
		var maxY = Math.max(corner1.getY(), corner2.getY());
		var maxZ = Math.max(corner1.getZ(), corner2.getZ());

		line3DFromPoints(minX, minY, minZ, maxX, minY, minZ, blockDensity, consumer);
		line3DFromPoints(minX, minY, minZ, minX, maxY, minZ, blockDensity, consumer);
		line3DFromPoints(minX, minY, minZ, minX, minY, maxZ, blockDensity, consumer);

		line3DFromPoints(maxX, maxY, maxZ, minX, maxY, maxZ, blockDensity, consumer);
		line3DFromPoints(maxX, maxY, maxZ, maxX, minY, maxZ, blockDensity, consumer);
		line3DFromPoints(maxX, maxY, maxZ, maxX, maxY, minZ, blockDensity, consumer);

		line3DFromPoints(maxX, minY, minZ, maxX, minY, maxZ, blockDensity, consumer);
		line3DFromPoints(maxX, minY, minZ, maxX, maxY, minZ, blockDensity, consumer);

		line3DFromPoints(minX, maxY, minZ, maxX, maxY, minZ, blockDensity, consumer);
		line3DFromPoints(minX, maxY, minZ, minX, maxY, maxZ, blockDensity, consumer);

		line3DFromPoints(minX, minY, maxZ, maxX, minY, maxZ, blockDensity, consumer);
		line3DFromPoints(minX, minY, maxZ, minX, maxY, maxZ, blockDensity, consumer);
	}

	public static final void rectangle3DFromPoints(double startX1, double startY1, double startZ1, double startX2,
			double startY2, double startZ2, double blockDensity, Consumer<Vector> consumer) {
		rectangle3DFromPoints(new Vector(startX1, startY1, startZ1), new Vector(startX2, startY2, startZ2),
				blockDensity, consumer);
	}

	public static final void rectangle3DFromPoints(BoundingBox box, double blockDensity, Consumer<Vector> consumer) {
		rectangle3DFromPoints(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ(),
				blockDensity, consumer);
	}

	public static final BlockDisplay fillBoundingBox(World world, BoundingBox box, BlockData data) {
		var min = box.getMin();
		var max = box.getMax();
		var scale = new Vector3f((float) (max.getX() - min.getX()), (float) (max.getY() - min.getY()),
				(float) (max.getZ() - min.getZ()));
		var transform = new Transformation(new Vector3f(), new AxisAngle4f(), scale, new AxisAngle4f());
		var display = (BlockDisplay) world.spawnEntity(min.toLocation(world), EntityType.BLOCK_DISPLAY,
				CreatureSpawnEvent.SpawnReason.CUSTOM, b -> {
					if (b instanceof BlockDisplay dis) {
						b.setPersistent(false);
						dis.setBlock(data);
						dis.setTransformation(transform);
					}
				});
		return display;
	}

	public static final void helix(double height, double radius, double espacement, double theta0, double density,
			Consumer<Vector> consumer) {
		var theta = theta0;
		var deltaTheta = radius / (2 * Math.PI * density);
		for (double y = 0; y <= height; y = y + deltaTheta * (espacement / (2 * Math.PI))) {
			var x = radius * Math.cos(theta);
			var z = radius * Math.sin(theta);
			consumer.accept(new Vector(x, y, z));
			theta += deltaTheta;
		}
	}
	
	public static final Collection<BlockDisplay> boundingBoxEdgeAsDisplayBlocks(World world, BoundingBox box, BlockData data){
		return boundingBoxEdgeAsDisplayBlocks(world, box, data, 0.03f);
	}
	
	public static final Collection<BlockDisplay> boundingBoxEdgeAsDisplayBlocks(World world, BoundingBox box, BlockData data, float thickness) {
		var min = box.getMin();
		var max = box.getMax();
		var displays = new LinkedList<BlockDisplay>();
		// 4 edges on one Z axis
		var z_length = (float)(max.getZ() - min.getZ());
		var x_length = (float)(max.getX() - min.getX());
		var y_length = (float)(max.getY() - min.getY());
		var transform1 = new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(thickness, thickness,z_length), new AxisAngle4f());
		for (var corner : Set.of(box.getMin(), box.getMin().setX(max.getX()-thickness), box.getMin().setY(max.getY()-thickness),box.getMin().setX(max.getX()-thickness).setY(max.getY()-thickness))) {
			displays.add((BlockDisplay) world.spawnEntity(corner.toLocation(world), EntityType.BLOCK_DISPLAY,
					CreatureSpawnEvent.SpawnReason.CUSTOM, b -> {
						if (b instanceof BlockDisplay dis) {
							b.setPersistent(false);
							dis.setBlock(data);
							dis.setTransformation(transform1);
							dis.setBrightness(new Brightness(15,15));
						}
					}));
		}
		// 4 edges of each opposite faces (8 edges total)
		var xtransform = new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(x_length, thickness,thickness), new AxisAngle4f());
		var ytransform = new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(thickness, y_length,thickness), new AxisAngle4f());
		for (var faceCorner : Set.of(box.getMin(), box.getMin().setZ(max.getZ()-thickness))) {
			for (var ycorner : Set.of(faceCorner.clone(), faceCorner.clone().setX(max.getX()-thickness))) {
				displays.add((BlockDisplay) world.spawnEntity(ycorner.toLocation(world), EntityType.BLOCK_DISPLAY,
						CreatureSpawnEvent.SpawnReason.CUSTOM, b -> {
							if (b instanceof BlockDisplay dis) {
								b.setPersistent(false);
								dis.setBlock(data);
								dis.setTransformation(ytransform);
								dis.setBrightness(new Brightness(15,15));
							}
						}));
			}
			for (var xcorner : Set.of(faceCorner.clone(), faceCorner.clone().setY(max.getY()-thickness))) {
				displays.add((BlockDisplay) world.spawnEntity(xcorner.toLocation(world), EntityType.BLOCK_DISPLAY,
						CreatureSpawnEvent.SpawnReason.CUSTOM, b -> {
							if (b instanceof BlockDisplay dis) {
								b.setPersistent(false);
								dis.setBlock(data);
								dis.setTransformation(xtransform);
								dis.setBrightness(new Brightness(15,15));
							}
						}));
			}
			
		}
		return displays;
	}
}
