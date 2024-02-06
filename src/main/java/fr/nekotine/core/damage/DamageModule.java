package fr.nekotine.core.damage;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.util.EntityUtil;
import fr.nekotine.core.util.EventUtil;

public class DamageModule implements IPluginModule, Listener{
	
	public DamageModule() {
		DamageFunction.SetDamageModule(this);
		EventUtil.register(this);
	}
	
	@Override
	public void unload() {
		EventUtil.unregister(this);
	}
	
	private boolean disabled = false;
	private DamageFunction damageFunction = DamageFunction.NEKOTINE;
	
	//
	
	public void SetDamageFunction(DamageFunction damageFunction) {
		this.damageFunction = damageFunction;
	}
	public void SetDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public boolean IsDisabled() {
		return disabled;
	}
	
	//
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof LivingEntity)) return;
		if(event.isCancelled()) return;
		if(!event.getEntity().isValid()) return;
		if(event.getEntity().isInvulnerable()) return;
		if(disabled) return;
		
		event.setCancelled(true);
		
		LivingEntity entity = (LivingEntity)event.getEntity();
		LivingEntity damager = GetDamager(event);
		Projectile projectile = GetProjectile(event);
		double damage = event.getDamage();

		LivingEntityDamageEvent customDamageEvent = new LivingEntityDamageEvent(entity, damager, projectile, event.getCause(), damage, false, true, null);
		
		damageFunction.GetDamageCritical().accept(
				customDamageEvent, 
				IsCritical(event),
				projectile instanceof AbstractArrow && ((AbstractArrow)projectile).isCritical());
		
		//If hit is valid (attack cooldown, ...)
		if(damageFunction.GetDamageValidation().apply(customDamageEvent)) customDamageEvent.callEvent();
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
		damageFunction.GetDamageSound().accept(event);
		
		//Knockback
		damageFunction.GetDamageKnockback().accept(event);
		
		//Hurt effect
		damageFunction.GetDamageEffect().accept(event);
	}
	
	//
	
	/**
	 * Endommage une LivingEntity
	 * @param damaged L'entité qui prend les dégâts
	 * @param damager L'entité qui fait les dégâts
	 * @param projectile Le projectile qui fait lesdégâts
	 * @param cause La cause des dégâts
	 * @param damage La valeur des dégâts
	 * @param ignoreArmor Si les dégâts doivent ignorer l'armure
	 * @param knockback Si les dégâts doivent faire reculer
	 * @param knockbackOrigin L'origine du recul
	 * @return L'event crée
	 */
	public LivingEntityDamageEvent Damage(LivingEntity damaged, LivingEntity damager, Projectile projectile, DamageCause cause, double damage, boolean ignoreArmor, boolean knockback, 
						Location knockbackOrigin) {
		LivingEntityDamageEvent e = new LivingEntityDamageEvent(damaged, damager, projectile, cause, damage, ignoreArmor, knockback, knockbackOrigin);
		e.callEvent();
		return e;
	}
	/**
	 * Fait des dégâts dans un radius
	 * @param damager L'entité qui fait les dégâts
	 * @param radius Le radius des dégâts
	 * @param cause La cause des dégâts
	 * @param damage La cause des dégâts
	 * @param ignoreArmor Si les dégâts doivent ignorer l'armure
	 * @param knockback Si les dégâts doivent faire reculer
	 * @param origin L'origine du dégâts
	 * @param toIgnore Liste des entités à ignorer
	 * @return Tous les events crées
	 */
	public ArrayList<LivingEntityDamageEvent> Explode(LivingEntity damager, double radius, DamageCause cause, double damage, boolean ignoreArmor, boolean knockback, Location origin, LivingEntity[] toIgnore) {
		ArrayList<LivingEntityDamageEvent> events = new ArrayList<>();
		for(LivingEntity damaged : EntityUtil.GetNearbyLivingEntities(origin, radius)) {
			if(Contains(toIgnore, damaged)) continue;
			events.add(Damage(damaged, damager, null, cause, damage, ignoreArmor, knockback, origin));
		}
		return events;
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
	private void ApplyModifiers(LivingEntityDamageEvent event) {	
		//D�gats de base ajout�s
		event.SetDamage(event.GetDamage() + event.GetBaseMod());

		//Ajout des multiplicateurs des d�gats de bases
		if(event.GetBaseMult() < -1) event.SetDamage(0);
		event.SetDamage(event.GetDamage() * (1 + event.GetBaseMult()));
		
		//Armure
		if(!event.IsIgnoreArmor()) damageFunction.GetDamageArmor().accept(event);
				
		//Enchantements
		damageFunction.GetDamageEnchantments().accept(event);
		
		//Ajout des multiplicateurs
		event.SetDamage(event.GetDamage() * event.GetFinalMult());

		//Si les dégats proviennent d'un /kill
		event.SetDamage( Math.min(event.GetDamage(), Double.MAX_VALUE) );
	}
	private void Damage(LivingEntityDamageEvent event) {
		double health = Math.max(0, event.GetDamaged().getHealth() - event.GetDamage());
		health = Math.min(health, EntityUtil.GetMaxHealth(event.GetDamaged()));
		
		event.GetDamaged().setLastDamage(event.GetDamage());
		event.GetDamaged().setLastDamageCause(new EntityDamageEvent(event.GetDamaged(), event.GetCause(), event.GetDamage()));
		if(event.GetDamager() instanceof Player && !event.GetDamaged().equals(event.GetDamager())) event.GetDamaged().setKiller((Player)event.GetDamager());
		
		event.GetDamaged().setHealth(health);
	}
	private boolean Contains(LivingEntity[] entities, LivingEntity entity) {
		for(int i = 0; i < entities.length ; i++) {
			if(entity.equals(entities[i])) return true;
		}
		return false;
	}
}
