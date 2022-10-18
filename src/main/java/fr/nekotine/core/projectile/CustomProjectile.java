package fr.nekotine.core.projectile;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import fr.nekotine.core.util.TimeUtil;

public class CustomProjectile {
	private final Entity projectile;
	private final LivingEntity sender;
	private final IProjectile iProj;
	private final long expireTime;
	private final boolean targetLivingEntity;
	private final boolean targetBlock;
	private ArrayList<LivingEntity> entityBlacklist;
	private ArrayList<Material> blockBlacklist;
	
	private final long startedTime;
	private boolean triggered;
	private boolean cancelled;
	private final double DISTANCE_TO_HIT_BLOCK = 0.05;
	
	//
	
	public CustomProjectile(Entity projectile, LivingEntity sender, IProjectile iProj, Vector velocity, long expireTime, boolean targetLivingEntity,
			boolean targetBlock, LivingEntity[] entityBlacklist, Material[] blockBlacklist) {
		this.projectile = projectile;
		this.sender = sender;
		this.iProj = iProj;
		this.expireTime = expireTime;
		this.targetLivingEntity = targetLivingEntity;
		this.targetBlock = targetBlock;
		TransferBlackLists(entityBlacklist, blockBlacklist);
		
		
		this.startedTime = TimeUtil.GetTime();
		this.triggered = false;
		this.cancelled = false;
		ConfigureProjectile(projectile, velocity);
	}
	
	//
	
	private void ConfigureProjectile(Entity projectile, Vector velocity) {
		projectile.setVelocity(velocity);
		if(projectile instanceof Item) {
			Item itemProjectile = (Item)projectile;
			itemProjectile.setCanMobPickup(false);
			itemProjectile.setCanPlayerPickup(false);
		}
	}
	private int CalculateCeil(double value) {
		return (int)Math.round( Math.ceil( Math.abs(value) ) * Math.signum(value) ) ;
	}
	private int CalculateFloor(double value) {
		return (int)Math.round( Math.floor( Math.abs(value) ) * Math.signum(value) ) ;
	}
	private int CalculateMin(double value) {
		return (value < 0) ? CalculateCeil(value) - 1: CalculateFloor(value);
	}
	private int CalculateMax(double value) {
		return (value < 0) ? CalculateFloor(value) + 1: CalculateCeil(value);
	}
	private void TransferBlackLists(LivingEntity[] entityBlacklist, Material[] blockBlacklist) {
		if(targetLivingEntity) {
			this.entityBlacklist = new ArrayList<>();
			for(int i=0 ; i<entityBlacklist.length; i++) this.entityBlacklist.add(entityBlacklist[i]);
		}
		if(targetBlock) {
			this.blockBlacklist = new ArrayList<>();
			for(int i=0 ; i<blockBlacklist.length; i++) this.blockBlacklist.add(blockBlacklist[i]);
		}
	}
	
	//

	public boolean Collision() {
		if(triggered) {
			iProj.Triggered(this);
			return true;
		}
		if(expireTime != -1 && TimeUtil.Difference(TimeUtil.GetTime(), startedTime) >  expireTime) {
			cancelled = false;
			iProj.Faded(this);
			return !cancelled;
		}
		if(targetLivingEntity) {
			for(Entity hitEntity : projectile.getNearbyEntities(0,0, 0)) {
				if(hitEntity instanceof LivingEntity) {
					LivingEntity hitLivingEntity = (LivingEntity)hitEntity;
					
					if(!entityBlacklist.contains(hitLivingEntity)) {
						if(hitLivingEntity instanceof Player) {
							Player hitPlayer = (Player)hitLivingEntity;
							if(hitPlayer.getGameMode() == GameMode.CREATIVE || hitPlayer.getGameMode() == GameMode.SPECTATOR) continue;
						}
						
						cancelled = false;
						iProj.Hit(hitLivingEntity, null, this);
						return !cancelled;
					}
				}	
			}
		}
		if(targetBlock) {
			BoundingBox box = projectile.getBoundingBox().clone().expand(DISTANCE_TO_HIT_BLOCK);
			int startX = CalculateMin(box.getMinX());
			int startY = CalculateMin(box.getMinY());
			int startZ = CalculateMin(box.getMinZ());
			
			int endX = CalculateMax(box.getMaxX());
			int endY = CalculateMax(box.getMaxY()) + 1;
			int endZ = CalculateMax(box.getMaxZ());

			for(int x = startX ; x <= endX ; x++) {
				for(int y = startY ; y <= endY ; y++) {
					for(int z = startZ ; z <= endZ ; z++) {
						
						Block hitBlock = projectile.getWorld().getBlockAt(x, y, z);
						if(!blockBlacklist.contains(hitBlock.getType()) && hitBlock.isSolid() && hitBlock.getBoundingBox().overlaps(box)) {
							
							cancelled = false;
							iProj.Hit(null, hitBlock, this);
							return !cancelled;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	//
	
	protected void SetTriggered(boolean triggered) {
		this.triggered = triggered;
	}
	protected void ProjectileHitBlock(Block hitBlock) {
		if(targetBlock) iProj.Hit(null, hitBlock, this);
	}
	
	//
	
	/**
	 * Si il faut continuer � traiter l'entit�e comme un projectile
	 * @param cancelled
	 */
	public void SetCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	//
	
	/**
	 * 
	 * @return L'entit� utilis�e comme projectile
	 */
	public Entity GetProjectile() {
		return projectile;
	}
	/**
	 * 
	 * @return L'interface utilis�e pour le projectile
	 */
	public IProjectile GetInterface() {
		return iProj;
	}
	/**
	 * 
	 * @return Le lanceur du projectile
	 */
	public LivingEntity GetSender() {
		return sender;
	}

}
