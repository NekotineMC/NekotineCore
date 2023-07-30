package fr.nekotine.core.inventory.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class MenuElement {

	private MenuInventory menuInventory;
	
	public abstract void draw(Inventory inventory, int x, int y, int width, int height);
	
	public abstract @Nullable ClickableMenuItem getClickedMenuItem(ItemStack item);
	
	public void setMenuInventory(MenuInventory menu) {
		menuInventory = menu;
	}
	
	public MenuInventory getMenuInventory() {
		return menuInventory;
	}
	
	public void askRedraw() {
		menuInventory.redraw();
	}
	
}
