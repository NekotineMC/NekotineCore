package fr.nekotine.core.inventory.menu.item;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.inventory.menu.ClickableMenuItem;
import fr.nekotine.core.inventory.menu.MenuElement;
import fr.nekotine.core.util.InventoryUtil;

/**
 * MenuElement pour interragire avec une valeur boolean
 * 
 * @author XxGoldenbluexX
 *
 */
public class BooleanInputMenuItem extends MenuElement implements ClickableMenuItem{
	
	private final Supplier<Boolean> valueSupplier;
	
	private final BiConsumer<@Nullable Player, Boolean> valueChangedCallback;
	
	private ItemStack trueItem;
	
	private ItemStack falseItem;
	
	public BooleanInputMenuItem(ItemStack trueItem, ItemStack falseItem, Supplier<Boolean> valueSupplier, BiConsumer<@Nullable Player, Boolean> valueChangedCallback) {
		this.trueItem = trueItem;
		this.falseItem = falseItem;
		this.valueSupplier= valueSupplier; 
		this.valueChangedCallback = valueChangedCallback;
	}
	
	public BooleanInputMenuItem(ItemStack trueItem, ItemStack falseItem, Supplier<Boolean> valueSupplier, Consumer<Boolean> valueChangedCallback) {
		this.trueItem = trueItem;
		this.falseItem = falseItem;
		this.valueSupplier = valueSupplier;
		this.valueChangedCallback = (player, value) -> valueChangedCallback.accept(value);
	}
	
	public boolean getValue() {
		return valueSupplier.get();
	}
	
	public void setValue(boolean value) {
		if (valueSupplier.get() != value) {
			if (valueChangedCallback != null) {
				valueChangedCallback.accept(null, value);
				askRedraw();
			}
		}
	}
	
	public void toggleValue() {
		if (valueChangedCallback != null) {
			valueChangedCallback.accept(null, !getValue());
			askRedraw();
		}
	}
	
	public void setValue(boolean value, Player source) {
		if (valueSupplier.get() != value) {
			if (valueChangedCallback != null) {
				valueChangedCallback.accept(source, value);
				askRedraw();
			}
		}
	}
	
	public void toggleValue(Player player) {
		if (valueChangedCallback != null) {
			valueChangedCallback.accept(player, !getValue());
			askRedraw();
		}
	}

	@Override
	public void click(Player player) {
		toggleValue(player);
	}

	@Override
	public void draw(Inventory inventory, int x, int y, int width, int height) {
		inventory.setItem(InventoryUtil.chestCoordinateToInventoryIndex(x,y), getValue() ? trueItem : falseItem);
	}

	@Override
	public @Nullable ClickableMenuItem getClickedMenuItem(ItemStack item) {
		if (item != null && (item.isSimilar(trueItem) || item.isSimilar(falseItem))) {
			return this;
		}
		return null;
	}

}
