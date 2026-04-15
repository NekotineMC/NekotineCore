package fr.nekotine.core.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

public class BukkitUtil {

	/**
	 * Écrase une Location avec les données d'une autre.
	 *
	 * @param toOverride
	 * @param with
	 */
	public static void overrideLocationWith(Location toOverride, Location with) {
		toOverride.set(with.getX(), with.getY(), with.getZ());
		toOverride.setPitch(with.getPitch());
		toOverride.setYaw(with.getYaw());
		toOverride.setWorld(with.getWorld());
	}

	public static Location defaultLocation() {
		return new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
	}

	public static void fillBoundingBoxWith(World world, BoundingBox bb, Material material) {
		var minX = bb.getMinX();
		var minY = bb.getMinY();
		var minZ = bb.getMinZ();
		var maxX = (int) bb.getMaxX();
		var maxY = (int) bb.getMaxY();
		var maxZ = (int) bb.getMaxZ();
		int x, y, z;
		for (x = (int) minX; x < maxX; x++) {
			for (y = (int) minY; y < maxY; y++) {
				for (z = (int) minZ; z < maxZ; z++) {
					world.getBlockAt(x, y, z).setType(material, false);
				}
			}
		}
	}
}
