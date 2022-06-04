package fr.nekotine.core.projectile;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public interface IProjectile {
	
	/**
	 * Appel�e lorsque le projectile touche une LivingEntity ou un Block
	 */
	public void Hit(LivingEntity hitEntity, Block hitBlock, CustomProjectile projectile);

	/**
	 * Appel�e lorsque la dur�e de vie du projectile est d�pass�e
	 */
	public void Faded(CustomProjectile projectile);
}
