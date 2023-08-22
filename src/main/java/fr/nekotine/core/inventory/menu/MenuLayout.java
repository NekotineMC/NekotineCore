package fr.nekotine.core.inventory.menu;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class MenuLayout extends MenuElement{
	
	protected List<MenuElement> menuElements = new LinkedList<>();
	
	public void addMenuElement(MenuElement element) {
		menuElements.add(element);
		element.setParent(this);
	}
	
	/**
	 * Retourne l'{@link fr.nekotine.core.inventory.menu.item.ActionMenuItem MenuItem} correspondant à l'{@link org;bukkit.inventory.ItemStack ItemStack} donné.
	 * Cette methode permet de retourner un {@link fr.nekotine.core.inventory.menu.item.ActionMenuItem MenuItem} peut import le système de stockage du layout.
	 * @param item
	 * @return
	 */
	public  @Nullable ClickableMenuItem getClickedMenuItem(ItemStack item) {
		ClickableMenuItem clicked = null;
		for(var element : menuElements) {
			clicked = element.getClickedMenuItem(item);
			if (clicked != null) {
				return clicked;
			}
		}
		return clicked;
	}
	
}
