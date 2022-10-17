package fr.nekotine.core.snapshot;

import java.util.Collection;

import org.bukkit.advancement.AdvancementProgress;
/**
 * Capture d'un {@link org.bukkit.advancement.AdvancementProgress AdvancementProgress}
 * Ne sauvegarde pas la date d'obtention de chaque critères (l'API ne permet pas des définir la date d'obtention).
 * @author XxGoldenbluexX
 *
 */
public class AdvancementProgressSnapshot implements Snapshot<AdvancementProgress>{

	private Collection<String> awardedCriterias;
	
	/**
	 * @implNote c'est l'équivalent du {@link fr.nekotine.core.snapshot.PlayerStatusSnaphot#deepSnapshot deepSnapshot}
	 */
	@Override
	public Snapshot<AdvancementProgress> snapshot(AdvancementProgress item) {
		deepSnapshot(item);
		return this;
	}

	@Override
	public Snapshot<AdvancementProgress> deepSnapshot(AdvancementProgress item) {
		awardedCriterias = item.getAwardedCriteria();
		return this;
	}

	@Override
	public void patch(AdvancementProgress item) {
		for (var criteriaName : item.getAwardedCriteria()) {
			item.revokeCriteria(criteriaName);
		}
		for (var criteriaName : awardedCriterias) {
			item.awardCriteria(criteriaName);
		}
	}
}
