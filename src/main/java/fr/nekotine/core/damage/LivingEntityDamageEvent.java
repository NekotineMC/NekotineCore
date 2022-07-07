package fr.nekotine.core.damage;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LivingEntityDamageEvent extends EntityDamageEvent{
	private LivingEntity damager;
	private Projectile projectile;
	private boolean ignoreArmor;
	private boolean knockback;
	
	//
	
	public LivingEntityDamageEvent(LivingEntity damaged, LivingEntity damager, Projectile projectile, DamageCause cause, double damage, boolean ignoreArmor, boolean knockback) {
		super(damaged, cause, damage);
		this.damager = damager;
		this.projectile = projectile;
		this.ignoreArmor = ignoreArmor;
		this.knockback = knockback;
		
	}
	
	//
	
	public boolean IsIgnoreArmor() {
		return ignoreArmor;
	}
	public boolean IsKnockback() {
		return knockback;
	}
	public LivingEntity GetDamager() {
		return damager;
	}
	public Projectile GetProjectile() {
		return projectile;
	}
	
	//

	public void SetIgnoreArmor(boolean ignoreArmor) {
		this.ignoreArmor = ignoreArmor;
		
	}
	public void SetKnockback(boolean knockback) {
		this.knockback = knockback;
	}
	public void EndEvent() {
		if(isCancelled()) return;
		if(ignoreArmor) setDamage( CalculateNeededDamage((LivingEntity)getEntity(), getDamage()) );
		if(!knockback) {
			
			((LivingEntity)getEntity()).damage(getDamage());
			setCancelled(true);
		}
	}
	
	//
	
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
}
