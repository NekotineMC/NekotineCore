package fr.nekotine.core.inventory.menu.layout;

import java.util.Collection;
import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.inventory.menu.ClickableMenuComponent;
import fr.nekotine.core.inventory.menu.MenuComponent;

public abstract class MenuLayout extends MenuComponent implements ClickableMenuComponent{
	
	public abstract void draw(Inventory inventory, int x, int y, int width, int height);
	
	private Collection<ClickableMenuComponent> clickables = new LinkedList<>();
	
	protected void registerClicakble(ClickableMenuComponent element) {
		clickables.add(element);
	}
	
	protected void unregisterClickable(ClickableMenuComponent element) {
		clickables.remove(element);
	}
	
	public void onClick(ItemStack itemStack, Player player) {
		for (var clickable : clickables) {
			clickable.onClick(itemStack, player);
		}
	}
	
}
