package fr.nekotine.core.inventory.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.NekotineCore;
import net.kyori.adventure.text.Component;

/**
 * Menu créé avec un coffre. Un menu est supposé servir un seul joueur.
 * 
 * @author XxGoldenbluexX
 *
 */
public class MenuInventory {

	private Inventory inventory;

	private MenuLayout layout;

	private int nbRow;

	public MenuInventory(@NotNull MenuLayout layout, int nbRow) {
		this.layout = layout;
		this.nbRow = nbRow;
		inventory = Bukkit.createInventory(null, nbRow * 9);
		NekotineCore.MODULES.get(MenuModule.class).registerMenu(this);
	}

	public MenuInventory(@NotNull MenuLayout layout, int nbRow, @NotNull Component title) {
		this.layout = layout;
		this.nbRow = nbRow;
		inventory = Bukkit.createInventory(null, nbRow * 9, title);
		NekotineCore.MODULES.get(MenuModule.class).registerMenu(this);
	}

	/**
	 * Ouvre le menu pour le joueur. Pour rappel, un menu est supposé servire pour
	 * un seul joueur. Si un menu est ouvert pour plusieurs joueurs, il sera
	 * synchronisé entre les deux.
	 * 
	 * @param player
	 */
	public void displayTo(@NotNull Player player) {
		layout.arrange(inventory, nbRow);
		player.openInventory(inventory);
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void OnItemStackClicked(@NotNull ItemStack itemStack) {
		var item = layout.toMenuItem(itemStack);
		if (item != null) {
			var action = item.getAction();
			if (action != null) {
				action.run();
			}
		}
	}

}
