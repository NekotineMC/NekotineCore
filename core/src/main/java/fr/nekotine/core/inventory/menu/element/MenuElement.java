package fr.nekotine.core.inventory.menu.element;

import fr.nekotine.core.inventory.menu.MenuComponent;
import org.bukkit.inventory.ItemStack;

public abstract class MenuElement extends MenuComponent {

	public abstract ItemStack draw();
}
