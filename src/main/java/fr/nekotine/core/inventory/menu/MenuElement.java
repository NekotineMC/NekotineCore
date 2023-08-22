package fr.nekotine.core.inventory.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class MenuElement {

	private MenuLayout parent;
	
	public abstract void draw(Inventory inventory, int x, int y, int width, int height);
	
	public abstract @Nullable ClickableMenuItem getClickedMenuItem(ItemStack item);
	
	public void setParent(MenuLayout parent) {
		this.parent = parent;
	}
	
	public MenuLayout getParent() {
		return parent;
	}
	
	public void askRedraw() {
		parent.askRedraw();
	}
	
}
