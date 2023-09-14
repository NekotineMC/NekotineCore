package fr.nekotine.core.inventory.menu.element;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ActionMenuItem extends ClickableMenuElement{

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
	
	public  @Nullable ClickableMenuElement getClickedMenuItem(ItemStack item) {
		return item != null && item.isSimilar(itemStack) ? this : null;
	}

	@Override
	public void click(Player player) {
		runnable.accept(player);
		askRedraw();
	}

	@Override
	public ItemStack draw() {
		return itemStack;
	}
}
