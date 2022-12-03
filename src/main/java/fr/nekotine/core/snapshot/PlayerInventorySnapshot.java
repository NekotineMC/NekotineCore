package fr.nekotine.core.snapshot;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInventorySnapshot implements Snapshot<PlayerInventory>{
	
	protected ItemStack[] content;
	
	private Map<EquipmentSlot, ItemStack> equipmentSlots = new HashMap<>();
	
	/**
	 * Pour éviter de sauvegarder la main hand et de la recharger.
	 */
	private static EnumSet<EquipmentSlot> savedSlots = EnumSet.of(
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET,
			EquipmentSlot.OFF_HAND
			);
	
	public Snapshot<PlayerInventory> snapshot(PlayerInventory inventory) {
		content = inventory.getContents();
		for (EquipmentSlot slot : savedSlots) {
			equipmentSlots.put(slot, inventory.getItem(slot));
		}
		return this;
	}
	
	public Snapshot<PlayerInventory> deepSnapshot(PlayerInventory inventory) {
		content = Arrays.asList(inventory.getContents()).stream().map(base -> base != null ? base.clone() : base).toArray(ItemStack[]::new);
		for (EquipmentSlot slot : savedSlots) {
			equipmentSlots.put(slot, inventory.getItem(slot).clone());
		}
		return this;
	}
	
	public void patch(PlayerInventory inventory) {
		inventory.setContents(content);
		for (EquipmentSlot slot : savedSlots) {
			inventory.setItem(slot, equipmentSlots.get(slot));
		}
	}
	
}
