package fr.nekotine.core.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.eventguard.PlayerDoubleEventGuard;
import fr.nekotine.core.ioc.Ioc;

public class EventUtil {
	
	/**
	 * 
	 * @param action L'action 1
	 * @param customAction L'action customis�e
	 * @return True si l'action fait partie de la customAction
	 */
	public static boolean isCustomAction(Action action, CustomAction customAction) {
		switch(customAction) {
		case INTERACT_ANY:
			return action==Action.RIGHT_CLICK_AIR || action==Action.RIGHT_CLICK_BLOCK;
		case HIT_ANY:
			return action==Action.LEFT_CLICK_AIR || action==Action.LEFT_CLICK_BLOCK;
		default:
			return false;
		}
	}
	
	/**
	 * Appeler cette fonction si on vérifie HIT_ANY
	 * @param evt L'évent d'interaction
	 * @param customAction L'action customisée
	 * @return True si l'action fait partie de la customAction
	 */
	public static boolean isCustomAction(PlayerInteractEvent evt, CustomAction customAction) {
		if(Ioc.resolve(PlayerDoubleEventGuard.class).alreadyCalled(evt.getPlayer(), evt)) {
			return false;
		}
		return isCustomAction(evt.getAction(), customAction);
	}

	/**
	 * Enregistre les Events pour le Listener
	 * @param plugin
	 * @param listener
	 */
	public static void register(Plugin plugin, Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, plugin);
	}
	
	/**
	 * Enregistre les Events pour le Listener
	 * @param listener
	 */
	public static void register(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, Ioc.resolve(JavaPlugin.class));
	}
	
	/**
	 * D&serengistre les Events pour le Listener
	 * @param listener
	 */
	public static void unregister(Listener listener) {
		HandlerList.unregisterAll(listener);
	}
	
	/**
	 * Appele un event
	 * @param evt
	 */
	public static void call(Event evt) {
		Bukkit.getPluginManager().callEvent(evt);
	}
}
