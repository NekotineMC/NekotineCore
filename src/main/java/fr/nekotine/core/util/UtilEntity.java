package fr.nekotine.core.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.util.Vector;

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
	public static boolean IsOnGround(Entity entity) {
		return entity.isOnGround();
	}
	/**
	 * 
	 * @param ent
	 * @param str
	 * @param yAdd
	 * @param yMax
	 * @param groundBoost
	 */
	public static void ApplyVelocity(Entity ent, double str, double yAdd, double yMax, boolean groundBoost){
		ApplyVelocity(ent, ent.getLocation().getDirection(), str, false, 0, yAdd, yMax, groundBoost);
	}
	/**
	 * 
	 * @param ent
	 * @param vec
	 * @param str
	 * @param ySet
	 * @param yBase
	 * @param yAdd
	 * @param yMax
	 * @param groundBoost
	 */
	public static void ApplyVelocity(Entity ent, Vector vec, double str, boolean ySet, double yBase, double yAdd, double yMax, boolean groundBoost) {
		if (Double.isNaN(vec.getX()) || Double.isNaN(vec.getY()) || Double.isNaN(vec.getZ()) || vec.length() == 0)
			return;
		
		//YSet
		if (ySet)
			vec.setY(yBase);

		//Modify
		vec.normalize();
		vec.multiply(str);
		
		//YAdd
		vec.setY(vec.getY() + yAdd);
		
		//Limit
		if (vec.getY() > yMax)
			vec.setY(yMax);
		
		if (groundBoost)
			if (IsOnGround(ent))
				vec.setY(vec.getY() + 0.2); 
		
		//Velocity
		ent.setFallDistance(0);
		
		ent.setVelocity(vec);	
	}
}
