package fr.nekotine.core.damage;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

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
	 * @return True si le d�g�t doit ignorer l'armure
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
	 * @return La LivingEntity qui fait des d�g�ts
	 */
	public LivingEntity GetDamager() {
		return damager;
	}
	/**
	 * 
	 * @return Le Projectile qui fait les d�g�ts
	 */
	public Projectile GetProjectile() {
		return projectile;
	}
	/**
	 * 
	 * @return Si l'Event a �t� annul�
	 */
	public boolean IsCancelled() {
		return cancelled;
	}
	/**
	 * 
	 * @return La LivingEntity qui prend des d�g�ts
	 */
	public LivingEntity GetDamaged() {
		return damaged;
	}
	/**
	 * 
	 * @return La cause des d�g�ts
	 */
	public DamageCause GetCause() {
		return cause;
	}
	/**
	 * 
	 * @return Les d�g�ts bruts
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
	 * @return Le multiplicateur de base qui sera ajout� aux d�g�ts
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
	 * @return Le multiplicateur final qui sera sera ajout� au recul
	 */
	public double GetKnockbackMult() {
		return knockbackMult;
	}
	/**
	 * 
	 * @return La Location de l'origine du knockback
	 */
	public Location GetKnockbackOrigin() {
		return knockbackOrigin;
	}
	
	//

	/**
	 * Si le coup doit ignorer l'armure du joueur
	 * @param ignoreArmor 
	 */
	public void SetIgnoreArmor(boolean ignoreArmor) {
		this.ignoreArmor = ignoreArmor;
	}
	/**
	 * Si le coup doit faire reculer le joueur
	 * @param knockback 
	 */
	public void SetKnockback(boolean knockback) {
		this.knockback = knockback;
	}
	/**
	 * Si le coup doit �tre annul�
	 * @param cancelled 
	 */
	public void SetCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	/**
	 * Fixe les d�g�ts de bases
	 * @param damage
	 */
	public void SetDamage(double damage) {
		this.damage = damage;
	}
	/**
	 * Ajoute des d�g�ts de bases
	 * @param baseMod
	 */
	public void AddBaseMod(double baseMod) {
		this.baseMod += baseMod;
	}
	/**
	 * Ajoute un multiplicateur de d�g�ts de base
	 * @param baseMult
	 */
	public void AddBaseMult(double baseMult) {
		this.baseMult += baseMult;
	}
	/**
	 * Ajoute un multiplicateur de d�g�ts final
	 * @param finalMult
	 */
	public void AddFinalMult(double finalMult) {
		this.finalMult *= finalMult;
	}
	/**
	 * Ajoute un multiplicateur de recul
	 * @param knockbackMult
	 */
	public void AddKnockbackMult(double knockbackMult) {
		this.knockbackMult *= knockbackMult;
	}
	/**
	 * Modifie l'origine du recul
	 * @param knockbackOrigin
	 */
	public void SetKnockbackOrigin(Location knockbackOrigin) {
		this.knockbackOrigin = knockbackOrigin;
	}
}
