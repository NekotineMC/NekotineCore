package fr.nekotine.core.bowcharge;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public interface IBowCharge {
	
	/**
	 * Lorsque la charge est terminée (ne veut pas dire que la flèche est tirée)
	 * @param user
	 * @param chargeName
	 */
	public void Ended(Player user, String chargeName);
	
	/**
	 * Lorsque la flèche est tirée
	 * @param user
	 * @param chargeName
	 * @param left Temps en ms restant de la charge
	 * @param arrow La flèche tirée
	 */
	public void Released(Player user, String chargeName, long left, Arrow arrow);
	
	/**
	 * Lorsque le joueur annule son tir
	 * @param player
	 * @param chargeName
	 * @param left Temps en ms restant de la charge
	 */
	public void Cancelled(Player user, String chargeName, long left);
	
}
