package fr.nekotine.core.util;

import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.nekotine.core.snapshot.InventorySnapshot;
import fr.nekotine.core.snapshot.PlayerInventorySnapshot;
import fr.nekotine.core.snapshot.Snapshot;

/**
 * Classe utilitaire pour les inventaires minecraft
 * 
 * @author XxGoldenbluexX
 *
 */
public class InventoryUtil {

	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie profonde, les {@link org.bukkit.inventory.ItemStack ItemStack} sont ceux de l'inventaire et peuvent
	 * être modifiés.
	 * @see InventoryUtil#deepSnapshot
	 * @param holder
	 * @return la snapshot
	 */
	public static Snapshot<Inventory> snapshot(InventoryHolder holder) {
		var snap = new InventorySnapshot();
		snap.snapshot(holder.getInventory());
		return snap;
	}
	
	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie de surface, les {@link org.bukkit.inventory.ItemStack ItemStack} sont clonés.
	 * @see InventoryUtil#snapshot
	 * @param holder
	 * @return la snapshot
	 */
	public static Snapshot<Inventory> deepSnapshot(InventoryHolder holder) {
		var snap = new InventorySnapshot();
		snap.deepSnapshot(holder.getInventory());
		return snap;
	}
	
	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie profonde, les {@link org.bukkit.inventory.ItemStack ItemStack} sont ceux de l'inventaire et peuvent
	 * être modifiés.
	 * @see InventoryUtil#deepSnapshot
	 * @param holder
	 * @return la snapshot
	 */
	public static Snapshot<PlayerInventory> snapshot(HumanEntity holder) {
		var snap = new PlayerInventorySnapshot();
		snap.snapshot(holder.getInventory());
		return snap;
	}
	
	/**
	 * Prend une capture de tous les ItemStack de l'inventaire.
	 * Contrairement à une copie de surface, les {@link org.bukkit.inventory.ItemStack ItemStack} sont clonés.
	 * @see InventoryUtil#snapshot
	 * @param holder
	 * @return la snapshot
	 */
	public static Snapshot<PlayerInventory> deepSnapshot(HumanEntity holder) {
		var snap = new PlayerInventorySnapshot();
		snap.deepSnapshot(holder.getInventory());
		return snap;
	}
	
	/**
	 * Remplis l'inventaire avec les {@link org.bukkit.inventory.ItemStack ItemStack} de la snapshot.
	 * @param holder
	 * @param snapshot
	 */
	public static void fill(InventoryHolder holder, Snapshot<Inventory> snapshot) {
		snapshot.patch(holder.getInventory());
	}
	/**
	 * Remplis l'inventaire avec les {@link org.bukkit.inventory.ItemStack ItemStack} de la snapshot.
	 * @param holder
	 * @param snapshot
	 */
	public static void fill(HumanEntity holder, Snapshot<PlayerInventory> snapshot) {
		snapshot.patch(holder.getInventory());
	}
	
	/**
	 * Forme un rectangle dans l'inventaire aux coordonnées données avec l'ItemStack donné.
	 * L'axe x est l'axe horizontale et l'axe y le vertical.
	 * Le point (0,0) est en haut a gauche dans l'inventaire.
	 * @param inventory
	 * @param itemStack
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public static void paintRectangle(Inventory inventory, ItemStack itemStack, int x1, int y1, int x2, int y2) {
		var minx = x1;
		var maxx = x2;
		var miny = y1;
		var maxy = y2;
		if (minx > maxx) {
			minx = x2;
			maxx = x1;
		}
		if (miny > maxy) {
			miny = y2;
			maxy = y1;
		}
		for (var x = minx; x <= maxx; x++) {
			for (var y = miny; y <= maxy; y++) {
				inventory.setItem((y * 9) + x, itemStack);
			}
		}
	}
	
	/**
	 * Remplis le rectangle spécifié avec les items spécifiés. Il peut y avoir moins d'ItemStack que l'aire du rectangle.
	 * Dans le cas ou il y a plus d'ItemStack que l'aire du rectangle permet d'en montrer, les ItemStack restant ne sont pas affichés.
	 * L'axe x est l'axe horizontale et l'axe y le vertical.
	 * Le point (0,0) est en haut a gauche dans l'inventaire.
	 * @param inventory
	 * @param content
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public static void fillRectangle(Inventory inventory, List<ItemStack> content, int x1, int y1, int x2, int y2) {
		var minx = x1;
		var maxx = x2;
		var miny = y1;
		var maxy = y2;
		if (minx > maxx) {
			minx = x2;
			maxx = x1;
		}
		if (miny > maxy) {
			miny = y2;
			maxy = y1;
		}
		var ite = content.iterator();
		for (var x = minx; x <= maxx; x++) {
			for (var y = miny; y <= maxy; y++) {
				if (ite.hasNext()) {
					inventory.setItem((y * 9) + x, ite.next());
				}else {
					return;
				}
			}
		}
	}
	
}
