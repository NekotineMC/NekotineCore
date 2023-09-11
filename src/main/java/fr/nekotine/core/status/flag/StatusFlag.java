package fr.nekotine.core.status.flag;

import org.bukkit.entity.LivingEntity;

public interface StatusFlag {

	public void applyStatus(LivingEntity appliedTo);
	
	public void removeStatus(LivingEntity appliedTo);
	
}
