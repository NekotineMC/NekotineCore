package fr.nekotine.core.inventory.menu.layout;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.inventory.menu.MenuItem;
import fr.nekotine.core.inventory.menu.MenuLayout;

/**
 * Un layout basique, affichant juste des {@link fr.nekotinecore.inventory.menu.MenuItem MenuItem} comme dans un coffre.
 * 
 * Ce layout offre la possibilité aux {@link fr.nekotinecore.inventory.menu.MenuItem MenuItem} d'être triés.
 * @author XxGoldenbluexX
 *
 */
public class ChestMenuLayout extends MenuLayout{

	public List<MenuItem> menuItems = new LinkedList<>();
	
	private Comparator<MenuItem> sorter;
	
	public void setSorter(Comparator<MenuItem> sorter) {
		this.sorter = sorter;
	}
	
	@Override
	public void arrange(Inventory inventory, int nbRow) {
		inventory.clear();
		inventory.addItem(menuItems.stream().sorted(sorter).map(i -> i.getItemStack()).toArray(ItemStack[]::new));
	}
	
	public void addMenuItem(@NotNull MenuItem menuItem) {
		menuItems.add(menuItem);
		if (sorter != null) {
			menuItems.sort(sorter);
		}
	}

	@Override
	public @Nullable MenuItem toMenuItem(ItemStack item) {
		var firstMatch = menuItems.stream().filter(i -> i.getItemStack().equals(item)).findFirst();
		if (firstMatch.isPresent()) {
			return firstMatch.get();
		}
		return null;
	}

}
