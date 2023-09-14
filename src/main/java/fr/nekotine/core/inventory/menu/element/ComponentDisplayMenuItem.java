package fr.nekotine.core.inventory.menu.element;

import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

/**
 * MenuElement pour afficher un Component dynamique
 * 
 * @author XxGoldenbluexX
 *
 */
public class ComponentDisplayMenuItem extends MenuElement{
	
	private final Supplier<Component> valueSupplier;
	
	private ItemStack item;
	
	public ComponentDisplayMenuItem(ItemStack item, Supplier<Component> valueSupplier) {
		this.item = item;
		this.valueSupplier= valueSupplier;
	}

	@Override
	public ItemStack draw() {
		var meta = item.getItemMeta();
		meta.displayName(valueSupplier.get());
		item.setItemMeta(meta);
		return item;
	}

}
