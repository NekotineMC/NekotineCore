package fr.nekotine.core.inventory.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.inventory.menu.layout.MenuLayout;
import net.kyori.adventure.text.Component;

/**
 * Menu créé avec un coffre.
 * 
 * @author XxGoldenbluexX
 *
 */
public class MenuInventory extends MenuLayout{

	private Inventory inventory;

	private MenuLayout layout;

	private int nbRow;

	public MenuInventory(@NotNull MenuLayout layout, int nbRow) {
		this.layout = layout;
		this.layout.setParent(this);
		this.nbRow = nbRow;
		inventory = Bukkit.createInventory(null, nbRow * 9);
		NekotineCore.MODULES.get(MenuModule.class).registerMenu(this);
	}

	public MenuInventory(@NotNull MenuLayout layout, int nbRow, @NotNull Component title) {
		this.layout = layout;
		this.layout.setParent(this);
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
		redraw();
		player.openInventory(inventory);
	}
	
	@Override
	public void askRedraw() {
		redraw();
	}
	
	public void redraw() {
		inventory.clear();
		draw(inventory, 0, 0, 9, nbRow);
	}

	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		layout.draw(inventory, x, y, width, height);
	}

}
