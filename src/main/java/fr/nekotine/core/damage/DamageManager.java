package fr.nekotine.core.damage;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import fr.nekotine.core.util.UtilEntity;
import fr.nekotine.core.util.UtilMath;

@ModuleNameAnnotation(Name = "DamageManager")
public class DamageManager extends PluginModule{
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof LivingEntity)) return;
		if(event.isCancelled()) return;
		if(!event.getEntity().isValid()) return;
		if(event.getEntity().isInvulnerable()) return;
		
		System.out.println("initial: "+event.getDamage());
		
		event.setCancelled(true);
		
		LivingEntity entity = (LivingEntity)event.getEntity();
		LivingEntity damager = GetDamager(event);
		Projectile projectile = GetProjectile(event);

		new LivingEntityDamageEvent(entity, damager, projectile, event.getCause(), event.getDamage(), false, true).callEvent();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void EndEvent(LivingEntityDamageEvent event) {
		if(event.IsCancelled()) return;
		if(!event.GetDamaged().isValid()) return;
		if(event.GetDamaged().isInvulnerable()) return;
		
		System.out.println("final: "+event.GetDamage());
		
		//Apply modifiers (mulitplyers, ignoreArmor, ...)
		ApplyModifiers(event);

		//Apply damage
		Damage(event);
		
		//Sound
		Sound(event);
		
		//Knockback
		Knockback(event);
		
		//Hurt effect
		event.GetDamaged().playEffect(EntityEffect.HURT);
		
		//Remove arrows
		if(event.GetProjectile() instanceof Arrow) event.GetProjectile().remove();

		//Delai avait de taper
		
		//Ajouter le punch, kb avec les enchants
	}
	
	//
	
	/**
	 * Endommage la LivingEntity
	 * @param damaged La LivingEntity qui prend les d�g�ts
	 * @param damager La LivingEntity qui fait les d�g�ts (si il y en a)
	 * @param projectile Le Projectile qui fait les d�g�ts (si il y en a)
	 * @param cause La cause du d�g�t
	 * @param damage Le montant brut de d�g�t
	 * @param ignoreArmor Si le coup doit ignorer l'armure du joueur
	 * @param knockback Si le coup doit faire reculer
	 */
	public void Damage(LivingEntity damaged, LivingEntity damager, Projectile projectile, DamageCause cause, double damage, boolean ignoreArmor, boolean knockback) {
		new LivingEntityDamageEvent(damaged, damager, projectile, cause, damage, ignoreArmor, knockback).callEvent();
	}
	
	//
	
	private LivingEntity GetDamager(EntityDamageEvent event) {
		if(!(event instanceof EntityDamageByEntityEvent)) return null;
		EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent)event;
		
		if(entityEvent.getDamager() instanceof LivingEntity) return (LivingEntity)entityEvent.getDamager();
		
		if(! (entityEvent.getDamager() instanceof Projectile)) return null;
		Projectile projectile = (Projectile)entityEvent.getDamager();
		if(! (projectile.getShooter() instanceof LivingEntity)) return null;
		
		return (LivingEntity)projectile.getShooter();
		
	}
	private Projectile GetProjectile(EntityDamageEvent event) {
		if(!(event instanceof EntityDamageByEntityEvent)) return null;
		EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent)event;
		if(! (entityEvent.getDamager() instanceof Projectile)) return null;
		
		return (Projectile)entityEvent.getDamager();
	}
	private int GetProtection(LivingEntity damaged) {
		if(!(damaged instanceof Player)) return 0;
		
		Player damagedPlayer = (Player)damaged;
		PlayerInventory inv = damagedPlayer.getInventory();
		ItemStack helm = inv.getHelmet();
		ItemStack chest = inv.getChestplate();
		ItemStack legs = inv.getLeggings();
		ItemStack boot = inv.getBoots();

		return (helm != null ? helm.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
				(chest != null ? chest.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
				(legs != null ? legs.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
				(boot != null ? boot.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0);
	}
	private double CalculateNeededDamage(LivingEntity damaged, double damage) {
		double defense = damaged.getAttribute(Attribute.GENERIC_ARMOR).getValue();
		double toughness = damaged.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
		double protection = GetProtection(damaged);
		PotionEffect effect = damaged.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		int resistance = effect == null ? 0 : effect.getAmplifier();
		
		double withArmorAndToughness = damage * (1 + Math.min(20, Math.max(defense / 5, defense - damage / (2 + toughness / 4))) / 25);
		double withResistance = withArmorAndToughness * (1 + (resistance * 0.2));
		double withEnchants = withResistance * (1 + (Math.min(20.0, protection) / 25));
		return withEnchants;
	}
	private void ApplyModifiers(LivingEntityDamageEvent event) {
		double damage = event.GetDamage();
		
		//D�gats de base ajout�s
		damage += event.GetBaseMod();
				
		//Ajout des multiplicateurs des d�gats de bases
		double baseMult = event.GetBaseMult();
		if(baseMult < -1) damage = 0;
		damage *= 1 + baseMult;
				
		//Ajout des multiplicateurs
		damage *= event.GetFinalMult();
		
		if(event.IsIgnoreArmor()) damage = CalculateNeededDamage(event.GetDamaged(), damage);
		
		//Si les dégats proviennent d'un /kill
		damage = Math.min(damage, Double.MAX_VALUE);
		
		event.SetDamage(damage);
	}
	private void Knockback(LivingEntityDamageEvent event) {
		if (event.IsKnockback() && event.GetDamager() != null){
			
			//Base
			double knockback = event.GetDamage();
			if (knockback < 2)	knockback = 2;
			knockback = Math.log10(knockback);
			
			knockback *= event.GetKnockbackMult();

			//Origin
			Location origin = event.GetDamager().getLocation();
			if (event.GetKnockbackOrigin() != null)
				origin = event.GetKnockbackOrigin();

			//Vec
			Vector trajectory = UtilMath.GetTrajectory2d(origin, event.GetDamaged().getLocation());
			trajectory.multiply(0.6 * knockback);
			trajectory.setY(Math.abs(trajectory.getY()));

			//Apply
			double vel = 0.2 + trajectory.length() * 0.8;

			UtilEntity.ApplyVelocity(event.GetDamaged(), trajectory, vel, 
					false, 0, Math.abs(0.2 * knockback), 0.4 + (0.04 * knockback), true);
		}
	}
	private void Sound(LivingEntityDamageEvent event) {
		UtilEntity.PlayDamageSound(event.GetDamaged());
		if (event.GetProjectile() != null && event.GetProjectile() instanceof Arrow && event.GetDamager() instanceof Player) {
			Player player = (Player)event.GetDamager();
			player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
		}
	}
	private void Damage(LivingEntityDamageEvent event) {
		double health = Math.max(0, event.GetDamaged().getHealth() - event.GetDamage());
		health = Math.min(health, UtilEntity.GetMaxHealth(event.GetDamaged()));
		
		event.GetDamaged().setLastDamage(event.GetDamage());
		event.GetDamaged().setLastDamageCause(new EntityDamageEvent(event.GetDamaged(), event.GetCause(), event.GetDamage()));
		if(event.GetDamager() instanceof Player) event.GetDamaged().setKiller((Player)event.GetDamager());
		
		event.GetDamaged().setHealth(health);
	}	
}
