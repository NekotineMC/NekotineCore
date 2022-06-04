package fr.nekotine.core.projectile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import fr.nekotine.core.module.PluginModule;

public class ProjectileManager extends PluginModule{
	
	private HashMap<Entity, CustomProjectile> projectiles = new HashMap<Entity, CustomProjectile>();
	
	public ProjectileManager(JavaPlugin plugin) {
		super(plugin, "ProjectileManager");
	}
	
	public void AddProjectile(Entity projectile, LivingEntity sender, IProjectile iProj, Vector velocity, long expireTime,
			boolean targetLivingEntity, boolean targetBlock) {
		CustomProjectile customProjectile = new CustomProjectile(projectile, sender, iProj, velocity, expireTime, targetLivingEntity, targetBlock);
		projectiles.put(projectile, customProjectile);
	}

	@EventHandler
	public void Tick(/* inserer tick event ici */) {
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
