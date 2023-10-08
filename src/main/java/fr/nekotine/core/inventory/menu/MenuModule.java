package fr.nekotine.core.inventory.menu;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.util.EventUtil;

public class MenuModule extends PluginModule implements Listener{
	
	private List<WeakReference<MenuInventory>> registeredMenus = new LinkedList<>();
	
	public MenuModule() {
		EventUtil.register(this);
	}
	
	@Override
	protected void unload() {
		EventUtil.unregister(this);
		super.unload();
	}
	
	public void registerMenu(MenuInventory menu) {
		registeredMenus.add(new WeakReference<>(menu));
	}
	
	public void unregisterMenu(MenuInventory menu) {
		registeredMenus.removeIf(ref -> menu == ref.get());
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		var tempList = new LinkedList<>(registeredMenus);
		for (var weak : tempList) {
			var menu = weak.get();
			if (menu == null) {
				registeredMenus.remove(weak);
			}else {
				if (menu.getInventory() == event.getInventory()) {
					menu.onClick(event);
					event.setCancelled(true);
				}
			}
		}
	}
	
}
