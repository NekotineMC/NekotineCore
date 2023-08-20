package fr.nekotine.core.inventory.menu.layout;

import java.util.Comparator;

import org.bukkit.inventory.Inventory;

import fr.nekotine.core.inventory.menu.MenuElement;
import fr.nekotine.core.inventory.menu.MenuLayout;

/**
 * Un layout basique, affichant juste des {@link fr.nekotine.core.inventory.menu.item.ActionMenuItem.inventory.menu.MenuItem MenuItem} comme dans un coffre.
 * 
 * Ce layout offre la possibilité aux {@link fr.nekotine.core.inventory.menu.item.ActionMenuItem.inventory.menu.MenuItem MenuItem} d'être triés.
 * @author XxGoldenbluexX
 *
 */
public class WrapMenuLayout extends MenuLayout{
	
	private Comparator<MenuElement> sorter;
	
	public void setSorter(Comparator<MenuElement> sorter) {
		this.sorter = sorter;
	}
	
	@Override
	public void addMenuElement(MenuElement element) {
		super.addMenuElement(element);
		if (this.sorter != null) {
			menuElements.sort(sorter);
		}
	}

	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		var curX = x;
		var curY = y;
		for (var item : menuElements) {
			item.draw(inventory, curX, curY, 1, 1);
			curX++;
			if (++curX > width - x) {
				curX = x;
				if (++curY > height - y) {
					return;
				}
			}
		}
	}

}
