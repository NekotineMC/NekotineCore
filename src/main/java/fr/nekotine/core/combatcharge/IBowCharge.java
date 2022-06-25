package fr.nekotine.core.combatcharge;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public interface IBowCharge {
	
	/**
	 * Lorsque la durée initiale de la charge est terminée (ne veut pas dire que la fleche est tirée)
	 * @param player Le joueur qui tire
	 * @param chargeName Le nom de la charge
	 */
	public void Ended(Player player, String chargeName);
	
	/**
	 * Lorsque le joueur tire sa fleche chargée
	 * @param player Le joueur qui a tiré
	 * @param chargeName Le nom de la charge
	 * @param left La durée restante de la charge
	 * @param arrow La fleche tirée
	 */
	public void Released(Player player, String chargeName, long left, Arrow arrow);
	
	/**
	 * Lorsque le joueur annule son tire
	 * @param player Le joueur qui a annulé son tir
	 * @param chargeName Le nom de la charge
	 * @param left La durée restante de la charge
	 */
	public void Cancelled(Player player, String chargeName, long left);
	
}
