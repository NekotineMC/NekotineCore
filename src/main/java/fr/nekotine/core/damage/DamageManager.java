package fr.nekotine.core.damage;

import java.util.Map;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import fr.nekotine.core.util.UtilEntity;
import fr.nekotine.core.util.UtilMath;

@ModuleNameAnnotation(Name = "DamageManager")
public class DamageManager extends PluginModule{
	private static final Material ATTACK_COOLDOWN_MATERIAL = Material.BARRIER;
	private static final int ATTACK_COOLDOWN_TICK = 10;
	private static final int NO_DAMAGE_DURATION_TICK = 20;
	private static final DamageCause[] DAMAGE_CAUSE_WITH_INVINCIBILITY_FRAME = {
			DamageCause.CONTACT, DamageCause.DRAGON_BREATH, DamageCause.FIRE, DamageCause.HOT_FLOOR, DamageCause.LAVA, DamageCause.SUFFOCATION};
	
	private static final double CRITICAL_BOOST = 1.5;
	
	private static final double ARROW_DAMAGE_PER_VELOCITY_LENGTH = 3;
	
	private static final double PROTECTION_ENCHANT_REDUCTION = 0.04;
	private static final double FALL_PROTECTION_ENCHANT_REDUCTION = 0.12;
	private static final double FIRE_PROTECTION_ENCHANT_REDUCTION = 0.08;
	private static final double PROJECTILE_PROTECTION_ENCHANT_REDUCTION = 0.08;
	private static final double BLAST_PROTECTION_ENCHANT_REDUCTION = 0.08;
	private static final double THORNS_DAMAGE = 2;
	
	//
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof LivingEntity)) return;
		if(event.isCancelled()) return;
		if(!event.getEntity().isValid()) return;
		if(event.getEntity().isInvulnerable()) return;
		
		event.setCancelled(true);
		
		LivingEntity entity = (LivingEntity)event.getEntity();
		LivingEntity damager = GetDamager(event);
		Projectile projectile = GetProjectile(event);
		double damage = event.getDamage();
		
		//Remove critical damage from close combat
		boolean critical = IsCritical(event);
		if(critical && projectile==null) damage = damage / CRITICAL_BOOST;
		
		//Consistant arrow damage
		if (projectile != null && projectile instanceof Arrow) damage = projectile.getVelocity().length() * ARROW_DAMAGE_PER_VELOCITY_LENGTH;
		
		LivingEntityDamageEvent customDamageEvent = new LivingEntityDamageEvent(entity, damager, projectile, event.getCause(), damage, false, true, null);
		//If hit is valid (attack cooldown, ...)
		if(ValidHit(customDamageEvent)) customDamageEvent.callEvent();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void EndEvent(LivingEntityDamageEvent event) {
		if(event.IsCancelled()) return;
		if(!event.GetDamaged().isValid()) return;
		if(event.GetDamaged().isInvulnerable()) return;
		
		//Apply modifiers (mulitplyers, ignoreArmor, enchants, ....) 
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
	 * @param knockbackOrigin La Location de l'origine du recul (si null, l'origine est le damager)
	 */
	public void Damage(LivingEntity damaged, LivingEntity damager, Projectile projectile, DamageCause cause, double damage, boolean ignoreArmor, boolean knockback, 
						Location knockbackOrigin) {
		new LivingEntityDamageEvent(damaged, damager, projectile, cause, damage, ignoreArmor, knockback, knockbackOrigin).callEvent();
	}
	/**
	 * 
	 * @param damager La LivingEntity qui fait les d�g�ts (si il y en a)
	 * @param radius La portée de l'explosion
	 * @param cause La cause de l'explosion
	 * @param damage Les dégâts de l'explosion
	 * @param ignoreArmor Si l'explosion doit ignorer l'armure
	 * @param knockback Si l'explosion doit faire reculer les joueurs
	 * @param origin L'origine de l'explosion 
	 * @param ignoreDamager Si l'explosion doit ignorer celui qui fait les dégats
	 */
	public void Explode(LivingEntity damager, double radius, DamageCause cause, double damage, boolean ignoreArmor, boolean knockback, Location origin, boolean ignoreDamager) {
		for(LivingEntity damaged : origin.getWorld().getNearbyLivingEntities(origin, radius)) {
			if(ignoreDamager && damager.equals(damaged)) continue;
			Damage(damaged, damager, null, cause, damage, ignoreArmor, knockback, origin);
		}
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
	private boolean IsCritical(EntityDamageEvent event) {
		if(!(event instanceof EntityDamageByEntityEvent)) return false;
		
		return ((EntityDamageByEntityEvent)event).isCritical();
	}
	private void ApplyArmor(LivingEntityDamageEvent event) {
		//Base resistance & armor protection
		double defense = event.GetDamaged().getAttribute(Attribute.GENERIC_ARMOR).getValue();
		double toughness = event.GetDamaged().getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
		PotionEffect effect = event.GetDamaged().getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		int resistance = effect == null ? 0 : effect.getAmplifier();
		
		double withArmorAndToughness = event.GetDamage() * (1 - Math.min(20, Math.max(defense / 5, defense - event.GetDamage() / (2 + toughness / 4))) / 25);
		double withResistance = withArmorAndToughness * (1 - (resistance * 0.2));
		
		event.SetDamage(withResistance);
		
	}
	private void ApplyEnchants(LivingEntityDamageEvent event) {
		//Defensif
		for (ItemStack armor : event.GetDamaged().getEquipment().getArmorContents()) {
			if (armor != null) {
				Map<Enchantment, Integer> enchants = armor.getEnchantments();
				for (Enchantment e : enchants.keySet()) {
					
					if (e.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
						event.AddFinalMult( 1 - (PROTECTION_ENCHANT_REDUCTION * enchants.get(e)));
					
					else if (e.equals(Enchantment.PROTECTION_FIRE) && 
						(event.GetCause() == DamageCause.FIRE ||
						event.GetCause() == DamageCause.FIRE_TICK ||
						event.GetCause() == DamageCause.LAVA))
						event.AddFinalMult(1 - (FIRE_PROTECTION_ENCHANT_REDUCTION * enchants.get(e)));
	
					else if (e.equals(Enchantment.PROTECTION_FALL) && 
							event.GetCause() == DamageCause.FALL)
						event.AddFinalMult(1 - (FALL_PROTECTION_ENCHANT_REDUCTION * enchants.get(e)));
	
					else if (e.equals(Enchantment.PROTECTION_EXPLOSIONS) && 
							event.GetCause() == DamageCause.ENTITY_EXPLOSION)
						event.AddFinalMult(1 - (BLAST_PROTECTION_ENCHANT_REDUCTION * enchants.get(e)));
	
					else if (e.equals(Enchantment.PROTECTION_PROJECTILE) && 
							event.GetCause() == DamageCause.PROJECTILE)
						event.AddFinalMult(1 - (PROJECTILE_PROTECTION_ENCHANT_REDUCTION * enchants.get(e)));
					
					else if (e.equals(Enchantment.THORNS)) {
						if(event.GetDamager() != null) Damage(event.GetDamager(), event.GetDamaged(), null, DamageCause.THORNS, THORNS_DAMAGE, false, true, null);
					}
				}
			}
		}
		
		//Offensifs
		if (event.GetDamager() != null) {
			ItemStack weapon = event.GetDamager().getActiveItem();
			if(event.GetDamager() instanceof Player) weapon = ((Player)event.GetDamager()).getInventory().getItemInMainHand();

			if (weapon != null) {
				Map<Enchantment, Integer> enchants = weapon.getEnchantments();
				for (Enchantment e : enchants.keySet()) {
					
					if (e.equals(Enchantment.ARROW_KNOCKBACK) || e.equals(Enchantment.KNOCKBACK)) 
						event.AddKnockbackMult(1 + (0.5 * enchants.get(e)));
					
					else if (e.equals(Enchantment.FIRE_ASPECT)) 
						event.GetDamaged().setFireTicks(Math.max(event.GetDamaged().getFireTicks(), 20 * ((enchants.get(e) * 4) - 1) ));
					
					else if (e.equals(Enchantment.ARROW_FIRE))
						event.GetDamaged().setFireTicks(Math.max(event.GetDamaged().getFireTicks(), 20 * ((enchants.get(e) * 5)) ));
					
					else if (e.equals(Enchantment.ARROW_DAMAGE))
						event.AddFinalMult(1 + (0.25 * (enchants.get(e) + 1)));
				}
			}
		}
	}
	private void ApplyModifiers(LivingEntityDamageEvent event) {	
		//D�gats de base ajout�s
		event.SetDamage(event.GetDamage() + event.GetBaseMod());

		//Ajout des multiplicateurs des d�gats de bases
		if(event.GetBaseMult() < -1) event.SetDamage(0);
		event.SetDamage(event.GetDamage() * (1 + event.GetBaseMult()));
		
		//Armure
		if(!event.IsIgnoreArmor()) ApplyArmor(event);
				
		//Enchantements
		ApplyEnchants(event);
		
		//Ajout des multiplicateurs
		event.SetDamage(event.GetDamage() * event.GetFinalMult());

		//Si les dégats proviennent d'un /kill
		event.SetDamage( Math.min(event.GetDamage(), Double.MAX_VALUE) );
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

			UtilEntity.ApplyVelocity(event.GetDamaged(), trajectory, vel, false, 0, Math.abs(0.2 * knockback), 0.4 + (0.04 * knockback), true);
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
	private boolean Contains(DamageCause[] causes, DamageCause cause) {
		for(int i=0 ; i<causes.length ; i++) {
			if (causes[i] == cause) return true;
		}
		return false;
	}
	private boolean ValidHit(LivingEntityDamageEvent event) {
		if(event.GetCause() == DamageCause.ENTITY_ATTACK) {
			if(!(event.GetDamager() instanceof Player)) return true;
			
			Player damager = (Player)event.GetDamager();
			int cooldown = damager.getCooldown(ATTACK_COOLDOWN_MATERIAL);
			
			if(cooldown > 0) return false;
			
			damager.setCooldown(ATTACK_COOLDOWN_MATERIAL, ATTACK_COOLDOWN_TICK);
			return true;
		}
		if(Contains(DAMAGE_CAUSE_WITH_INVINCIBILITY_FRAME, event.GetCause())){
			
			if(event.GetDamaged().getNoDamageTicks() > 0) return false;
			
			event.GetDamaged().setNoDamageTicks(NO_DAMAGE_DURATION_TICK);			
			return true;
		}
		return true;
	}
}
