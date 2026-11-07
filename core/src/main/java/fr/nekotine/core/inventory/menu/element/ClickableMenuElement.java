package fr.nekotine.core.inventory.menu.element;

import fr.nekotine.core.inventory.menu.ClickableMenuComponent;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class ClickableMenuElement extends MenuElement implements ClickableMenuComponent {

	@Override
	public void onClick(InventoryClickEvent event) {
		var item = event.getCurrentItem();
		if (item != null && item.isSimilar(draw())) {
			click(event);
		}
	}

	/**
	 * Actions when clicked ItemStack is matching
	 *
	 * @param event
	 */
	public abstract void click(InventoryClickEvent event);
}
