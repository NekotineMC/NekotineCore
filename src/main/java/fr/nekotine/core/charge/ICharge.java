package fr.nekotine.core.charge;

public interface ICharge {
	
	/**
	 * Appele lorsque la charge est arrivee a expiration
	 * @param user L'utilisateur de la charge
	 * @param chargeName Nom de la charge
	 */
	public void Ended(String user, String chargeName);
	
	/**
	 * Appele lorsque la charge est annulee
	 * @param user L'utilisateur de la charge
	 * @param chargeName Nom de la charge
	 * @param left La duree restante de la charge
	 */
	public void Cancelled(String user, String chargeName, long left);
}
