package fr.nekotine.core.inventory.menu.layout;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.inventory.menu.InvalidLayoutSizeException;
import fr.nekotine.core.inventory.menu.MenuElement;
import fr.nekotine.core.inventory.menu.MenuLayout;
import fr.nekotine.core.util.InventoryUtil;

/**
 * Un layout basique, affichant juste des {@link fr.nekotine.core.inventory.menu.item.ActionMenuItem.inventory.menu.MenuItem MenuItem} comme dans un coffre.
 * 
 * Ce layout offre la possibilité aux {@link fr.nekotine.core.inventory.menu.item.ActionMenuItem.inventory.menu.MenuItem MenuItem} d'être triés.
 * @author XxGoldenbluexX
 *
 */
public class BorderMenuLayout extends MenuLayout{

	private MenuElement child;
	
	private ItemStack brush;
	
	public BorderMenuLayout(ItemStack borderBrush, MenuElement child) {
		brush = borderBrush;
		this.child = child;
	}
	
	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		if (width < 3 || height < 3) {
			throw new InvalidLayoutSizeException(String.format("La taille d'une border doit être de 3x3 minimum (%sx%s donné)", width, height));
		}
		InventoryUtil.paintRectangle(inventory, brush, x, y, x, y + height);
		InventoryUtil.paintRectangle(inventory, brush, x+width, y, x+width, y + height);
		InventoryUtil.paintRectangle(inventory, brush, x+1, y, x+width-1, y);
		InventoryUtil.paintRectangle(inventory, brush, x+1, y+height, x+width-1, y+height);
		child.draw(inventory, x+1, y+1, width-2, height-2);
	}

}
