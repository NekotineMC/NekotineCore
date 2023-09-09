package fr.nekotine.core.visibility;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.nekotine.core.status.effect.CustomEffectType;

public class UnrenderedEffectType implements CustomEffectType{

	private final Player _unrenderedFor;
	
	private final EntityVisibilityModule _visibilityModule;
	
	private UnrenderedEffectType(EntityVisibilityModule visibilityModule, Player unrenderedFor) {
		_unrenderedFor = unrenderedFor;
		_visibilityModule = visibilityModule;
	}
	
	@Override
	public void onApply(LivingEntity target) {
		_visibilityModule.hideFrom(target, _unrenderedFor);
	}

	@Override
	public void onUnapply(LivingEntity target) {
		_visibilityModule.showFrom(target, _unrenderedFor);
	}

	@Override
	public void onAmplifierChange(LivingEntity target, int lastAmplifier, int newAmplifier) {
	}

	@Override
	public boolean haveAmplifier() {
		return false;
	}
	
	public static UnrenderedEffectType UnrenderedFor(EntityVisibilityModule visibilityModule, Player unrenderedFor) {
		return new UnrenderedEffectType(visibilityModule, unrenderedFor);
	}
}
