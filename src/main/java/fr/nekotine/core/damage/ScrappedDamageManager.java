package fr.nekotine.core.damage;

import java.util.WeakHashMap;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;

public class ScrappedDamageManager extends PluginModule{
	public ScrappedDamageManager(JavaPlugin plugin, ModuleManager manager) {
		super(plugin, "DamageManager", manager);
	}
	
	//
	
	private WeakHashMap<EntityDamageEvent, ScrappedDamageEventExpander> entityDamageEvents = new WeakHashMap<>();
	
	//
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void OnDamage(EntityDamageEvent event) {
		if(!Exist(event)) {	
			entityDamageEvents.put(event, new ScrappedDamageEventExpander(false, false));
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void CalculateDamage(EntityDamageEvent event) {
		if(!event.isCancelled() && Exist(event) && event.getEntity() instanceof LivingEntity) {
			
			LivingEntity damaged = (LivingEntity)event.getEntity();
			double damage = event.getDamage();
			
			if(Get(event).IsIgnoreArmor()) event.setDamage(CalculateNeededDamage(damaged, damage));
			
			if(!Get(event).IsKnockback()) {
				damaged.damage(damage);
				event.setCancelled(true);
			}
		}
		
		entityDamageEvents.remove(event);
	}
	
	//
	
	public ScrappedDamageEventExpander Get(EntityDamageEvent event) {
		return entityDamageEvents.get(event);
	}
	public boolean Exist(EntityDamageEvent event) {
		return entityDamageEvents.containsKey(event);
	}
	public void Damage(LivingEntity damaged, double damage, DamageCause cause, boolean ignoreArmor) {
		EntityDamageEvent event = new EntityDamageEvent(damaged, cause, damage);
		entityDamageEvents.put(event, new ScrappedDamageEventExpander(ignoreArmor, false));
		
		event.callEvent();
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
