package fr.nekotine.core.usable;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "UsableModule")
public class UsableModule extends PluginModule{
	
	private final HashMap<ItemStack, Usable> usables = new HashMap<ItemStack, Usable>();

	@Override
	protected void onDisable() {
		usables.clear();
	}
	
	/**
	 * Enregistre ce Usable pour qu'il recoive les événements.
	 * Si l'ItemStack utilisé par ce Usable à déja un Usable associé, l'ajout au registre échoue.
	 * @param usable L'usable à ajouter au registre.
	 * @return si oui ou non le Usable à été ajouté au registre.
	 */
	public boolean register(Usable usable) {
		if (haveRegisteredUsable(usable.getItem())) return false;
		usables.put(usable.getItem(), usable);
		return true;
	}
	
	/**
	 * Retourne si oui ou non un Usable est associé à cet ItemStack
	 * @param item
	 * @return
	 */
	public boolean haveRegisteredUsable(ItemStack item) {
		return usables.containsKey(item);
	}
	
	/**
	 * Retire ce Usable du registre s'il y est présent.
	 * @param usable
	 * @return Si oui ou non l'Usable a été retiré du registre.
	 */
	public boolean unregister(Usable usable) {
		return usables.remove(usable.getItem()) != null;
	}
	
	// --- Events
	
	@EventHandler
	public void OnInteract(PlayerInteractEvent e) {
		for (Usable u : usables.values()) {
			if (u.getItem().equals(e.getItem())) {
				u.OnInteract(e);
			}
		}
	}
	
	@EventHandler
	public void OnDrop(PlayerDropItemEvent e) {
		for (Usable u : usables.values()) {
			if (u.getItem().isSimilar(e.getItemDrop().getItemStack())) {
				u.OnDrop(e);
			}
		}
	}
	
	@EventHandler
	public void OnInventoryClick(InventoryClickEvent e) {
		for (Usable u : usables.values()) {
			if (u.getItem().equals(e.getCurrentItem())) {
				u.OnInventoryClick(e);
			}
		}
	}
	
	@EventHandler
	public void OnConsume(PlayerItemConsumeEvent e) {
		for (Usable u : usables.values()) {
			if (u.getItem().equals(e.getItem())) {
				u.OnConsume(e);
			}
		}
	}
	
	@EventHandler
	public void OnReadyArrow(PlayerReadyArrowEvent e) {
		for (Usable u : usables.values()) {
			if (u.getItem().equals(e.getArrow())) {
				u.OnReadyArrow(e);
			}
		}
	}
	@EventHandler
	public void OnBowShoot(EntityShootBowEvent e) {
		for (Usable u : usables.values()) {
			if (u.getItem().equals(e.getBow())) {
				u.OnBowShoot(e);
			}
		}
	}
}
