package fr.nekotine.core.inventory.menu.layout;

import java.util.Collection;
import java.util.LinkedList;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.inventory.menu.element.ClickableMenuElement;
import fr.nekotine.core.util.InventoryUtil;

/**
 * @author XxGoldenbluexX
 *
 */
public class ToolbarMenuLayout extends MenuLayout{
	
	private Collection<ClickableMenuElement> tools = new LinkedList<>();
	
	private ItemStack brush;
	
	private MenuLayout child;
	
	public ToolbarMenuLayout(ItemStack borderBrush, MenuLayout child) {
		this.brush = borderBrush;
		this.child = child;
		child.setParent(this);
		registerClicakble(child);
	}
	
	public void addTool(ClickableMenuElement element) {
		tools.add(element);
		element.setParent(this);
		registerClicakble(element);
	}

	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		var curX = x;
		var curY = y;
		
		for (var item : tools) {
			if (curX > width - x) {
				curX = x;
				if (++curY > height - y) {
					return;
				}
			}
			inventory.setItem(InventoryUtil.chestCoordinateToInventoryIndex(curX, curY), item.draw());
			curX++;
		}
		
		curX = x;
		if (++curY > height - y) {
			return;
		}
		InventoryUtil.paintRectangle(inventory, brush, curX, curY, curX + width, curY);
		child.draw(inventory, curX, ++curY, width, height - (curY - y));
	}

}
