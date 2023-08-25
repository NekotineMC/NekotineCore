package fr.nekotine.core.inventory.menu.item;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.inventory.menu.ClickableMenuItem;
import fr.nekotine.core.inventory.menu.MenuElement;
import fr.nekotine.core.util.InventoryUtil;

public class ActionMenuItem extends MenuElement implements ClickableMenuItem{

	ItemStack itemStack;
	
	Consumer<Player> runnable;
	
	public ActionMenuItem(ItemStack itemStack, Consumer<Player> action) {
		this.itemStack = itemStack;
		runnable = action;
	}
	
	public ActionMenuItem(ItemStack itemStack, Runnable action) {
		this.itemStack = itemStack;
		runnable = p -> action.run();
	}
	
	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		inventory.setItem(InventoryUtil.chestCoordinateToInventoryIndex(x, y), itemStack);
	}
	
	public  @Nullable ClickableMenuItem getClickedMenuItem(ItemStack item) {
		return item != null && item.isSimilar(itemStack) ? this : null;
	}

	@Override
	public void click(Player player) {
		runnable.accept(player);
	}
}
