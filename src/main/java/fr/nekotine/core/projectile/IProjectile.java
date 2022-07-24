package fr.nekotine.core.projectile;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public interface IProjectile {
	
	/**
	 * Lorsque le projectile a touch� une entit�e ou un block
	 * @param hitEntity 
	 * @param hitBlock
	 * @param projectile
	 */
	public void Hit(LivingEntity hitEntity, Block hitBlock, CustomProjectile projectile);

	/**
	 *Lorsque le projectile a atteint sa dur�e de vie maximale
	 * @param projectile
	 */
	public void Faded(CustomProjectile projectile);
	
	/**
	 * Lorsque le projectile a manuellement �t� d�clench�
	 * @param projectile
	 */
	public void Triggered(CustomProjectile projectile);
}
