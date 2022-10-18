package fr.nekotine.core.charge;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import fr.nekotine.core.util.TimeUtil;

public class Charge {
	
	private long started;
	private boolean cancelled;
	private long lastLeft;
	private boolean paused;
	private long pausedTime;
	
	//
	
	private final String user;
	private final String chargeName;
	private long duration;
	private final boolean displayOnExpBar;
	private final boolean withAudio;
	private final long audioBipNumber;
	private final ICharge iCharge;
	
	//
	
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
		this.paused = false;
		
		PlayUncheckedAudio(0);
	}
	
	//
	
	protected boolean Update() {
		if(paused) return false;
		
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
	
	protected void SetCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	protected long GetTimeLeft() {
		return duration - (TimeUtil.GetTime() - started);
	}
	protected void SetPaused(boolean paused) {
		if(this.paused && !paused) {
			duration += TimeUtil.Passed(pausedTime);
		}else if(!this.paused && paused) {
			pausedTime = TimeUtil.GetTime();
		}
		this.paused = paused;
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
		float ratio = (float)(TimeUtil.GetTime() - started) / duration;
		PlayAudio(ratio);
	}
	private void SetExp() {
		if(!displayOnExpBar) return;
		
		Player player = Bukkit.getPlayer(user);
		if(player == null) return;
		
		float ratio = (float)(TimeUtil.GetTime() - started) / duration;
		player.setExp(ratio);
	}
	private void PlayUncheckedAudio(float ratio) {
		if(!withAudio) return;
		
		Player player = Bukkit.getPlayer(user);
		if(player == null) return;
		
		player.playSound(player, Sound.BLOCK_DISPENSER_DISPENSE, 0.2f, ratio);
	}
}
