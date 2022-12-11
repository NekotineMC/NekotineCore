package fr.nekotine.core.inventory.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class MenuLayout {
	
	/**
	 * Arrange un inventaire de type coffre.
	 * @param inventory
	 * @param nbRow
	 */
	public abstract void arrange(Inventory inventory, int nbRow);
	
	/**
	 * Retourne l'{@link fr.nekotine.core.inventory.menu.MenuItem MenuItem} correspondant à l'{@link org;bukkit.inventory.ItemStack ItemStack} donné.
	 * Cette methode permet de retourner un {@link fr.nekotine.core.inventory.menu.MenuItem MenuItem} peut import le système de stockage du layout.
	 * @param item
	 * @return
	 */
	public abstract MenuItem toMenuItem(ItemStack item);
	
}
