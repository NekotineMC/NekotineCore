package fr.nekotine.core.projectile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.ticking.TickingModule;
import fr.nekotine.core.ticking.event.TickElapsedEvent;
import fr.nekotine.core.util.EventUtil;

public class ProjectileModule extends PluginModule implements Listener{
	private final HashMap<Entity, CustomProjectile> projectiles = new HashMap<Entity, CustomProjectile>();
	private final HashMap<Entity, CustomProjectile> projectilesBuffer = new HashMap<Entity, CustomProjectile>();
	
	public ProjectileModule() {
		EventUtil.register(this);
		Ioc.resolve(ModuleManager.class).tryLoad(TickingModule.class);
	}
	
	@Override
	protected void unload() {
		EventUtil.unregister(this);
		super.unload();
	}
	
	//
	
	/**
	 * RIEN N'EMPECHE LE PROJECTILE DE TOUCHER SON LANCEUR
	 * @param projectile Entité qui sert de projectile
	 * @param sender Lanceur du projectile
	 * @param iProj
	 * @param velocity Velocité initiale du projectile
	 * @param expireTime Durée de vie du projectile en ms
	 * @param targetLivingEntity Si le projectile peut toucher les LivingEntity
	 * @param targetBlock Si le projectile peut toucher les Block
	 * @param entityBlacklist Entités à ignorer
	 * @param blockBlacklist Matériaux des blocks à ignorer
	 * @return True si le projectile a été ajouté
	 */
	public boolean AddProjectile(Entity projectile, LivingEntity sender, IProjectile iProj, Vector velocity, long expireTime,
			boolean targetLivingEntity, boolean targetBlock, LivingEntity[] entityBlacklist, Material[] blockBlacklist) {
		if(BufferExist(projectile)) return false;

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
	 * Déclenche manuellement tous les projectiles du lanceur
	 * @param sender
	 */
	public void TriggerFromSender(@NotNull LivingEntity sender) {
		TransferBuffer();
		
		for (Iterator<Entry<Entity, CustomProjectile>> iterator = projectiles.entrySet().iterator(); iterator.hasNext();){
			Entry<Entity, CustomProjectile> entry = iterator.next();
			
			if(sender.equals( entry.getValue().GetSender()) ) entry.getValue().SetTriggered(true);
		}
	}
	/**
	 * Déclenche manuellement tous les projectiles li�s � l'interface
	 * @param iProj
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
	 * @param sender
	 * @return Tous les projectiles du lanceur
	 */
	public ArrayList<CustomProjectile> GetFromSender(@NotNull LivingEntity sender) {
		TransferBuffer();
		
		ArrayList<CustomProjectile> senderProjectiles = new ArrayList<>();
		for(CustomProjectile projectile : projectiles.values()) {
			if(sender.equals( projectile.GetSender()) ) senderProjectiles.add(projectile);
		}
		return senderProjectiles;
	}
	/**
	 * 
	 * @param projectile
	 * @return True si le projectile existe
	 */
	public boolean Exist(Entity projectile) {
		return MainExist(projectile) || BufferExist(projectile);
	}
	
	//
	
	@EventHandler
	public void Tick(TickElapsedEvent e) {
		TransferBuffer();

		for (Iterator<Entry<Entity, CustomProjectile>> iterator = projectiles.entrySet().iterator(); iterator.hasNext();){
			Entry<Entity, CustomProjectile> entry = iterator.next();
			if(!entry.getValue().GetProjectile().isValid() || entry.getValue().Collision()) iterator.remove();
		}
	}
	@EventHandler
	public void OnHopperPickup(InventoryPickupItemEvent e) {
		TransferBuffer();
		if(MainExist(e.getItem())) e.setCancelled(true);
	}
	@EventHandler
	public void OnExplosion(ExplosionPrimeEvent e) {
		TransferBuffer();
		if(MainExist(e.getEntity())) e.setCancelled(true);
	}
	@EventHandler
	public void OnDeath(EntityDeathEvent e) {
		TransferBuffer();
		if(MainExist(e.getEntity())) e.setCancelled(true);
	}
	@EventHandler
	public void OnProjectileHit(ProjectileHitEvent e) {
		TransferBuffer();
		if(e.getHitBlock() == null) return;
		if(MainExist(e.getEntity())) projectiles.get(e.getEntity()).ProjectileHitBlock(e.getHitBlock());
	}
	@EventHandler
	public void OnProjectileCollide(ProjectileHitEvent e) {
		TransferBuffer();
		if(MainExist(e.getEntity())) e.setCancelled(true);
	}
	
	//
	
	private void TransferBuffer() {
		for(Entry<Entity, CustomProjectile> entry : projectilesBuffer.entrySet()) {
			if(!MainExist(entry.getKey())) projectiles.put(entry.getKey(), entry.getValue());
		}
		projectilesBuffer.clear();
	}
	private boolean BufferExist(Entity projectile) {
		return projectilesBuffer.containsKey(projectile);
	}
	private boolean MainExist(Entity projectile) {
		return projectiles.containsKey(projectile);
	}
}
