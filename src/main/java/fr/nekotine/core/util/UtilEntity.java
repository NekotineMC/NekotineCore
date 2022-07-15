package fr.nekotine.core.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
	 * @return True si l'entité est suportée par un block solide
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
	public static void PlayDamageSound(LivingEntity entity) {
		Sound sound = Sound.ENTITY_ZOMBIFIED_PIGLIN_HURT;
		try {
			sound = Sound.valueOf("ENTITY_"+entity.getType()+"_HURT");
		} catch (IllegalArgumentException  e) {
			System.out.println("[NekotineCore][UtilEntity][PlayDamageSound] impossible d'obtenir le son pour "+entity.getType());
			System.out.println(e.getStackTrace());
		}
		
		entity.getWorld().playSound(entity.getLocation(), sound, 1.5f + (float)(0.5f * Math.random()), 0.8f + (float)(0.4f * Math.random()));
	}
	/**
	 * 
	 * @param entity
	 * @return La vie maximale de l'entité
	 */
	public static double GetMaxHealth(LivingEntity entity) {
		return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
	}
	/**
	 * 
	 * @param entity
	 * @param value (0.5 si <= 0)
	 */
	public static void SetMaxHealth(LivingEntity entity, double value) {
		entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(value);
	}
}
