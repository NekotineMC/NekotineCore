package fr.nekotine.core.util;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import fr.nekotine.core.inventory.InventorySnapshot;
import fr.nekotine.core.inventory.PlayerInventorySnapshot;

/**
 * Classe utilitaire pour les inventaires minecraft
 * 
 * @author XxGoldenbluexX
 *
 */
public class UtilInventory {

	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie profonde, les {@link org.bukkit.inventory.ItemStack ItemStack} sont ceux de l'inventaire et peuvent
	 * être modifiés.
	 * @see UtilInventory#deepSnapshot
	 * @param holder
	 * @return la snapshot
	 */
	public static InventorySnapshot snapshot(InventoryHolder holder) {
		var snap = new InventorySnapshot();
		snap.snapshot(holder.getInventory());
		return snap;
	}
	
	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie de surface, les {@link org.bukkit.inventory.ItemStack ItemStack} sont clonés.
	 * @see UtilInventory#snapshot
	 * @param holder
	 * @return la snapshot
	 */
	public static InventorySnapshot deepSnapshot(InventoryHolder holder) {
		var snap = new InventorySnapshot();
		snap.deepSnapshot(holder.getInventory());
		return snap;
	}
	
	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie profonde, les {@link org.bukkit.inventory.ItemStack ItemStack} sont ceux de l'inventaire et peuvent
	 * être modifiés.
	 * @see UtilInventory#deepSnapshot
	 * @param holder
	 * @return la snapshot
	 */
	public static PlayerInventorySnapshot snapshot(HumanEntity holder) {
		var snap = new PlayerInventorySnapshot();
		snap.snapshot(holder.getInventory());
		return snap;
	}
	
	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie de surface, les {@link org.bukkit.inventory.ItemStack ItemStack} sont clonés.
	 * @see UtilInventory#snapshot
	 * @param holder
	 * @return la snapshot
	 */
	public static PlayerInventorySnapshot deepSnapshot(HumanEntity holder) {
		var snap = new PlayerInventorySnapshot();
		snap.deepSnapshot(holder.getInventory());
		return snap;
	}
	
	/**
	 * Remplis l'inventaire avec les {@link org.bukkit.inventory.ItemStack ItemStack} de la snapshot.
	 * @param holder
	 * @param snapshot
	 */
	public static void fill(InventoryHolder holder, InventorySnapshot snapshot) {
		snapshot.fill(holder.getInventory());
	}
	/**
	 * Remplis l'inventaire avec les {@link org.bukkit.inventory.ItemStack ItemStack} de la snapshot.
	 * @param holder
	 * @param snapshot
	 */
	public static void fill(HumanEntity holder, PlayerInventorySnapshot snapshot) {
		snapshot.fill(holder.getInventory());
	}
	
}
