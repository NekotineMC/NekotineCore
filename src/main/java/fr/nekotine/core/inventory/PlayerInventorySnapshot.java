package fr.nekotine.core.inventory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInventorySnapshot extends InventorySnapshot{
	
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
	
	public void snapshot(PlayerInventory inventory) {
		content = inventory.getContents();
		for (EquipmentSlot slot : savedSlots) {
			equipmentSlots.put(slot, inventory.getItem(slot));
		}
	}
	
	public void deepSnapshot(PlayerInventory inventory) {
		content = Arrays.asList(inventory.getContents()).stream().map(base -> base.clone()).toArray(ItemStack[]::new);
		for (EquipmentSlot slot : savedSlots) {
			equipmentSlots.put(slot, inventory.getItem(slot).clone());
		}
	}
	
	public void fill(PlayerInventory inventory) {
		inventory.setContents(content);
		for (EquipmentSlot slot : savedSlots) {
			inventory.setItem(slot, equipmentSlots.get(slot));
		}
	}
	
}
