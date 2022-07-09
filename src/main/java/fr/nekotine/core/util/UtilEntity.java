package fr.nekotine.core.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class UtilEntity {
	/**
	 * 
	 * @param location La location du slime
	 * @param reason La raison de l'apparition
	 * @param size La taille du slime
	 * @return Le slime crée
	 */
	public static Slime SpawnSlime(Location location, SpawnReason reason, int size) {
		Slime slime = (Slime)location.getWorld().spawnEntity(location, EntityType.SLIME, reason);
		slime.setSize(size);
		return slime;
	}
	/**
	 * 
	 * @param entity L'entité à regarder
	 * @return True si l'entité est située au dessus d'un block solide ( ne vérifie pas si elle vole au dessus de ce bloc )
	 */
	public static boolean IsOnSolidBlock(Entity entity) {
		return entity.getLocation().subtract(0, 1, 0).getBlock().isSolid();
	}
}
