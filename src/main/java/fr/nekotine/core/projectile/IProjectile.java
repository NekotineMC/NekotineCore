package fr.nekotine.core.projectile;

import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public interface IProjectile {
	
	/**
	 * Appelée lorsque le projectile touche une LivingEntity ou un Block
	 */
	public void Hit(@Nullable LivingEntity hitEntity, @Nullable Block hitBlock, CustomProjectile projectile);

	/**
	 * Appelée lorsque la durée de vie du projectile est dépassée
	 */
	public void Faded(CustomProjectile projectile);
}
