package fr.nekotine.core.damage;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;

public class DamageManager extends PluginModule{
	public DamageManager(JavaPlugin plugin, ModuleManager manager) {
		super(plugin, "DamageManager", manager);
	}
	
	//
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof LivingEntity)) return;
		if(event.isCancelled()) return;
		
		LivingEntity entity = (LivingEntity)event.getEntity();
		LivingEntity damager = GetDamager(event);
		Projectile projectile = GetProjectile(event);
		
		LivingEntityDamageEvent extendedEvent = new LivingEntityDamageEvent(entity, damager, projectile, event.getCause(), event.getDamage(), false, true);
		
		event.setCancelled(true);
		extendedEvent.callEvent();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void EndEvent(LivingEntityDamageEvent event) {
		if(event.IsCancelled()) return;
		
		ApplyModifiers(event);
		
		if(event.IsIgnoreArmor()) 
			event.SetDamage( CalculateNeededDamage(event.GetDamaged(), event.GetDamage()) );
		if(event.IsKnockback() && event.GetDamager() != null) 
			event.GetDamaged().setVelocity(event.GetDamager().getLocation().getDirection().setY(0).normalize().multiply(event.GetKnockbackMult()));
		
		
		event.GetDamaged().damage(event.GetDamage());
		event.SetCancelled(true);
	}
	
	//
	
	/**
	 * Endommage la LivingEntity
	 * @param damaged La LivingEntity qui prend les dégâts
	 * @param damager La LivingEntity qui fait les dégâts (si il y en a)
	 * @param projectile Le Projectile qui fait les dégâts (si il y en a)
	 * @param cause La cause du dégât
	 * @param damage Le montant brut de dégât
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
		
		//Dégats de base ajoutés
		damage += event.GetBaseMod();
				
		//Ajout des multiplicateurs des dégats de bases
		double baseMult = event.GetBaseMult();
		if(baseMult < -1) damage = 0;
		damage *= 1 + baseMult;
				
		//Ajout des multiplicateurs
		damage *= event.GetFinalMult();
		
		event.SetDamage(damage);
	}
}
