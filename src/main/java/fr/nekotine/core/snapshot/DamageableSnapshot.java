package fr.nekotine.core.snapshot;

import org.bukkit.entity.Damageable;

public class DamageableSnapshot implements Snapshot<Damageable>{

	private double health;
	
	private double absorption;
	
	/**
	 * @implNote c'est l'équivalent du {@link fr.nekotine.core.snapshot.DamageableSnapshot#deepSnapshot deepSnapshot}
	 */
	@Override
	public Snapshot<Damageable> snapshot(Damageable item) {
		deepSnapshot(item);
		return this;
	}

	@Override
	public Snapshot<Damageable> deepSnapshot(Damageable item) {
		health = item.getHealth();
		absorption = item.getAbsorptionAmount();
		return this;
	}

	@Override
	public void patch(Damageable item) {
		item.setHealth(health);
		item.setAbsorptionAmount(absorption);
	}

}
