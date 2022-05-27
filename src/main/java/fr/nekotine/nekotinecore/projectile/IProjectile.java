package fr.nekotine.nekotinecore.projectile;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public interface IProjectile {
	
	//Can be null
	public void Hit(LivingEntity hitEntity, Block hitBlock, CustomProjectile projectile);
	
	public void Faded(CustomProjectile projectile);
}
