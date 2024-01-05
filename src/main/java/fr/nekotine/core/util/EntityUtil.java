package fr.nekotine.core.util;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import fr.nekotine.core.ioc.Ioc;

public class EntityUtil {
	/**
	 * Crée un slime de la taille donnée.
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
		Sound sound = Sound.ENTITY_PLAYER_HURT;
		
		EntityType type = entity.getType();
		if(entity instanceof Llama) type = EntityType.LLAMA;
		
		try {
			sound = Sound.valueOf("ENTITY_"+type+"_HURT");
		} catch (IllegalArgumentException  e) {
			Ioc.resolve(JavaPlugin.class).getLogger()
			.log(Level.SEVERE, "[NekotineCore] > [UtilEntity] > [PlayDamageSound] impossible d'obtenir le son pour "+entity.getType(), (Throwable)e);
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
	public static Collection<LivingEntity> GetNearbyLivingEntities(Location center, double radius) {
		Collection<LivingEntity> nearby = center.getWorld().getNearbyLivingEntities(center, radius);
		nearby.removeIf(new Predicate<LivingEntity>() {
			@Override
			public boolean test(LivingEntity t) {
				return t.getLocation().distanceSquared(center) > Math.pow(radius, 2);
			}
		});
		return nearby;
	}
	
	/**
	 * Retire tous les effets de potion minecraft à cette {@link org.bukkit.entity.LivingEntity LicingEntity}.
	 * @param target l'entitée vidée de ses effets.
	 */
	public static void clearPotionEffects(LivingEntity target) {
		Registry.POTION_EFFECT_TYPE.forEach(e -> target.removePotionEffect(e));
	}
	
	/**
	 * Change la valeur de tous les attributs listés dans {@link org.bukkit.attribute.Attribute Attribute} pour leurs valeur de base.
	 * Supprime également tous les {@link org.bukkit.attribute.AttributeModifier AttributeModifier}.
	 * @param target
	 */
	public static void defaultAllAttributes(Attributable target) {
		for (var attrType : Attribute.values()) {
			var attrInstance = target.getAttribute(attrType);
			if (attrInstance != null) { // Toutes les entitées n'ont pas tous les attributs
				if (attrType == Attribute.GENERIC_MOVEMENT_SPEED) {
					attrInstance.setBaseValue(0.1);
				}else {
					attrInstance.setBaseValue(attrInstance.getDefaultValue());
				}
				for (var modifier : attrInstance.getModifiers()) {
					attrInstance.removeModifier(modifier);
				}
			}
		}
	}
	
	public static void fakeDamage(LivingEntity target, Player ... observers) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.HURT_ANIMATION);
		packet.getIntegers().write(0, target.getEntityId());
		var pmanager = ProtocolLibrary.getProtocolManager();
		for (var player : observers) {
			pmanager.sendServerPacket(player, packet);
		}
	}
	
	public static void fakeDamage(LivingEntity target, Iterable<Player> observers) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.HURT_ANIMATION);
		packet.getIntegers().write(0, target.getEntityId());
		var pmanager = ProtocolLibrary.getProtocolManager();
		for (var player : observers) {
			pmanager.sendServerPacket(player, packet);
		}
	}
	
	public static void fakeDamage(LivingEntity target) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.HURT_ANIMATION);
		packet.getIntegers().write(0, target.getEntityId());
		var pmanager = ProtocolLibrary.getProtocolManager();
		pmanager.broadcastServerPacket(packet);
	}
}
