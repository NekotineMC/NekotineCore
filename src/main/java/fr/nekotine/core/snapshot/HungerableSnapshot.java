package fr.nekotine.core.snapshot;

import org.bukkit.entity.HumanEntity;

public class HungerableSnapshot implements Snapshot<HumanEntity> {

	private int foodLevel;
	
	private float saturation;
	
	/**
	 * @implNote c'est l'équivalent du {@link fr.nekotine.core.snapshot.PlayerStatusSnaphot#deepSnapshot deepSnapshot}
	 */
	@Override
	public Snapshot<HumanEntity> snapshot(HumanEntity item) {
		return this;
	}

	@Override
	public Snapshot<HumanEntity> deepSnapshot(HumanEntity item) {
		foodLevel = item.getFoodLevel();
		saturation = item.getSaturation();
		return this;
	}

	@Override
	public void patch(HumanEntity item) {
		item.setFoodLevel(foodLevel);
		item.setSaturation(saturation);
	}

}
