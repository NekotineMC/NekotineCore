package fr.nekotine.core.inventory.menu.item;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
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
	
	private BiConsumer<@Nullable Player, Boolean> valueChangedCallback;
	
	private ItemStack trueItem;
	
	private ItemStack falseItem;
	
	private boolean value;
	
	public BooleanInputMenuItem(ItemStack trueItem, ItemStack falseItem) {
		this.trueItem = trueItem;
		this.falseItem = falseItem;
	}
	
	public BooleanInputMenuItem(ItemStack trueItem, ItemStack falseItem, BiConsumer<@Nullable Player, Boolean> valueChangedCallback) {
		this.trueItem = trueItem;
		this.falseItem = falseItem;
		this.valueChangedCallback = valueChangedCallback;
	}
	
	public BooleanInputMenuItem(ItemStack trueItem, ItemStack falseItem, Consumer<Boolean> valueChangedCallback) {
		this.trueItem = trueItem;
		this.falseItem = falseItem;
		this.valueChangedCallback = (player, value) -> valueChangedCallback.accept(value);
	}
	
	public BooleanInputMenuItem(ItemStack trueItem, ItemStack falseItem, boolean defaultValue) {
		this.trueItem = trueItem;
		this.falseItem = falseItem;
		value = defaultValue;
	}
	
	public BooleanInputMenuItem(ItemStack trueItem, ItemStack falseItem, boolean defaultValue, BiConsumer<@Nullable Player, Boolean> valueChangedCallback) {
		this.trueItem = trueItem;
		this.falseItem = falseItem;
		this.valueChangedCallback = valueChangedCallback;
		value = defaultValue;
	}
	
	public BooleanInputMenuItem(ItemStack trueItem, ItemStack falseItem, boolean defaultValue, Consumer<Boolean> valueChangedCallback) {
		this.trueItem = trueItem;
		this.falseItem = falseItem;
		this.valueChangedCallback = (player, value) -> valueChangedCallback.accept(value);
		value = defaultValue;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public void setValue(boolean value) {
		if (this.value != value) {
			this.value = value;
			if (valueChangedCallback != null) {
				valueChangedCallback.accept(null, this.value);
			}
			askRedraw();
		}
	}
	
	public void toggleValue() {
		value = !value;
		if (valueChangedCallback != null) {
			valueChangedCallback.accept(null, this.value);
		}
		askRedraw();
	}
	
	public void setValue(boolean value, Player source) {
		if (this.value != value) {
			this.value = value;
			if (valueChangedCallback != null) {
				valueChangedCallback.accept(source, this.value);
			}
			askRedraw();
		}
	}
	
	public void toggleValue(Player player) {
		value = !value;
		if (valueChangedCallback != null) {
			valueChangedCallback.accept(player, this.value);
		}
		askRedraw();
	}

	@Override
	public void click(Player player) {
		toggleValue(player);
	}

	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		inventory.setItem(InventoryUtil.chestCoordinateToInventoryIndex(x,y), value ? trueItem : falseItem);
	}

	@Override
	public @Nullable ClickableMenuItem getClickedMenuItem(ItemStack item) {
		if (item != null && (item.isSimilar(trueItem) || item.isSimilar(falseItem))) {
			return this;
		}
		return null;
	}

}
