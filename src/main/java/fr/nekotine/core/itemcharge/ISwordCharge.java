package fr.nekotine.core.itemcharge;

import org.bukkit.entity.Player;

public interface ISwordCharge {
	/**
	 * Lorsque la durée initiale de la charge est terminée (ne veut pas dire que le joueur a arreté l'action)
	 * @param player Le joueur qui charge
	 * @param chargeName Le nom de la charge
	 */
	public void Ended(Player player, String chargeName);
	
	/**
	 * Lorsque le joueur arrête la charge
	 * @param player Le joueur qui a chargé
	 * @param chargeName Le nom de la charge
	 * @param left La durée restante de la charge
	 */
	public void Released(Player player, String chargeName, long left);
}
