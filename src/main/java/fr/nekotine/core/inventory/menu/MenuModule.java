package fr.nekotine.core.inventory.menu;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.util.EventUtil;

public class MenuModule extends PluginModule implements Listener{
	
	private List<WeakReference<MenuInventory>> registeredMenus = new LinkedList<>();
	
	public MenuModule() {
		EventUtil.register(this);
	}
	
	public void registerMenu(MenuInventory menu) {
		registeredMenus.add(new WeakReference<>(menu));
	}
	
	/**
	 * 
	 * @param inv
	 * @param itemStack
	 * @return ShouldCancelEvent
	 */
	private boolean onMenuClicked(Inventory inv, ItemStack itemStack) {
		var ite = registeredMenus.iterator();
		var shouldCancel = false;
		while(ite.hasNext()) {
			var val = ite.next().get();
			if (val == null) {
				ite.remove();
			}else {
				if (val.getInventory() == inv) {
					val.OnItemStackClicked(itemStack);
					shouldCancel = true;
				}
			}
		}
		return shouldCancel;
	}
	
	public void unregisterMenu(MenuInventory menu) {
		registeredMenus.removeIf(ref -> menu == ref.get());
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(onMenuClicked(event.getInventory(), event.getCurrentItem()));
	}
	
}
