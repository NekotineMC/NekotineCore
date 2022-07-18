package fr.nekotine.core.usable;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "UsableManager")
public class UsableManager extends PluginModule{
	private final HashMap<ItemStack, Usable> usables = new HashMap<ItemStack, Usable>();
	
	//
	
	public Usable AddUsable(ItemStack item, Inventory holder, IUsable iUsable) {
		if(usables.containsKey(item)) return null;
		
		Usable usable = new Usable(item, holder, iUsable);
		usables.put(item, usable);
		return usable;
	}
	
	//
	
	@EventHandler
	public void OnInteract(PlayerInteractEvent e) {
		usables.values().forEach( (usable) -> usable.OnInteract(e));
	}
	@EventHandler
	public void OnDrop(PlayerDropItemEvent e) {
		usables.values().forEach( (usable) -> usable.OnDrop(e));
	}
	@EventHandler
	public void OnInventoryClick(InventoryClickEvent e) {
		usables.values().forEach( (usable) -> usable.OnInventoryClick(e));
	}
	@EventHandler
	public void OnConsume(PlayerItemConsumeEvent e) {
		usables.values().forEach( (usable) -> usable.OnConsume(e));
	}
	
	//
	
	public Usable Get(ItemStack item) {
		return usables.get(item);
	}
	public boolean Exist(ItemStack item) {
		return usables.containsKey(item);
	}
}
