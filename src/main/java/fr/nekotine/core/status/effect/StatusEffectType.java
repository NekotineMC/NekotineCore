package fr.nekotine.core.status.effect;

import org.bukkit.entity.LivingEntity;

public interface StatusEffectType {

	public abstract void applyEffect(LivingEntity target);
	
	public abstract void removeEffect(LivingEntity target);
	
}
