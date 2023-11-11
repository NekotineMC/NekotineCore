package fr.nekotine.core.state;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

public class PotionEffectState implements ItemState<LivingEntity>{

	private final PotionEffect effect;
	
	public PotionEffectState(PotionEffect effect) {
		this.effect = effect;
	}
	
	@Override
	public void setup(LivingEntity item) {
		item.addPotionEffect(effect);
	}

	@Override
	public void teardown(LivingEntity item) {
		item.removePotionEffect(effect.getType());
	}

}
