package fr.nekotine.core.snapshot;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class PlayerStatusSnaphot implements Snapshot<Player>{
	
	private GameMode gamemode;
	
	private Location location;
	
	private int experiencePoints;
	
	private boolean allowFlight;
	
	private float flySpeed;
	
	private float walkSpeed;
	
	private Snapshot<PlayerInventory> inventorySnapshot;
	
	private Snapshot<Attributable> attributes;
	
	private Snapshot<LivingEntity> potionEffects;
	
	private Snapshot<Damageable> healthSnapshot;
	
	private Snapshot<HumanEntity> hungerSnapshot;
	
	/**
	 * @implNote c'est l'équivalent du {@link fr.nekotine.core.snapshot.PlayerStatusSnaphot#deepSnapshot deepSnapshot}
	 */
	@Override
	public Snapshot<Player> snapshot(Player item) {
		deepSnapshot(item);
		return this;
	}

	@Override
	public Snapshot<Player> deepSnapshot(Player item) {
		gamemode = item.getGameMode();
		location = item.getLocation().clone();
		inventorySnapshot = new PlayerInventorySnapshot().deepSnapshot(item.getInventory());
		attributes = new AttributableSnapshot().deepSnapshot(item);
		potionEffects = new PotionEffectConsumerSnapshot().deepSnapshot(item);
		healthSnapshot = new DamageableSnapshot().deepSnapshot(item);
		hungerSnapshot = new HungerableSnapshot().deepSnapshot(item);
		experiencePoints = item.getTotalExperience();
		allowFlight = item.getAllowFlight();
		flySpeed = item.getFlySpeed();
		walkSpeed = item.getWalkSpeed();
		return this;
	}

	@Override
	public void patch(Player item) {
		item.setGameMode(gamemode);
		item.teleport(location);
		inventorySnapshot.patch(item.getInventory());
		attributes.patch(item);
		potionEffects.patch(item);
		healthSnapshot.patch(item);
		hungerSnapshot.patch(item);
		item.setTotalExperience(experiencePoints);
		item.setAllowFlight(allowFlight);
		item.setFlySpeed(flySpeed);
		item.setWalkSpeed(walkSpeed);
	}

}
