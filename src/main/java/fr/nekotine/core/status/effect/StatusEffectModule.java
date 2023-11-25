package fr.nekotine.core.status.effect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.ticking.TickingModule;
import fr.nekotine.core.ticking.event.TickElapsedEvent;
import fr.nekotine.core.util.EventUtil;

public class StatusEffectModule extends PluginModule implements Listener{
	
	private final Map<LivingEntity,Map<StatusEffectType, List<AppliedStatusEffect>>> effectMap = new WeakHashMap<>();
	
	public StatusEffectModule() {
		Ioc.resolve(ModuleManager.class).tryLoad(TickingModule.class);
		EventUtil.register(this);
	}
	
	@Override
	protected void unload() {
		EventUtil.unregister(this);
		super.unload();
	}
	
	public void addEffect(LivingEntity entity, StatusEffect effect) {
		var entityMap = effectMap.computeIfAbsent(entity, e -> new HashMap<>());
		var list = entityMap.get(effect.type());
		var applied = new AppliedStatusEffect(effect);
		if (list != null) {
			list.add(applied);
			return;
		}
		list = entityMap.compute(effect.type(), (e,old) -> new LinkedList<>());
		list.add(applied);
		effect.type().applyEffect(entity);
		return;
		
	}
	
	public void removeEffect(LivingEntity entity, StatusEffect effect) {
		var entityMap = effectMap.get(entity);
		if (entityMap == null) {
			return;
		}
		var list = entityMap.get(effect.type());
		if (list == null) {
			return;
		}
		list.removeIf(a -> a.source==effect);
		if (list.isEmpty()) {
			effect.type().removeEffect(entity);
			entityMap.remove(effect.type());
		}
	}
	
	@EventHandler
	public void onTick(TickElapsedEvent event) {
		for (var entity : effectMap.keySet()) {
			var entityMap = effectMap.get(entity);
			for (var effect : entityMap.keySet()) {
				var list = entityMap.get(effect);
				if (list == null) {
					continue;
				}
				list.removeIf(a -> (--a.durationLeft) == 0);
				if (list.isEmpty()) {
					effect.removeEffect(entity);
					entityMap.remove(effect);
				}
			}
		}
	}
	
	private static class AppliedStatusEffect{
		
		private final StatusEffect source;
		
		private int durationLeft;
		
		private AppliedStatusEffect(StatusEffect source) {
			this.source = source;
			durationLeft = source.duration();
		}
		
	}
	
}
