package fr.nekotine.core.effect;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.ticking.TickingModule;
import fr.nekotine.core.ticking.event.TickElapsedEvent;

public class CustomEffectModule extends PluginModule implements Listener{

	private final Map<LivingEntity,CustomEffectContainer> effectMap = new WeakHashMap<>();
	
	public CustomEffectModule() {
		NekotineCore.MODULES.load(TickingModule.class);
	}
	
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
	
	public void hasEffect(LivingEntity entity, CustomEffectType type) {
		getContainer(entity).hasEffect(type);
	}
	
	@EventHandler
	public void onTick(TickElapsedEvent event) {
		for (CustomEffectContainer container : effectMap.values()) {
			container.tick();
		}
	}
	
}
