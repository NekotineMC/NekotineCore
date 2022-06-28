package fr.nekotine.core.bowcharge;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public interface IBowCharge {
	
	/**
	 * Lorsque la dur�e initiale de la charge est termin�e (ne veut pas dire que la fleche est tir�e)
	 * @param player Le joueur qui tire
	 * @param chargeName Le nom de la charge
	 */
	public void Ended(Player player, String chargeName);
	
	/**
	 * Lorsque le joueur tire sa fleche charg�e
	 * @param player Le joueur qui a tir�
	 * @param chargeName Le nom de la charge
	 * @param left La dur�e restante de la charge
	 * @param arrow La fleche tir�e
	 */
	public void Released(Player player, String chargeName, long left, Arrow arrow);
	
	/**
	 * Lorsque le joueur annule son tire
	 * @param player Le joueur qui a annul� son tir
	 * @param chargeName Le nom de la charge
	 * @param left La dur�e restante de la charge
	 */
	public void Cancelled(Player player, String chargeName, long left);
	
}
