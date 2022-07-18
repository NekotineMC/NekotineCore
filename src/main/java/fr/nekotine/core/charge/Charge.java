package fr.nekotine.core.charge;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import fr.nekotine.core.util.UtilTime;

public class Charge {
	
	private long started;
	private boolean cancelled;
	private long lastLeft;
	
	//
	
	private final String user;
	private final String chargeName;
	private final long duration;
	private final boolean displayOnExpBar;
	private final boolean withAudio;
	private final long audioBipNumber;
	private final ICharge iCharge;
	
	//
	
	/**
	 * WARNING: Si displayOnExpBar=true, alors user doit être le nom du joueur
	 * @param user Utilisateur de la charge
	 * @param chargeName Nom de la charge /!\ Doit �tre diff�rente pour chaque user
	 * @param duration Dur�e en ms
	 * @param displayOnExpBar Si la charge doit être affichée dans la barre d'exp
	 * @param iCharge Interface
	 */
	public Charge(String user, String chargeName, long duration, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, ICharge iCharge) {
		this.user = user;
		this.chargeName = chargeName;
		this.duration = duration;
		this.displayOnExpBar = displayOnExpBar;
		this.withAudio = withAudio;
		this.audioBipNumber = audioBipNumber + 1;
		this.iCharge = iCharge;
		
		this.started = System.currentTimeMillis();
		this.cancelled = false;
		this.lastLeft = duration;
		
		PlayUncheckedAudio(0);
	}
	
	//
	
	protected boolean Update() {
		if(cancelled) {
			iCharge.Cancelled(user, chargeName, GetTimeLeft());
			return true;
		}
		if(Expired()) {
			PlayAudio(1);
			iCharge.Ended(user, chargeName);
			return true;
		}
		
		PlayIndicators();
		
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
	private void PlayIndicators() {
		SetExp();
		PlayAudio();
		
		lastLeft = GetTimeLeft();
	}
	private boolean CanPlayAudio(Player player, float ratio) {
		if(audioBipNumber < 0) return false;
		
		float denom = (float)duration / audioBipNumber;
		return (Math.ceil(lastLeft / denom) != Math.ceil(GetTimeLeft() / denom));
	}
	private void PlayAudio(float ratio) {
		if(!withAudio) return;
		
		Player player = Bukkit.getPlayer(user);
		if(player == null) return;
		
		if(!CanPlayAudio(player, ratio)) return;
		player.playSound(player, Sound.BLOCK_DISPENSER_DISPENSE, 0.2f, ratio);
	}
	private void PlayAudio() {
		float ratio = (float)(UtilTime.GetTime() - started) / duration;
		PlayAudio(ratio);
	}
	private void SetExp() {
		if(!displayOnExpBar) return;
		
		Player player = Bukkit.getPlayer(user);
		if(player == null) return;
		
		float ratio = (float)(UtilTime.GetTime() - started) / duration;
		player.setExp(ratio);
	}
	private void PlayUncheckedAudio(float ratio) {
		if(!withAudio) return;
		
		Player player = Bukkit.getPlayer(user);
		if(player == null) return;
		
		player.playSound(player, Sound.BLOCK_DISPENSER_DISPENSE, 0.2f, ratio);
	}
	
	
	
	
}
