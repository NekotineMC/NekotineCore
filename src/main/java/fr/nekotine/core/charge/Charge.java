package fr.nekotine.core.charge;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import fr.nekotine.core.util.UtilTime;

public class Charge {
	
	private long started;
	private boolean cancelled;
	
	//
	
	private final String user;
	private final String chargeName;
	private final long duration;
	private final boolean displayOnExpBar;
	private final ICharge iCharge;
	
	private static final int AUDIO_BIP_NUMBER = 15;
	//
	
	/**
	 * WARNING: Si displayOnExpBar=true, alors user doit être le nom du joueur
	 * @param user Utilisateur de la charge
	 * @param chargeName Nom de la charge /!\ Doit �tre diff�rente pour chaque user
	 * @param duration Dur�e en ms
	 * @param displayOnExpBar Si la charge doit être affichée dans la barre d'exp
	 * @param iCharge Interface
	 */
	public Charge(String user, String chargeName, long duration, boolean displayOnExpBar, ICharge iCharge) {
		this.user = user;
		this.chargeName = chargeName;
		this.duration = duration;
		this.displayOnExpBar = displayOnExpBar;
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
			if(displayOnExpBar) SetExp(1);
			iCharge.Ended(user, chargeName);
			return true;
		}
		
		if(displayOnExpBar) SetExp((float)(UtilTime.GetTime() - started) / duration);
		
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
		return duration - (UtilTime.GetTime() - started);
	}
	
	//

	private boolean Expired() {
		return GetTimeLeft() < 0;
	}
	private void SetExp(float ratio) {
		Player player = Bukkit.getPlayer(user);
		if(player == null) return;
		float before = player.getExp();
		player.setExp(ratio);
		
		float denom = 1f / AUDIO_BIP_NUMBER;
		System.out.println(denom);
		System.out.println(before / denom);
		System.out.println(ratio / denom);
		
		if(Math.floor(before / denom) != Math.floor(ratio / denom)) player.playSound(player, Sound.BLOCK_DISPENSER_DISPENSE, 0.2f, ratio * 2);
	}
}
