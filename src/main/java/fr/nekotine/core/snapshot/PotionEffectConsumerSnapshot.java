package fr.nekotine.core.snapshot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

public class PotionEffectConsumerSnapshot implements Snapshot<LivingEntity>{

	List<Map<String, Object>> serializedeffects;
	
	@Override
	public Snapshot<LivingEntity> snapshot(LivingEntity item) {
		deepSnapshot(item);
		return this;
	}

	@Override
	public Snapshot<LivingEntity> deepSnapshot(LivingEntity item) {
		serializedeffects = new LinkedList<>();
		for (var e : item.getActivePotionEffects()) {
			serializedeffects.add(e.serialize());
		}
		return this;
	}

	@Override
	public void patch(LivingEntity item) {
		for (var effect : item.getActivePotionEffects()) {
			item.removePotionEffect(effect.getType());
		}
		if (serializedeffects != null) {
			for (var effect : serializedeffects) {
				item.addPotionEffect(new PotionEffect(effect));
			}
		}
	}

}
