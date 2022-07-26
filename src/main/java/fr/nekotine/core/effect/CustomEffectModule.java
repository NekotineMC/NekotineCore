package fr.nekotine.core.effect;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.LivingEntity;

import fr.nekotine.core.module.PluginModule;

public class CustomEffectModule extends PluginModule{

	private final Map<LivingEntity,CustomEffectContainer> effectMap = new WeakHashMap<>();
	
	private CustomEffectContainer getContainer(LivingEntity entity) {
		if (effectMap.containsKey(entity)) {
			return effectMap.get(entity);
		}
		CustomEffectContainer container = new CustomEffectContainer(entity);
		effectMap.put(entity, container);
		return container;
	}
	
	public void addEffect(LivingEntity entity, CustomEffect effect) {
		getContainer(entity).addEffect(effect);
	}
	
	public void removeEffect(LivingEntity entity, CustomEffect effect) {
		getContainer(entity).removeEffect(effect);
	}
	
	public void clearEffect(LivingEntity entity, CustomEffectType type) {
		getContainer(entity).clearEffect(type);
	}
	
	@Override
	protected void onEnable() {
		super.onEnable();
	}
	
	@Override
	protected void onDisable() {
		super.onDisable();
	}
	
}
