package fr.nekotine.core.itemcharge;

import org.bukkit.entity.Player;

public interface IItemCharge {
	/**
	 * Lorsque la charge est terminée (ne veut pas dire que le joueur a arrêté l'action)
	 * @param player
	 * @param chargeName
	 */
	public void Ended(Player player, String chargeName);
	
	/**
	 * Lorsque le joueur a arrêté l'action
	 * @param player
	 * @param chargeName
	 * @param left Temps en ms restant de la charge
	 */
	public void Released(Player player, String chargeName, long left);
}
