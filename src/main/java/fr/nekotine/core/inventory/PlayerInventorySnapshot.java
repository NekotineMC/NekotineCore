package fr.nekotine.core.inventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInventorySnapshot extends InventorySnapshot{
	
	private Map<EquipmentSlot, ItemStack> equipmentSlots = new HashMap<>();
	
	public void snapshot(PlayerInventory inventory) {
		content = inventory.getContents();
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			equipmentSlots.put(slot, inventory.getItem(slot));
		}
	}
	
	public void deepSnapshot(PlayerInventory inventory) {
		content = Arrays.asList(inventory.getContents()).stream().map(base -> base.clone()).toArray(ItemStack[]::new);
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			equipmentSlots.put(slot, inventory.getItem(slot).clone());
		}
	}
	
	public void fill(PlayerInventory inventory) {
		inventory.setContents(content);
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			inventory.setItem(slot, equipmentSlots.get(slot));
		}
	}
	
}
