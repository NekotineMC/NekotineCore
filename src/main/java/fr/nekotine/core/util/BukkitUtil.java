package fr.nekotine.core.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class BukkitUtil {

	/**
	 * Écrase une Location avec les données d'une autre.
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
		return new Location(Bukkit.getWorlds().get(0),0,0,0);
	}
	
}
