package fr.nekotine.core.projectile;

import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class CustomProjectile {
	private Entity projectile;
	private LivingEntity sender;
	private IProjectile iProj;
	
	private long expireTime;
	
	private boolean targetLivingEntity;
	private boolean targetBlock;
	
	private final double DISTANCE_TO_HIT_BLOCK = 0.5;
	
	public CustomProjectile(Entity projectile, LivingEntity sender, IProjectile iProj, Vector velocity, long expireTime, boolean targetLivingEntity,
			boolean targetBlock) {
		this.projectile = projectile;
		this.sender = sender;
		this.iProj = iProj;
		this.expireTime = expireTime;
		this.targetLivingEntity = targetLivingEntity;
		this.targetBlock = targetBlock;
		
		ConfigureProjectile(projectile, velocity);
	}
	
	private void ConfigureProjectile(Entity projectile, Vector velocity) {
		projectile.setInvulnerable(true);
		projectile.setVelocity(velocity);
		if(projectile instanceof Item) {
			Item itemProjectile = (Item)projectile;
			itemProjectile.setCanMobPickup(false);
			itemProjectile.setCanPlayerPickup(false);
		}
	}
	
	/**
	 * @return Vrai si le projectile est expiré/collision
	 */
	public boolean Collision() {
		
		if(expireTime != -1 && System.currentTimeMillis() >  expireTime) {
			iProj.Faded(this);
			return true;
		}
		
		if(targetLivingEntity) {
			double boundingSizeX = projectile.getBoundingBox().getWidthX();
			double boundingSizeY = projectile.getBoundingBox().getHeight();
			double boundingSizeZ = projectile.getBoundingBox().getWidthZ();
			
			for(Entity hitEntity : projectile.getNearbyEntities(boundingSizeX / 2, boundingSizeY / 2, boundingSizeZ / 2)) {
				if(hitEntity instanceof LivingEntity) {
					LivingEntity hitLivingEntity = (LivingEntity)hitEntity;
					
					if(hitLivingEntity.equals(sender)) continue;
					
					if(hitLivingEntity instanceof Player) {
						Player hitPlayer = (Player)hitLivingEntity;
						if(hitPlayer.getGameMode() == GameMode.CREATIVE || hitPlayer.getGameMode() == GameMode.SPECTATOR) continue;
					}
					
					iProj.Hit(hitLivingEntity, null, this);
					return true;
				}	
			}
		}
		
		if(targetBlock) {
			RayTraceResult result = projectile.getWorld().rayTraceBlocks(projectile.getLocation(), projectile.getVelocity(), DISTANCE_TO_HIT_BLOCK);
			if(result.getHitBlock() != null && result.getHitBlock().isSolid()) {
				iProj.Hit(null, result.getHitBlock(), this);
				return true;
			}
		}
		
		
		return false;
	}
	
	/**
	 * @return La Entity utilisée comme projectile
	 */
	public @NotNull Entity GetProjectile() {
		return projectile;
	}
	
	/**
	 * 
	 * @return La LivingEntity qui a lancée le projectile
	 */
	public @Nullable LivingEntity GetSender() {
		return sender;
	}

}
