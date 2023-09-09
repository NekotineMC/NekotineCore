package fr.nekotine.core.status.flag;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.entity.LivingEntity;

import fr.nekotine.core.module.PluginModule;

public class StatusFlagModule extends PluginModule{

	private final Map<LivingEntity, Set<StatusFlag>> map = new WeakHashMap<>();
	
	/**
	 * Ajoute un flag
	 * @param entity
	 * @param flag
	 * @return flag ajouté
	 */
	public boolean addFlag(LivingEntity entity, StatusFlag flag) {
		return map.computeIfAbsent(entity, e -> new HashSet<>()).add(flag);
	}
	
	public void addFlags(LivingEntity entity, StatusFlag...flags) {
		for (var f : flags) {
			addFlag(entity, f);
		}
	}
	
	//TODO HERE
	
}
