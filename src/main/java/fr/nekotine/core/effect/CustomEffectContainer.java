package fr.nekotine.core.effect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.LivingEntity;

public class CustomEffectContainer {

	private final Map<CustomEffectType, List<CustomEffect>> effectMap;
	
	private final LivingEntity _owner;
	
	private final Comparator<CustomEffect> amplifierComparator;
	
	public CustomEffectContainer(LivingEntity owner) {
		effectMap = new HashMap<>();
		_owner = owner;
		amplifierComparator = new Comparator<CustomEffect>() {
			@Override
			public int compare(CustomEffect o1, CustomEffect o2) {
				return o2.getAmplifier() - o1.getAmplifier();
			}
		};
	}
	
	public void addEffect(CustomEffect effect) {
		CustomEffectType type = effect.getType();
		if (!effectMap.containsKey(type)) {
			effectMap.put(type, new ArrayList<CustomEffect>(3));
		}
		List<CustomEffect> list = effectMap.get(type);
		if (!list.isEmpty()) {
			if (type.haveAmplifier() && effect.getAmplifier() != list.get(0).getAmplifier()) {
				type.onAmplifierChange(_owner, list.get(list.size()).getAmplifier(), effect.getAmplifier());
			}
		}else {
			type.onApply(_owner);
		}
		list.add(effect);
		if (type.haveAmplifier()) {
			list.sort(amplifierComparator);
		}
	}
	
	
	public void removeEffect(CustomEffect effect) {
		CustomEffectType type = effect.getType();
		if (effectMap.containsKey(type)) {
			List<CustomEffect> list = effectMap.get(type);
			list.remove(effect);
			if (!list.isEmpty()) {
				if (list.get(0).getAmplifier() < effect.getAmplifier()) {
					type.onAmplifierChange(_owner, effect.getAmplifier(), list.get(0).getAmplifier());
				}
			}else {
				type.onUnapply(_owner);
			}
		}
	}
	
	public void clearEffect(CustomEffectType type) {
		if (effectMap.containsKey(type)) {
			if (!effectMap.get(type).isEmpty()) {
				type.onUnapply(_owner);
			}
		}
		effectMap.remove(type);
	}
	
}
