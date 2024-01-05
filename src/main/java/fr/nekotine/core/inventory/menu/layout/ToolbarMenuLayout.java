package fr.nekotine.core.inventory.menu.layout;

import java.util.Collection;
import java.util.LinkedList;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.inventory.menu.ClickableMenuComponent;
import fr.nekotine.core.inventory.menu.element.MenuElement;
import fr.nekotine.core.util.InventoryUtil;

/**
 * @author XxGoldenbluexX
 *
 */
public class ToolbarMenuLayout extends MenuLayout{
	
	private Collection<MenuElement> tools = new LinkedList<>();
	
	private ItemStack brush;
	
	private MenuLayout child;
	
	private Direction direction;
	
	public ToolbarMenuLayout(ItemStack borderBrush, MenuLayout child, Direction direction) {
		this.brush = borderBrush;
		this.child = child;
		this.direction = direction;
		child.setParent(this);
		registerClicakble(child);
	}
	
	public ToolbarMenuLayout(ItemStack borderBrush, MenuLayout child) {
		this(borderBrush, child, Direction.UP);
	}
	
	public void addTool(MenuElement element) {
		tools.add(element);
		element.setParent(this);
		if (element instanceof ClickableMenuComponent c) {
			registerClicakble(c);
		}
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
			inventory.setItem(InventoryUtil.chestCoordinateToInventoryIndex(curX, directionalY(curY,y,height)), item.draw());
			curX++;
		}
		
		curX = x;
		if (++curY - y > height) {
			return;
		}
		InventoryUtil.paintRectangle(inventory, brush, curX, directionalY(curY,y,height), curX + width, directionalY(curY,y,height));
		child.draw(inventory, curX, directionalY(++curY,y,height), width, height - (curY - y));
	}
	
	/**
	 * Retourne la valeur de Y selon le haut ou le bas du composant, selon la direction selectionnée
	 * @param y
	 * @param baseY
	 * @param height
	 * @return
	 */
	private int directionalY(int y, int baseY, int height) {
		switch(direction) {
		case UP:
			return y;
		case DOWN:
			var locY = y - baseY;
			return baseY + height - locY -1;
		}
		return y;
	}
	
	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public enum Direction{
		UP,
		DOWN;
	}

}
