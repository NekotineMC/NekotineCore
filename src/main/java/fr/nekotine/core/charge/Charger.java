package fr.nekotine.core.charge;

public class Charger {
	
	private long started;
	
	private String chargeName;
	private long duration;
	private ICharge iCharge;
	
	public Charger(String chargeName, long duration, ICharge iCharge) {
		this.chargeName = chargeName;
		this.duration = duration;
		this.iCharge = iCharge;
		
		started = System.currentTimeMillis();
	}
	
	public boolean Charged() {
		if(Expired()) {
			//iCharge.
			return true;
		}
		return false;
	}
	
	private boolean Expired() {
		return System.currentTimeMillis() - started >= duration;
	}
}
