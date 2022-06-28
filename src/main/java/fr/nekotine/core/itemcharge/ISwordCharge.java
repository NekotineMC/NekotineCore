package fr.nekotine.core.itemcharge;

import org.bukkit.entity.Player;

public interface ISwordCharge {
	/**
	 * Lorsque la dur�e initiale de la charge est termin�e (ne veut pas dire que le joueur a arret� l'action)
	 * @param player Le joueur qui charge
	 * @param chargeName Le nom de la charge
	 */
	public void Ended(Player player, String chargeName);
	
	/**
	 * Lorsque le joueur arr�te la charge
	 * @param player Le joueur qui a charg�
	 * @param chargeName Le nom de la charge
	 * @param left La dur�e restante de la charge
	 */
	public void Released(Player player, String chargeName, long left);
}
