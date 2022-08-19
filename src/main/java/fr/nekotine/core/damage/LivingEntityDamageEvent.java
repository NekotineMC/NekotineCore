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
	 * @return La LivingEntity qui fait des dégâts
	 */
	public LivingEntity GetDamager() {
		return damager;
	}
	/**
	 * 
	 * @return Le Projectile qui fait les dégâts
	 */
	public Projectile GetProjectile() {
		return projectile;
	}
	/**
	 * 
	 * @return Si l'Event a été annulé
	 */
	public boolean IsCancelled() {
		return cancelled;
	}
	/**
	 * 
	 * @return La LivingEntity qui prend des dégâts
	 */
	public LivingEntity GetDamaged() {
		return damaged;
	}
	/**
	 * 
	 * @return La cause des dégâts
	 */
	public DamageCause GetCause() {
		return cause;
	}
	/**
	 * 
	 * @return Les dégâts bruts
	 */
	public double GetDamage() {
		return damage;
	}
	/**
	 * 
	 * @return La valeur de base qui sera ajoutée aux dégâts
	 */
	public double GetBaseMod() {
		return baseMod;
	}
	/**
	 * 
	 * @return Le multiplicateur de base qui sera ajouté aux dégâts
	 */
	public double GetBaseMult() {
		return baseMult;
	}
	/**
	 * 
	 * @return Le multiplicateur final qui sera ajouté aux dégâts
	 */
	public double GetFinalMult() {
		return finalMult;
	}
	/**
	 * 
	 * @return Le multiplicateur final qui sera sera ajouté au recul
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
	 * Si le coup doit être annulé
	 * @param cancelled 
	 */
	public void SetCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	/**
	 * Fixe les dégâts de bases
	 * @param damage
	 */
	public void SetDamage(double damage) {
		this.damage = damage;
	}
	/**
	 * Ajoute des dégâts de bases
	 * @param baseMod
	 */
	public void AddBaseMod(double baseMod) {
		this.baseMod += baseMod;
	}
	/**
	 * Ajoute un multiplicateur de dégâts de base
	 * @param baseMult
	 */
	public void AddBaseMult(double baseMult) {
		this.baseMult += baseMult;
	}
	/**
	 * Ajoute un multiplicateur de dégâts final
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
