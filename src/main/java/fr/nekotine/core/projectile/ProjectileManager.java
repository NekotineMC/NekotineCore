package fr.nekotine.core.projectile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.arrache.TickEvent;
import fr.nekotine.core.damage.LivingEntityDamageEvent;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "ProjectileManager")
public class ProjectileManager extends PluginModule{
	private final HashMap<Entity, CustomProjectile> projectiles = new HashMap<Entity, CustomProjectile>();
	private final HashMap<Entity, CustomProjectile> projectilesBuffer = new HashMap<Entity, CustomProjectile>();
	
	//
	
	/**
	 * 
	 * @param projectile L'entité à envoyer
	 * @param sender Le lanceur du projectile
	 * @param iProj Interface
	 * @param velocity La vélocité initiale du projectile
	 * @param expireTime La durée de vie du projectile en ms
	 * @param targetLivingEntity Si le projectile doit toucher les LivingEntity
	 * @param targetBlock Si le projectile doit toucher les blocs
	 */
	public boolean AddProjectile(Entity projectile, LivingEntity sender, IProjectile iProj, Vector velocity, long expireTime,
			boolean targetLivingEntity, boolean targetBlock, LivingEntity[] entityBlacklist, Material[] blockBlacklist) {
		if(Exist(projectile)) return false;

		CustomProjectile customProjectile = new CustomProjectile(projectile, sender, iProj, velocity, expireTime, targetLivingEntity, targetBlock, entityBlacklist, blockBlacklist);
		projectilesBuffer.put(projectile, customProjectile);
		return true;
	}
	public boolean AddProjectile(Entity projectile, LivingEntity sender, IProjectile iProj, Vector velocity, long expireTime,
			boolean targetLivingEntity, boolean targetBlock) {
		LivingEntity[] entityBlacklist = {};
		Material[] blockBlacklist = {};
		return AddProjectile(projectile, sender, iProj, velocity, expireTime, targetLivingEntity, targetBlock, entityBlacklist, blockBlacklist);
	}

	//
	
	/**
	 * Trigger tous les projectiles du lanceur
	 * @param sender Le lanceur du projectile
	 */
	public void TriggerFromSender(@NotNull LivingEntity sender) {
		TransferBuffer();
		
		for (Iterator<Entry<Entity, CustomProjectile>> iterator = projectiles.entrySet().iterator(); iterator.hasNext();){
			Entry<Entity, CustomProjectile> entry = iterator.next();
			
			if(sender.equals( entry.getValue().GetSender()) ) entry.getValue().SetTriggered(true);
		}
	}
	/**
	 * Trigger tous les projectiles du lanceur
	 * @param iProj L'interface utilisée pour les projectiles
	 */
	public void TriggerFromInterface(@NotNull IProjectile iProj) {
		TransferBuffer();
		
		for (Iterator<Entry<Entity, CustomProjectile>> iterator = projectiles.entrySet().iterator(); iterator.hasNext();){
			Entry<Entity, CustomProjectile> entry = iterator.next();
			
			if(iProj.equals( entry.getValue().GetInterface()) ) entry.getValue().SetTriggered(true);
		}
	}
	/**
	 * 
	 * @param sender Le lanceur
	 * @return La liste des projectiles lancés par le lanceur
	 */
	public ArrayList<CustomProjectile> GetFromSender(@NotNull LivingEntity sender) {
		TransferBuffer();
		
		ArrayList<CustomProjectile> senderProjectiles = new ArrayList<>();
		for(CustomProjectile projectile : projectiles.values()) {
			if(sender.equals( projectile.GetSender()) ) senderProjectiles.add(projectile);
		}
		return senderProjectiles;
	}
	
	//
	
	@EventHandler
	public void Tick(TickEvent e) {
		TransferBuffer();

		for (Iterator<Entry<Entity, CustomProjectile>> iterator = projectiles.entrySet().iterator(); iterator.hasNext();){
			Entry<Entity, CustomProjectile> entry = iterator.next();
			if(!entry.getValue().GetProjectile().isValid() || entry.getValue().Collision()) iterator.remove();
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void OnDamage(LivingEntityDamageEvent e) {
		if(projectilesBuffer.containsKey(e.GetDamager()) || projectiles.containsKey( e.GetDamager()) ) e.SetCancelled(true);
	}
	@EventHandler
	public void OnHopperPickup(InventoryPickupItemEvent e) {
		if(projectilesBuffer.containsKey(e.getItem()) || projectiles.containsKey(e.getItem())) e.setCancelled(true);
	}
	
	//
	
	private void TransferBuffer() {
		for(Entry<Entity, CustomProjectile> entry : projectilesBuffer.entrySet()) {
			projectiles.put(entry.getKey(), entry.getValue());
		}
		projectilesBuffer.clear();
	}
	private boolean Exist(Entity entity) {
		return projectiles.containsKey(entity);
	}
}
