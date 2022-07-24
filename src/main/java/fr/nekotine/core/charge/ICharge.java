package fr.nekotine.core.charge;

public interface ICharge {
	
	/**
	 * Lorsque la charge est termin�e
	 * @param user
	 * @param chargeName
	 */
	public void Ended(String user, String chargeName);
	
	/**
	 * Lorsque la charge a �t� annul�e
	 * @param user
	 * @param chargeName
	 * @param left Temps restant de la charge en ms
	 */
	public void Cancelled(String user, String chargeName, long left);
}
