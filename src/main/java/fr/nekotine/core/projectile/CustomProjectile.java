package fr.nekotine.core.projectile;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class CustomProjectile {
	private Entity projectile;
	private LivingEntity sender;
	private IProjectile iProj;
	private long expireTime;
	private boolean targetPlayer;
	private boolean targetBlock;
	
	public CustomProjectile(Entity projectile, LivingEntity sender, IProjectile iProj, Vector velocity, long expireTime, boolean targetPlayer,
			boolean targetBlock) {
		this.projectile = projectile;
		this.sender = sender;
		this.iProj = iProj;
		this.expireTime = expireTime;
		this.targetPlayer = targetPlayer;
		this.targetBlock = targetBlock;
		
		projectile.setVelocity(velocity);
	}
	
	public boolean Collision(/* inserer tick event ici */) {
		if(expireTime != -1 && System.currentTimeMillis() >  expireTime) {
				iProj.Faded(this);
				return true;
		}
		
		return false;
	}
}
