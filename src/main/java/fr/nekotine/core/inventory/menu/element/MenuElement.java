package fr.nekotine.core.inventory.menu.element;

import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.inventory.menu.MenuComponent;

public abstract class MenuElement extends MenuComponent{

	public abstract ItemStack draw();
	
}
