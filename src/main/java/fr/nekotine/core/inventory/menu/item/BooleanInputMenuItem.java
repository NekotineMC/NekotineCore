package fr.nekotine.core.inventory.menu.item;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.inventory.menu.ClickableMenuItem;
import fr.nekotine.core.inventory.menu.MenuElement;
import fr.nekotine.core.util.InventoryUtil;

/**
 * Un layout basique, affichant juste des {@link fr.nekotine.core.inventory.menu.item.nekotinecore.inventory.menu.MenuItem MenuItem} comme dans un coffre.
 * 
 * Ce layout offre la possibilité aux {@link fr.nekotine.core.inventory.menu.item.nekotinecore.inventory.menu.MenuItem MenuItem} d'être triés.
 * @author XxGoldenbluexX
 *
 */
public class BooleanInputMenuItem extends MenuElement implements ClickableMenuItem{
	
	private ItemStack trueItem;
	
	private ItemStack falseItem;
	
	private boolean value;
	
	public BooleanInputMenuItem(ItemStack trueItem, ItemStack falseItem) {
		this.trueItem = trueItem;
		this.falseItem = falseItem;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public void setValue(boolean value) {
		this.value = value;
		askRedraw();
	}
	
	public void toggleValue() {
		value = !value;
		askRedraw();
	}

	@Override
	public void click() {
		toggleValue();
	}

	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		inventory.setItem(InventoryUtil.chestCoordinateToInventoryIndex(x,y), value ? trueItem : falseItem);
	}

	@Override
	public @Nullable ClickableMenuItem getClickedMenuItem(ItemStack item) {
		if (item == trueItem || item == falseItem) {
			return this;
		}
		return null;
	}

}