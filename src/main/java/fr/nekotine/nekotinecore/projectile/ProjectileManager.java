package fr.nekotine.nekotinecore.projectile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;

public class ProjectileManager {
	private HashMap<Entity, CustomProjectile> projectiles = new HashMap<Entity, CustomProjectile>();
	
	@EventHandler
	public void Tick(/* inserer tick event ici */) {
		for (Iterator<Entry<Entity, CustomProjectile>> iterator = projectiles.entrySet().iterator(); iterator.hasNext();){
			Entry<Entity, CustomProjectile> entry = iterator.next();
			if(entry.getValue().Collision()) iterator.remove();
		}
	}
}
