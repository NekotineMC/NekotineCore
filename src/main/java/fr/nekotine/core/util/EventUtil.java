package fr.nekotine.core.util;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import fr.nekotine.core.NekotineCore;

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
	
	public static boolean isCustomAction(PlayerInteractEvent evt, CustomAction customAction) {
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
		Bukkit.getPluginManager().registerEvents(listener, NekotineCore.getAttachedPlugin());
	}
	
	/**
	 * D&serengistre les Events pour le Listener
	 * @param listener
	 */
	public static void unregister(Listener listener) {
		HandlerList.unregisterAll(listener);
	}
}
