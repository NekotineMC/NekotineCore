package fr.nekotine.core.inventory.menu.item;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.inventory.menu.ClickableMenuItem;
import fr.nekotine.core.inventory.menu.MenuElement;
import fr.nekotine.core.util.InventoryUtil;

public class ActionMenuItem extends MenuElement implements ClickableMenuItem{

	ItemStack itemStack;
	
	Runnable runnable;
	
	public ActionMenuItem(ItemStack itemStack, Runnable action) {
		this.itemStack = itemStack;
		runnable = action;
	}
	
	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		inventory.setItem(InventoryUtil.chestCoordinateToInventoryIndex(x, y), itemStack);
	}
	
	public  @Nullable ClickableMenuItem getClickedMenuItem(ItemStack item) {
		return item != null && item.isSimilar(itemStack) ? this : null;
	}

	@Override
	public void click() {
		runnable.run();
	}
}
