package fr.nekotine.core.usable;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "UsableModule")
public class UsableModule extends PluginModule{
	private final HashMap<ItemStack, Usable> usables = new HashMap<ItemStack, Usable>();
	
	//
	
	/**
	 * 
	 * @param item L'item à regarder
	 * @param holder L'inventaire dans lequel il se trouve
	 * @return L'usable crée
	 */
	public Usable AddUsable(ItemStack item, Inventory holder) {
		if(usables.containsKey(item)) return null;
		
		Usable usable = new Usable(this, item, holder);
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
	@EventHandler
	public void OnReadyArrow(PlayerReadyArrowEvent e) {
		usables.values().forEach( (usable) -> usable.OnReadyArrow(e));
	}
	@EventHandler
	public void OnBowShoot(EntityShootBowEvent e) {
		usables.values().forEach( (usable) -> usable.OnBowShoot(e));
	}
	
	//
	
	private boolean Exist(ItemStack item) {
		return usables.containsKey(item);
	}
	
	//
	
	protected void Remove(ItemStack item) {
		if(!Exist(item)) return;
		
		usables.remove(item);
	}
}
