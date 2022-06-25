package fr.nekotine.core.util;

import org.bukkit.event.block.Action;

public class UtilEvent {
	
	/**
	 * 
	 * @param action L'action 1
	 * @param customAction L'action customisée
	 * @return True si l'action fait partie de la customAction
	 */
	public static boolean IsAction(Action action, CustomAction customAction) {
		switch(customAction) {
		case RIGHT_CLICK:
			if(action==Action.RIGHT_CLICK_AIR || action==Action.RIGHT_CLICK_BLOCK) {
				return true;
			}
			return false;
		default:
			return false;
		}
	}
}
