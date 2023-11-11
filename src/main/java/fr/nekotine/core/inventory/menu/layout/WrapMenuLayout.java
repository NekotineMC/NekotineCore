package fr.nekotine.core.inventory.menu.layout;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.inventory.Inventory;

import fr.nekotine.core.inventory.menu.ClickableMenuComponent;
import fr.nekotine.core.inventory.menu.element.MenuElement;
import fr.nekotine.core.util.InventoryUtil;

/**
 * Un layout basique, affichant juste des {@link fr.nekotine.core.inventory.menu.element.ActionMenuItem.inventory.menu.MenuItem MenuItem} comme dans un coffre.
 * 
 * Ce layout offre la possibilité aux {@link fr.nekotine.core.inventory.menu.element.ActionMenuItem.inventory.menu.MenuItem MenuItem} d'être triés.
 * @author XxGoldenbluexX
 *
 */
public class WrapMenuLayout extends MenuLayout{
	
	private Comparator<MenuElement> sorter;
	
	private List<MenuElement> items = new LinkedList<>();
	
	public void setSorter(Comparator<MenuElement> sorter) {
		this.sorter = sorter;
	}
	
	public void addElement(MenuElement element) {
		items.add(element);
		element.setParent(this);
		if (sorter != null) {
			items.sort(sorter);
		}
		if (element instanceof ClickableMenuComponent clickable) {
			registerClicakble(clickable);
		}
	}

	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		var curX = x;
		var curY = y;
		for (var item : items) {
			if (curX > width + x - 1) {
				curX = x;
				if (++curY > height + y - 1) {
					return;
				}
			}
			inventory.setItem(InventoryUtil.chestCoordinateToInventoryIndex(curX, curY), item.draw());
			curX++;
		}
	}

}
