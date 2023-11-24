package fr.nekotine.core.damage;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import fr.nekotine.core.util.EntityUtil;
import fr.nekotine.core.util.MathUtil;

public enum DamageFunction {
	NEKOTINE(
			Constants.NEKOTINE_DAMAGE_EFFECT,
			Constants.NEKOTINE_DAMAGE_SOUND,
			Constants.NEKOTINE_DAMAGE_KNOCKBACK,
			Constants.NEKOTINE_DAMAGE_ENCHANTMENTS,
			Constants.NEKOTINE_DAMAGE_ARMOR,
			Constants.NEKOTINE_DAMAGE_VALIDATION,
			Constants.NEKOTINE_DAMAGE_CRITICAL),
	CUSTOM(
			Constants.NULL_CONSUMER, 
			Constants.NULL_CONSUMER, 
			Constants.NULL_CONSUMER, 
			Constants.NULL_CONSUMER, 
			Constants.NULL_CONSUMER, 
			Constants.NULL_FUNCTION, 
			Constants.NULL_TRICONSUMER);
	
	//
	
	private Consumer<LivingEntityDamageEvent> damageEffect;
	private Consumer<LivingEntityDamageEvent> damageSound;
	private Consumer<LivingEntityDamageEvent> damageKnockback;
	private Consumer<LivingEntityDamageEvent> damageEnchantments;
	private Consumer<LivingEntityDamageEvent> damageArmor;
	private Function<LivingEntityDamageEvent, Boolean> damageValidation;
	private TriConsumer<LivingEntityDamageEvent, Boolean, Boolean> damageCritical;
	
	private static DamageModule damageModule;
	
	//
	
	DamageFunction(
			Consumer<LivingEntityDamageEvent> damageEffect,
			Consumer<LivingEntityDamageEvent> damageSound,
			Consumer<LivingEntityDamageEvent> damageKnockback,
			Consumer<LivingEntityDamageEvent> damageEnchantments,
			Consumer<LivingEntityDamageEvent> damageArmor,
			Function<LivingEntityDamageEvent, Boolean> damageValidation,
			TriConsumer<LivingEntityDamageEvent, Boolean, Boolean> damageCritical) {
		this.damageEffect = damageEffect;
		this.damageSound = damageSound;
		this.damageKnockback = damageKnockback;
		this.damageEnchantments = damageEnchantments;
		this.damageArmor = damageArmor;
		this.damageValidation = damageValidation;
		this.damageCritical = damageCritical;
	}
	
	//

	public Consumer<LivingEntityDamageEvent> GetDamageEffect() {
		return damageEffect;
	}
	public Consumer<LivingEntityDamageEvent> GetDamageSound() {
		return damageSound;
	}
	public Consumer<LivingEntityDamageEvent> GetDamageKnockback() {
		return damageKnockback;
	}
	public Consumer<LivingEntityDamageEvent> GetDamageEnchantments() {
		return damageEnchantments;
	}
	public Consumer<LivingEntityDamageEvent> GetDamageArmor() {
		return damageArmor;
	}
	public Function<LivingEntityDamageEvent, Boolean> GetDamageValidation() {
		return damageValidation;
	}
	public TriConsumer<LivingEntityDamageEvent, Boolean, Boolean> GetDamageCritical() {
		return damageCritical;
	}
	
	//
	
	public void SetDamageEffect(Consumer<LivingEntityDamageEvent> damageEffect) {
		this.damageEffect = damageEffect;
	}
	public void SetDamageSound(Consumer<LivingEntityDamageEvent> damageSound) {
		this.damageSound = damageSound;
	}
	public void SetDamageKnockback(Consumer<LivingEntityDamageEvent> damageKnockback) {
		this.damageKnockback = damageKnockback;
	}
	public void SetDamageEnchantments(Consumer<LivingEntityDamageEvent> damageEnchantments) {
		this.damageEnchantments = damageEnchantments;
	}
	public void SetDamageArmor(Consumer<LivingEntityDamageEvent> damageArmor) {
		this.damageArmor = damageArmor;
	}
	public void SetDamageValidation(Function<LivingEntityDamageEvent, Boolean> damageValidation) {
		this.damageValidation = damageValidation;
	}
	public void SetDamageCritical(TriConsumer<LivingEntityDamageEvent, Boolean, Boolean> damageCritical) {
		this.damageCritical = damageCritical;
	}
	
	//
	
	public static Consumer<LivingEntityDamageEvent> GetNullConsumer(){
		return Constants.NULL_CONSUMER;
	}
	public static Function<LivingEntityDamageEvent, Boolean> GetNullFunction(){
		return Constants.NULL_FUNCTION;
	}
	public static TriConsumer<LivingEntityDamageEvent, Boolean, Boolean> GetNullTriConsumer(){
		return Constants.NULL_TRICONSUMER;
	}
	
	//
	
	private static class Constants {
		private static final Consumer<LivingEntityDamageEvent> NULL_CONSUMER = new Consumer<LivingEntityDamageEvent>() {
			@Override
			public void accept(LivingEntityDamageEvent e) {
			}
		};
		private static final Function<LivingEntityDamageEvent, Boolean> NULL_FUNCTION = new Function<LivingEntityDamageEvent, Boolean>() {
			@Override
			public Boolean apply(LivingEntityDamageEvent e) {
				return true;
			}
		};
		private static final TriConsumer<LivingEntityDamageEvent, Boolean, Boolean> NULL_TRICONSUMER = new TriConsumer<LivingEntityDamageEvent, Boolean, Boolean>() {
			@Override
			public void accept(LivingEntityDamageEvent e, Boolean a, Boolean b) {
			}
		};
		private static final Consumer<LivingEntityDamageEvent> NEKOTINE_DAMAGE_EFFECT = new Consumer<LivingEntityDamageEvent>() {
			@Override
			public void accept(LivingEntityDamageEvent e) {
				if(e.GetDamage() >= 0) {
					//Récupérer l'angle entre le joueur et le damager
					e.GetDamaged().playEffect(EntityEffect.HURT);
					return;
				}
				e.GetDamaged().getWorld().spawnParticle(Particle.HEART, e.GetDamaged().getLocation().add(0, 1, 0), 5, 0.5, 0, 0.5);
			}
		};
		private static final Consumer<LivingEntityDamageEvent> NEKOTINE_DAMAGE_SOUND = new Consumer<LivingEntityDamageEvent>() {
			@Override
			public void accept(LivingEntityDamageEvent e) {
				if (e.GetProjectile() != null && e.GetProjectile() instanceof Arrow && e.GetDamager() instanceof Player) {
					Player player = (Player)e.GetDamager();
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
				}
				
				if(e.GetDamage() >= 0) {
					EntityUtil.PlayDamageSound(e.GetDamaged());
					return;
				}
				
				e.GetDamaged().getWorld().playSound(e.GetDamaged().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
			}
		};
		private static final Consumer<LivingEntityDamageEvent> NEKOTINE_DAMAGE_KNOCKBACK = new Consumer<LivingEntityDamageEvent>() {
			@Override
			public void accept(LivingEntityDamageEvent e) {
				if (e.IsKnockback() && (e.GetKnockbackOrigin() != null || e.GetDamager() != null)){
					
					//Base
					double knockback = e.GetDamage();
					if (knockback < 2)	knockback = 2;
					knockback = Math.log10(knockback);
					
					knockback *= e.GetKnockbackMult();

					//Origin
					Location origin;
					if (e.GetKnockbackOrigin() != null) {
						origin = e.GetKnockbackOrigin();
					}else {
						origin = e.GetDamager().getLocation();
					}

					//Vec
					Vector trajectory = MathUtil.GetTrajectory2d(origin, e.GetDamaged().getLocation());
					trajectory.multiply(0.6 * knockback);
					trajectory.setY(Math.abs(trajectory.getY()));

					//Apply
					double vel = 0.2 + trajectory.length() * 0.8;

					EntityUtil.ApplyVelocity(e.GetDamaged(), trajectory, vel, false, 0, Math.abs(0.2 * knockback), 0.4 + (0.04 * knockback), true);
				}
			}
		};
		private static final Consumer<LivingEntityDamageEvent> NEKOTINE_DAMAGE_ENCHANTMENTS = new Consumer<LivingEntityDamageEvent>() {
			private static final double PROTECTION_ENCHANT_REDUCTION = 0.04;
			private static final double FALL_PROTECTION_ENCHANT_REDUCTION = 0.12;
			private static final double FIRE_PROTECTION_ENCHANT_REDUCTION = 0.08;
			private static final double PROJECTILE_PROTECTION_ENCHANT_REDUCTION = 0.08;
			private static final double BLAST_PROTECTION_ENCHANT_REDUCTION = 0.08;
			private static final double THORNS_DAMAGE = 2;
				
			@Override
			public void accept(LivingEntityDamageEvent e) {
				//Defensif
				for (ItemStack armor : e.GetDamaged().getEquipment().getArmorContents()) {
					if (armor != null) {
						Map<Enchantment, Integer> enchants = armor.getEnchantments();
						for (Enchantment enchant : enchants.keySet()) {
							
							if (enchant.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
								e.AddFinalMult( 1 - (PROTECTION_ENCHANT_REDUCTION * enchants.get(enchant)));
							
							else if (enchant.equals(Enchantment.PROTECTION_FIRE) && 
								(e.GetCause() == DamageCause.FIRE ||
								e.GetCause() == DamageCause.FIRE_TICK ||
								e.GetCause() == DamageCause.LAVA))
								e.AddFinalMult(1 - (FIRE_PROTECTION_ENCHANT_REDUCTION * enchants.get(enchant)));
			
							else if (enchant.equals(Enchantment.PROTECTION_FALL) && 
									e.GetCause() == DamageCause.FALL)
								e.AddFinalMult(1 - (FALL_PROTECTION_ENCHANT_REDUCTION * enchants.get(enchant)));
			
							else if (enchant.equals(Enchantment.PROTECTION_EXPLOSIONS) && 
									e.GetCause() == DamageCause.ENTITY_EXPLOSION)
								e.AddFinalMult(1 - (BLAST_PROTECTION_ENCHANT_REDUCTION * enchants.get(enchant)));
			
							else if (enchant.equals(Enchantment.PROTECTION_PROJECTILE) && 
									e.GetCause() == DamageCause.PROJECTILE)
								e.AddFinalMult(1 - (PROJECTILE_PROTECTION_ENCHANT_REDUCTION * enchants.get(enchant)));
							
							else if (enchant.equals(Enchantment.THORNS)) {
								if(e.GetDamager() != null) damageModule.Damage(e.GetDamager(), e.GetDamaged(), null, DamageCause.THORNS, THORNS_DAMAGE, false, true, null);
							}
						}
					}
				}
				
				//Offensifs
				if (e.GetDamager() != null) {
					ItemStack weapon = e.GetDamager().getActiveItem();
					if(e.GetDamager() instanceof Player) weapon = ((Player)e.GetDamager()).getInventory().getItemInMainHand();

					if (weapon != null) {
						Map<Enchantment, Integer> enchants = weapon.getEnchantments();
						for (Enchantment enchant : enchants.keySet()) {
							
							if (enchant.equals(Enchantment.ARROW_KNOCKBACK) || enchant.equals(Enchantment.KNOCKBACK)) 
								e.AddKnockbackMult(1 + (0.5 * enchants.get(enchant)));
							
							else if (enchant.equals(Enchantment.FIRE_ASPECT)) 
								e.GetDamaged().setFireTicks(Math.max(e.GetDamaged().getFireTicks(), 20 * ((enchants.get(enchant) * 4) - 1) ));
							
							else if (enchant.equals(Enchantment.ARROW_FIRE))
								e.GetDamaged().setFireTicks(Math.max(e.GetDamaged().getFireTicks(), 20 * ((enchants.get(enchant) * 5)) ));
							
							else if (enchant.equals(Enchantment.ARROW_DAMAGE))
								e.AddFinalMult(1 + (0.25 * (enchants.get(enchant) + 1)));
						}
					}
				}
			}
		};
		private static final Consumer<LivingEntityDamageEvent> NEKOTINE_DAMAGE_ARMOR = new Consumer<LivingEntityDamageEvent>() {
			@Override
			public void accept(LivingEntityDamageEvent e) {
				//Base resistance & armor protection
				double defense = e.GetDamaged().getAttribute(Attribute.GENERIC_ARMOR).getValue();
				double toughness = e.GetDamaged().getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
				PotionEffect effect = e.GetDamaged().getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
				int resistance = effect == null ? 0 : effect.getAmplifier();
				
				double withArmorAndToughness = e.GetDamage() * (1 - Math.min(20, Math.max(defense / 5, defense - e.GetDamage() / (2 + toughness / 4))) / 25);
				double withResistance = withArmorAndToughness * (1 - (resistance * 0.2));
				
				e.SetDamage(withResistance);
			}
		};
		private static final Function<LivingEntityDamageEvent, Boolean> NEKOTINE_DAMAGE_VALIDATION = new Function<LivingEntityDamageEvent, Boolean>() {
			private static final Material ATTACK_COOLDOWN_MATERIAL = Material.BARRIER;
			private static final int ATTACK_COOLDOWN_TICK = 10;
			private static final int NO_DAMAGE_DURATION_TICK = 20;
			private static final DamageCause[] DAMAGE_CAUSE_WITH_INVINCIBILITY_FRAME = {DamageCause.CONTACT, DamageCause.DRAGON_BREATH, DamageCause.FIRE, DamageCause.HOT_FLOOR, DamageCause.LAVA, DamageCause.SUFFOCATION};

			private boolean Contains(DamageCause[] causes, DamageCause cause) {
				for(int i=0 ; i<causes.length ; i++) {
					if (causes[i] == cause) return true;
				}
				return false;
			}
			
			@Override
			public Boolean apply(LivingEntityDamageEvent e) {
				if(e.GetCause() == DamageCause.ENTITY_ATTACK) {
					if(!(e.GetDamager() instanceof Player)) return true;
					
					Player damager = (Player)e.GetDamager();
					int cooldown = damager.getCooldown(ATTACK_COOLDOWN_MATERIAL);
					
					if(cooldown > 0) return false;
					
					damager.setCooldown(ATTACK_COOLDOWN_MATERIAL, ATTACK_COOLDOWN_TICK);
					return true;
				}
				if(Contains(DAMAGE_CAUSE_WITH_INVINCIBILITY_FRAME, e.GetCause())){
					
					if(e.GetDamaged().getNoDamageTicks() > 0) return false;
					
					e.GetDamaged().setNoDamageTicks(NO_DAMAGE_DURATION_TICK);			
					return true;
				}
				return true;
			}
		};
		private static final TriConsumer<LivingEntityDamageEvent, Boolean, Boolean> NEKOTINE_DAMAGE_CRITICAL = new TriConsumer<LivingEntityDamageEvent, Boolean, Boolean>() {
			private static final double CRITICAL_BOOST = 1.5;
			
			@Override
			public void accept(LivingEntityDamageEvent e, Boolean a, Boolean b) {
				if(a) e.SetDamage(e.GetDamage() / CRITICAL_BOOST);
				if(b) ((AbstractArrow)e.GetProjectile()).setCritical(false);
			}
		};
	}
	
	//
	
	public static void SetDamageModule(DamageModule damageModule) {
		DamageFunction.damageModule = damageModule;
	}
}
