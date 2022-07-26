package fr.nekotine.core.effect;

import org.bukkit.entity.LivingEntity;

public interface CustomEffectType {

	public abstract void onApply(LivingEntity target);
	
	public abstract void onUnapply(LivingEntity target);
	
	public abstract void onAmplifierChange(LivingEntity target, int lastAmplifier, int newAmplifier);
	
	public abstract boolean haveAmplifier();
	
}
