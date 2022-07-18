package fr.nekotine.core.usable;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Usable {
	private final ItemStack item;
	private final Inventory holder;
	private final IUsable iUsable;
	
	//
	
	public Usable(ItemStack item, Inventory holder, IUsable iUsable) {
		this.item = item;
		this.holder = holder;
		this.iUsable = iUsable;
	}
	
	//
	
	public void OnInteract(PlayerInteractEvent e) {
		if(item.equals(e.getItem())) iUsable.Interact(e);
	}
	public void OnDrop(PlayerDropItemEvent e) {
		if(item.equals(e.getItemDrop().getItemStack())) iUsable.Drop(e);
	}
	public void OnInventoryClick(InventoryClickEvent e) {
		if(item.equals(e.getCurrentItem())) iUsable.InventoryClick(e);
	}
	public void OnConsume(PlayerItemConsumeEvent e) {
		if(item.equals(e.getItem())) iUsable.Consume(e);
	}
	
	//
	
	public void AddAmount(int quantity) {
		item.add(quantity);
	}
	public void SetAmount(int quantity) {
		item.setAmount(quantity);
	}
	public void SetEnchanted(boolean enchanted) {

	}
	public void AddEnchant() {
		
	}
	public void Remove() {
		holder.remove(item);
	}
	
	//
	
	public int GetCount() {
		return item.getAmount();
	}

}
