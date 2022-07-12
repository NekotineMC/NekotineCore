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

import fr.nekotine.core.util.UtilTime;

public class CustomProjectile {
	private final Entity projectile;
	private final LivingEntity sender;
	private final IProjectile iProj;
	private final long expireTime;
	private final boolean targetLivingEntity;
	private final boolean targetBlock;
	
	private final long startedTime;
	private boolean triggered;
	private final double DISTANCE_TO_HIT_BLOCK = 0.5;
	
	public CustomProjectile(Entity projectile, LivingEntity sender, IProjectile iProj, Vector velocity, long expireTime, boolean targetLivingEntity,
			boolean targetBlock) {
		this.projectile = projectile;
		this.sender = sender;
		this.iProj = iProj;
		this.expireTime = expireTime;
		this.targetLivingEntity = targetLivingEntity;
		this.targetBlock = targetBlock;
		
		this.startedTime = UtilTime.GetTime();
		this.triggered = false;
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
	
	//
	
	/**
	 * @return Vrai si le projectile est expir�/collision
	 */
	public boolean Collision() {
		if(triggered) {
			iProj.Triggered(this);
			return triggered;
		}
		if(expireTime != -1 && UtilTime.Difference(UtilTime.GetTime(), startedTime) >  expireTime) {
			triggered = true;
			iProj.Faded(this);
			return triggered;
		}
		
		if(targetLivingEntity) {
			for(Entity hitEntity : projectile.getNearbyEntities(0,0, 0)) {
				if(hitEntity instanceof LivingEntity) {
					LivingEntity hitLivingEntity = (LivingEntity)hitEntity;
					
					if(hitLivingEntity.equals(sender)) continue;
					
					if(hitLivingEntity instanceof Player) {
						Player hitPlayer = (Player)hitLivingEntity;
						if(hitPlayer.getGameMode() == GameMode.CREATIVE || hitPlayer.getGameMode() == GameMode.SPECTATOR) continue;
					}
					
					triggered = true;
					iProj.Hit(hitLivingEntity, null, this);
					return triggered;
				}	
			}
		}
		
		if(targetBlock) {
			RayTraceResult result = projectile.getWorld().rayTraceBlocks(projectile.getLocation(), projectile.getVelocity(), DISTANCE_TO_HIT_BLOCK);
			if(result!=null && result.getHitBlock() != null && result.getHitBlock().isSolid()) {
				
				triggered = true;
				iProj.Hit(null, result.getHitBlock(), this);
				return triggered;
			}
		}
		
		
		return false;
	}
	
	//
	
	/**
	 * 
	 * @param triggered Si le projectile doit être supprimé
	 */
	public void SetTriggered(boolean triggered) {
		this.triggered = triggered;
	}
	/**
	 * @return La Entity utilis�e comme projectile
	 */
	public @NotNull Entity GetProjectile() {
		return projectile;
	}
	
	/**
	 * 
	 * @return La LivingEntity qui a lanc�e le projectile
	 */
	public @Nullable LivingEntity GetSender() {
		return sender;
	}

}
