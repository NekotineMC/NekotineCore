package fr.nekotine.core.damage;

import javax.annotation.Nullable;

import org.bukkit.Location;
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
	private Location knockbackOrigin;
	
	//
	
	public LivingEntityDamageEvent(LivingEntity damaged, LivingEntity damager, Projectile projectile, DamageCause cause, double damage, boolean ignoreArmor, boolean knockback, 
			Location knockbackOrigin) {
		this.damaged = damaged;
		this.damager = damager;
		this.projectile = projectile;
		this.cause = cause;
		this.damage = damage;
		this.ignoreArmor = ignoreArmor;
		this.knockback = knockback;
		this.knockbackOrigin = knockbackOrigin;
		
		this.baseMod = 0;
		this.baseMult = 0;
		this.finalMult = 1;
		this.knockbackMult = 1;
		this.cancelled = false;
	}
	
	//
	
	/**
	 * 
	 * @return True si le dégât doit ignorer l'armure
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
	 * @return La LivingEntity qui fait les dï¿½gï¿½ts
	 */
	public @Nullable LivingEntity GetDamager() {
		return damager;
	}
	/**
	 * 
	 * @return Le Projectile qui fait les dï¿½gï¿½ts
	 */
	public @Nullable Projectile GetProjectile() {
		return projectile;
	}
	/**
	 * 
	 * @return Si l'Event a ï¿½tï¿½ annullï¿½
	 */
	public boolean IsCancelled() {
		return cancelled;
	}
	public @NotNull LivingEntity GetDamaged() {
		return damaged;
	}
	/**
	 * 
	 * @return La cause du dï¿½gï¿½t
	 */
	public @Nullable DamageCause GetCause() {
		return cause;
	}
	/**
	 * 
	 * @return Le dï¿½gï¿½ts brut du coup
	 */
	public double GetDamage() {
		return damage;
	}
	/**
	 * 
	 * @return La valeur de base qui sera ajoutï¿½e aux dï¿½gï¿½ts
	 */
	public double GetBaseMod() {
		return baseMod;
	}
	/**
	 * 
	 * @return Le multiplicatuer de base qui sera ajoutï¿½ aux dï¿½gï¿½ts
	 */
	public double GetBaseMult() {
		return baseMult;
	}
	/**
	 * 
	 * @return Le multiplicateur final qui sera ajoutï¿½ aux dï¿½gï¿½ts
	 */
	public double GetFinalMult() {
		return finalMult;
	}
	/**
	 * 
	 * @return Le multiplicateur final qui sera ajoutï¿½ au recul
	 */
	public double GetKnockbackMult() {
		return knockbackMult;
	}
	/**
	 * 
	 * @return @Nullable La location de l'origine du knockback
	 */
	public Location GetKnockbackOrigin() {
		return knockbackOrigin;
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
	 * @param cancelled Si le coup doit ï¿½tre annullï¿½
	 */
	public void SetCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	/**
	 * 
	 * @param damage Dï¿½gï¿½ts de bases du coup
	 */
	public void SetDamage(double damage) {
		this.damage = damage;
	}
	/**
	 * 
	 * @param baseMod Les dï¿½gï¿½ts de base ï¿½ ajouter
	 */
	public void AddBaseMod(double baseMod) {
		this.baseMod += baseMod;
	}
	/**
	 * 
	 * @param baseMult Le multiplicateur de base ï¿½ ajouter
	 */
	public void AddBaseMult(double baseMult) {
		this.baseMult += baseMult;
	}
	/**
	 * 
	 * @param finalMult Le multiplicateur final ï¿½ ajouter
	 */
	public void AddFinalMult(double finalMult) {
		this.finalMult *= finalMult;
	}
	/**
	 * 
	 * @param knockbackMult Le multiplicateur final ï¿½ ajouter
	 */
	public void AddKnockbackMult(double knockbackMult) {
		this.knockbackMult *= knockbackMult;
	}
	/**
	 * 
	 * @param knockbackOrigin La Location d'origine du knockback
	 */
	public void SetKnockbackOrigin(Location knockbackOrigin) {
		this.knockbackOrigin = knockbackOrigin;
	}
}
