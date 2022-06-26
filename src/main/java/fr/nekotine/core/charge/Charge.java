package fr.nekotine.core.charge;

public class Charge {
	
	private long started;
	private boolean cancelled;
	
	//
	
	private String user;
	private String chargeName;
	private long duration;
	private ICharge iCharge;
	
	//
	
	/**
	 * 
	 * @param user Utilisateur de la charge
	 * @param chargeName Nom de la charge /!\ Doit être différente pour chaque user
	 * @param duration Durée en ms
	 * @param iCharge Interface
	 */
	public Charge(String user, String chargeName, long duration, ICharge iCharge) {
		this.user = user;
		this.chargeName = chargeName;
		this.duration = duration;
		this.iCharge = iCharge;
		
		this.started = System.currentTimeMillis();
		this.cancelled = false;
	}
	
	//
	
	protected boolean Update() {
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
	
	//
	
	/**
	 * Annulle la charge
	 * @param cancelled
	 */
	protected void SetCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	/**
	 * 
	 * @return Temps restant en ms
	 */
	protected long GetTimeLeft() {
		return duration - (GetTime() - started);
	}
	
	//
	
	private long GetTime() {
		return System.currentTimeMillis();
	}
	private boolean Expired() {
		return GetTimeLeft() < 0;
	}
}
