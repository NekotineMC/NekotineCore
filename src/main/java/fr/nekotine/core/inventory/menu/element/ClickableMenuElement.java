package fr.nekotine.core.inventory.menu.element;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.inventory.menu.ClickableMenuComponent;

public abstract class ClickableMenuElement extends MenuElement implements ClickableMenuComponent{

	@Override
	public void onClick(ItemStack clicked, Player player) {
		if (clicked.isSimilar(draw())) {
			click(player);
		}
	}
	
	public abstract void click(Player player);
	
}
