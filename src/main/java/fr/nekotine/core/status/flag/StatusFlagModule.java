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
		if (map.computeIfAbsent(entity, e -> new HashSet<>()).add(flag)) {
			flag.applyStatus(entity);
			return true;
		}
		return false;
	}
	
	public void addFlags(LivingEntity entity, StatusFlag...flags) {
		for (var f : flags) {
			addFlag(entity, f);
		}
	}
	
	public void addFlags(LivingEntity entity, Set<StatusFlag> flags) {
		for (var f : flags) {
			addFlag(entity, f);
		}
	}
	
	/**
	 * 
	 * @param entity
	 * @param flag
	 * @return flag supprimé
	 */
	public boolean removeFlag(LivingEntity entity, StatusFlag flag) {
		var set = map.get(entity);
		if (set == null) {
			return false;
		}
		if (set.remove(flag)) {
			flag.removeStatus(entity);
			if (set.isEmpty()) {
				map.remove(entity);
			}
			return true;
		}
		return false;
	}
	
	public void removeFlags(LivingEntity entity, StatusFlag...flags) {
		for (var f : flags) {
			removeFlag(entity, f);
		}
	}
	
	public void removeFlags(LivingEntity entity, Set<StatusFlag> flags) {
		for (var f : flags) {
			removeFlag(entity, f);
		}
	}
	
	public boolean hasAny(LivingEntity entity, StatusFlag...flags) {
		var set = map.get(entity);
		if (set  == null) {
			return false;
		}
		for (var flag : flags) {
			if (set.contains(flag)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAny(LivingEntity entity, Set<StatusFlag> flags) {
		var set = map.get(entity);
		if (set  == null) {
			return false;
		}
		for (var flag : flags) {
			if (set.contains(flag)) {
				return true;
			}
		}
		return false;
	}
	
}
