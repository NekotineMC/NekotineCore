package fr.nekotine.core.usable;

import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;

import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.util.EventUtil;

public class UsableModule implements IPluginModule, Listener{
	
	private final Set<Usable> usables = new LinkedHashSet<>();

	public UsableModule() {
		EventUtil.register(this);
	}
	
	@Override
	public void unload() {
		EventUtil.unregister(this);
		usables.clear();
	}
	
	/**
	 * Enregistre ce Usable pour qu'il recoive les événements.
	 * @param usable L'usable à ajouter au registre.
	 * @return si oui ou non le Usable à été ajouté au registre.
	 */
	public boolean register(Usable usable) {
		return usables.add(usable);
	}
	
	/**
	 * Retourne si oui ou non l'usable est dans le registre.
	 * @param item
	 * @return
	 */
	public boolean isRegistered(Usable usable) {
		return usables.contains(usable);
	}
	
	/**
	 * Retire ce Usable du registre s'il y est présent.
	 * @param usable
	 * @return Si oui ou non l'Usable a été retiré du registre.
	 */
	public boolean unregister(Usable usable) {
		return usables.remove(usable);
	}
	
	// --- Events
	
	@EventHandler
	public void OnInteract(PlayerInteractEvent e) {
		for (Usable u : usables) {
			if (u.getItemStack().equals(e.getItem())) {
				u.OnInteract(e);
			}
		}
	}
	
	@EventHandler
	public void OnDrop(PlayerDropItemEvent e) {
		for (Usable u : usables) {
			if (u.getItemStack().isSimilar(e.getItemDrop().getItemStack())) {
				u.OnDrop(e);
			}
		}
	}
	
	@EventHandler
	public void OnInventoryClick(InventoryClickEvent e) {
		for (Usable u : usables) {
			if (u.getItemStack().equals(e.getCurrentItem())) {
				u.OnInventoryClick(e);
			}
		}
	}
	
	@EventHandler
	public void OnConsume(PlayerItemConsumeEvent e) {
		for (Usable u : usables) {
			if (u.getItemStack().equals(e.getItem())) {
				u.OnConsume(e);
			}
		}
	}
	
	@EventHandler
	public void OnReadyArrow(PlayerReadyArrowEvent e) {
		for (Usable u : usables) {
			if (u.getItemStack().equals(e.getArrow())) {
				u.OnReadyArrow(e);
			}
		}
	}
	@EventHandler
	public void OnBowShoot(EntityShootBowEvent e) {
		for (Usable u : usables) {
			if (u.getItemStack().equals(e.getBow())) {
				u.OnBowShoot(e);
			}
		}
	}
}
