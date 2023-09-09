package fr.nekotine.core.status.effect;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.LivingEntity;

public class CustomEffectContainer {

	private final Map<CustomEffectType, List<AppliedEffect>> effectMap;
	
	private final LivingEntity _owner;
	
	private final Comparator<AppliedEffect> amplifierComparator;
	
	protected CustomEffectContainer(LivingEntity owner) {
		effectMap = new HashMap<>();
		_owner = owner;
		amplifierComparator = new Comparator<AppliedEffect>() {
			@Override
			public int compare(AppliedEffect o1, AppliedEffect o2) {
				return o2.reference.getAmplifier() - o1.reference.getAmplifier();
			}
		};
	}
	
	protected void addEffect(CustomEffect effect) {
		AppliedEffect appliedEffect = new AppliedEffect(effect, this);
		CustomEffectType type = effect.getType();
		if (!effectMap.containsKey(type)) {
			effectMap.put(type, new LinkedList<AppliedEffect>());
		}
		List<AppliedEffect> list = effectMap.get(type);
		if (!list.isEmpty()) {
			if (type.haveAmplifier() && effect.getAmplifier() != list.get(0).reference.getAmplifier()) {
				type.onAmplifierChange(_owner, list.get(list.size()).reference.getAmplifier(), effect.getAmplifier());
			}
		}else {
			type.onApply(_owner);
		}
		list.add(appliedEffect);
		if (type.haveAmplifier()) {
			list.sort(amplifierComparator);
		}
	}
	
	
	protected void removeEffect(CustomEffect effect) {
		CustomEffectType type = effect.getType();
		if (effectMap.containsKey(type)) {
			List<AppliedEffect> list = effectMap.get(type);
			list.removeIf((eff) -> eff.reference.equals(effect));
			if (!list.isEmpty()) {
				if (list.get(0).reference.getAmplifier() < effect.getAmplifier()) {
					type.onAmplifierChange(_owner, effect.getAmplifier(), list.get(0).reference.getAmplifier());
				}
			}else {
				type.onUnapply(_owner);
			}
		}
	}
	
	protected void clearEffect(CustomEffectType type) {
		if (effectMap.containsKey(type)) {
			if (!effectMap.get(type).isEmpty()) {
				type.onUnapply(_owner);
			}
		}
		effectMap.remove(type);
	}
	
	protected boolean hasEffect(CustomEffectType type) {
		return effectMap.containsKey(type) && !effectMap.get(type).isEmpty();
	}
	protected void tick() {
		for (List<AppliedEffect> effList : effectMap.values()) {
			for (AppliedEffect eff : effList) {
				eff.tick();
			}
		}
	}
	
	private class AppliedEffect{
		
		private CustomEffect reference;
		
		private CustomEffectContainer container;
		
		private int remainingTicks;
		
		private AppliedEffect(CustomEffect reference, CustomEffectContainer container) {
			remainingTicks = reference.getDuration();
			this.container = container;
		}
		
		private void tick() {
			remainingTicks--;
			if (remainingTicks <= 0) {
				container.removeEffect(reference);
			}
		}
		
	}
	
}
