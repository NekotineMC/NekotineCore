package fr.nekotine.core.status.effect;

/**
 * 
 * Duration = durée de l'effet en tick, -1 pour une durée infinie
 * 
 * @author XxGoldenbluexX
 *
 *
 */
public record StatusEffect(StatusEffectType type, int duration) {
}
