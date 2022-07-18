package fr.nekotine.core.usable;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public interface IUsable {
	public void Interact(PlayerInteractEvent e);
	public void InventoryClick(InventoryClickEvent e);
	public void Consume(PlayerItemConsumeEvent e);
	public void Drop(PlayerDropItemEvent e);
}
