package fr.nekotine.core.inventory;

import java.util.Arrays;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventorySnapshot{
	
	protected ItemStack[] content;
	
	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie profonde, les {@link org.bukkit.inventory.ItemStack ItemStack} sont ceux de l'inventaire et peuvent
	 * être modifiés
	 * @see InventorySnapshot#deepSnapshot
	 * @param holder
	 * @return la snapshot
	 */
	public void snapshot(Inventory inventory) {
		content = inventory.getContents();
	}
	
	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie de surface, les {@link org.bukkit.inventory.ItemStack ItemStack} sont clonés.
	 * @see InventorySnapshot#snapshot
	 * @param holder
	 * @return la snapshot
	 */
	public void deepSnapshot(Inventory inventory) {
		content = Arrays.asList(inventory.getContents()).stream().map(base -> base.clone()).toArray(ItemStack[]::new);
	}
	
	public void fill(Inventory inventory) {
		inventory.setContents(content);
	}
	
}
