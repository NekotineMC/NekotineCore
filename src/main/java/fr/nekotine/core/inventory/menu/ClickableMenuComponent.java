package fr.nekotine.core.inventory.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ClickableMenuComponent {

	public void onClick(ItemStack clicked, Player player);
	
}
