package fr.nekotine.core.charge;

public class Charger {
	
	private long started;
	private boolean cancelled;
	
	private String user;
	private String chargeName;
	private long duration;
	private ICharge iCharge;
	
	/**
	 * 
	 * @param user Utilisateur de la charge
	 * @param chargeName Nom de la charge /!\ Doit être différente pour chaque user
	 * @param duration Durée en ms
	 * @param iCharge Interface
	 */
	public Charger(String user, String chargeName, long duration, ICharge iCharge) {
		this.user = user;
		this.chargeName = chargeName;
		this.duration = duration;
		this.iCharge = iCharge;
		
		this.started = System.currentTimeMillis();
		this.cancelled = false;
	}
	
	public boolean Charged() {
		if(cancelled) {
			iCharge.Cancelled(user, chargeName, GetTimeLeft());
			return true;
		}
		if(Expired()) {
			iCharge.Ended(user, chargeName);
			return true;
		}
		return false;
	}
	
	/**
	 * Annulle la charge
	 * @param cancelled
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	private long GetTime() {
		return System.currentTimeMillis();
	}
	private long GetTimeLeft() {
		return GetTime() - started;
	}
	private boolean Expired() {
		return GetTimeLeft() >= duration;
	}
}
