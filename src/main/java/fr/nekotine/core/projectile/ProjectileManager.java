package fr.nekotine.core.projectile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.arrache.TickEvent;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;

public class ProjectileManager extends PluginModule{
	public ProjectileManager(JavaPlugin plugin, ModuleManager moduleManager) {
		super(plugin, "ProjectileManager", moduleManager);
	}
	
	//
	
	private HashMap<Entity, CustomProjectile> projectiles = new HashMap<Entity, CustomProjectile>();
	
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
	public void AddProjectile(Entity projectile, LivingEntity sender, IProjectile iProj, Vector velocity, long expireTime,
			boolean targetLivingEntity, boolean targetBlock) {
		CustomProjectile customProjectile = new CustomProjectile(projectile, sender, iProj, velocity, expireTime, targetLivingEntity, targetBlock);
		projectiles.put(projectile, customProjectile);
	}

	//
	
	/**
	 * Trigger tous les projectiles du lanceur
	 * @param sender Le lanceur du projectile
	 */
	public void TriggerFromSender(@NotNull LivingEntity sender) {
		for (Iterator<Entry<Entity, CustomProjectile>> iterator = projectiles.entrySet().iterator(); iterator.hasNext();){
			Entry<Entity, CustomProjectile> entry = iterator.next();
			
			if(sender.equals( entry.getValue().GetSender()) ) entry.getValue().SetTriggered(true);
		}
	}
	/**
	 * 
	 * @param sender Le lanceur
	 * @return La liste des projectiles lancés par le lanceur
	 */
	public ArrayList<CustomProjectile> GetFromSender(@NotNull LivingEntity sender) {
		ArrayList<CustomProjectile> senderProjectiles = new ArrayList<>();
		for(CustomProjectile projectile : projectiles.values()) {
			if(sender.equals( projectile.GetSender()) ) senderProjectiles.add(projectile);
		}
		return senderProjectiles;
	}
	
	//
	
	@EventHandler
	public void Tick(TickEvent e) {
		for (Iterator<Entry<Entity, CustomProjectile>> iterator = projectiles.entrySet().iterator(); iterator.hasNext();){
			Entry<Entity, CustomProjectile> entry = iterator.next();
			if(!entry.getKey().isValid() || entry.getValue().Collision()) iterator.remove();
		}
	}
	@EventHandler
	public void OnHopperPickup(InventoryPickupItemEvent event) {
		if(projectiles.containsKey(event.getItem())) event.setCancelled(true);
	}
}
