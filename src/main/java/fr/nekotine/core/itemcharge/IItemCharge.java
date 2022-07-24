package fr.nekotine.core.itemcharge;

import org.bukkit.entity.Player;

public interface IItemCharge {
	/**
	 * Lorsque la charge est termin�e (ne veut pas dire que le joueur a arr�t� l'action)
	 * @param player
	 * @param chargeName
	 */
	public void Ended(Player player, String chargeName);
	
	/**
	 * Lorsque le joueur a arr�t� l'action
	 * @param player
	 * @param chargeName
	 * @param left Temps en ms restant de la charge
	 */
	public void Released(Player player, String chargeName, long left);
}
