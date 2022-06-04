package fr.nekotine.core.projectile;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public interface IProjectile {
	
	/**
	 * Appelée lorsque le projectile touche une LivingEntity ou un Block
	 */
	public void Hit(LivingEntity hitEntity, Block hitBlock, CustomProjectile projectile);

	/**
	 * Appelée lorsque la durée de vie du projectile est dépassée
	 */
	public void Faded(CustomProjectile projectile);
}
