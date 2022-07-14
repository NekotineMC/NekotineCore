package fr.nekotine.core.damage;

import javax.annotation.Nullable;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

public class LivingEntityDamageEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	//
	
	private final LivingEntity damaged;
	private final LivingEntity damager;
	private final Projectile projectile;
	private final DamageCause cause;
	private double damage;
	private boolean ignoreArmor;
	private boolean knockback;
	
	private double baseMod;
	private double baseMult;
	private double finalMult;
	private double knockbackMult;
	private boolean cancelled;
	
	//
	
	public LivingEntityDamageEvent(LivingEntity damaged, LivingEntity damager, Projectile projectile, DamageCause cause, double damage, boolean ignoreArmor, boolean knockback) {
		this.damaged = damaged;
		this.damager = damager;
		this.projectile = projectile;
		this.cause = cause;
		this.damage = damage;
		this.ignoreArmor = ignoreArmor;
		this.knockback = knockback;
		
		this.baseMod = 0;
		this.baseMult = 0;
		this.finalMult = 1;
		this.knockbackMult = 1;
		this.cancelled = false;
	}
	
	//
	
	/**
	 * 
	 * @return True si le coup doit ignorer l'armur du joueur
	 */
	public boolean IsIgnoreArmor() {
		return ignoreArmor;
	}
	/**
	 * 
	 * @return True si la LivingEntity doit reculer sous l'impact
	 */
	public boolean IsKnockback() {
		return knockback;
	}
	/**
	 * 
	 * @return La LivingEntity qui fait les d�g�ts
	 */
	public @Nullable LivingEntity GetDamager() {
		return damager;
	}
	/**
	 * 
	 * @return Le Projectile qui fait les d�g�ts
	 */
	public @Nullable Projectile GetProjectile() {
		return projectile;
	}
	/**
	 * 
	 * @return Si l'Event a �t� annull�
	 */
	public boolean IsCancelled() {
		return cancelled;
	}
	public @NotNull LivingEntity GetDamaged() {
		return damaged;
	}
	/**
	 * 
	 * @return La cause du d�g�t
	 */
	public @Nullable DamageCause GetCause() {
		return cause;
	}
	/**
	 * 
	 * @return Le d�g�ts brut du coup
	 */
	public double GetDamage() {
		return damage;
	}
	/**
	 * 
	 * @return La valeur de base qui sera ajout�e aux d�g�ts
	 */
	public double GetBaseMod() {
		return baseMod;
	}
	/**
	 * 
	 * @return Le multiplicatuer de base qui sera ajout� aux d�g�ts
	 */
	public double GetBaseMult() {
		return baseMult;
	}
	/**
	 * 
	 * @return Le multiplicateur final qui sera ajout� aux d�g�ts
	 */
	public double GetFinalMult() {
		return finalMult;
	}
	/**
	 * 
	 * @return Le multiplicateur final qui sera ajout� au recul
	 */
	public double GetKnockbackMult() {
		return knockbackMult;
	}
	
	//

	/**
	 * 
	 * @param ignoreArmor Si le coup doit ignorer l'armure du joueur
	 */
	public void SetIgnoreArmor(boolean ignoreArmor) {
		this.ignoreArmor = ignoreArmor;
	}
	/**
	 * 
	 * @param knockback Si le coup doit faire reculer le joueur
	 */
	public void SetKnockback(boolean knockback) {
		this.knockback = knockback;
	}
	/**
	 * 
	 * @param cancelled Si le coup doit �tre annull�
	 */
	public void SetCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	/**
	 * 
	 * @param damage D�g�ts de bases du coup
	 */
	public void SetDamage(double damage) {
		this.damage = damage;
	}
	/**
	 * 
	 * @param baseMod Les d�g�ts de base � ajouter
	 */
	public void AddBaseMod(double baseMod) {
		this.baseMod += baseMod;
	}
	/**
	 * 
	 * @param baseMult Le multiplicateur de base � ajouter
	 */
	public void AddBaseMult(double baseMult) {
		this.baseMult += baseMult;
	}
	/**
	 * 
	 * @param finalMult Le multiplicateur final � ajouter
	 */
	public void AddFinalMult(double finalMult) {
		this.finalMult *= finalMult;
	}
	/**
	 * 
	 * @param knockbackMult Le multiplicateur final � ajouter
	 */
	public void AddKnockbackMult(double knockbackMult) {
		this.knockbackMult *= knockbackMult;
	}
}
