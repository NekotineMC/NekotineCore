package fr.nekotine.core.bowcharge;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;

import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.util.UtilGear;

public class BowCharge implements ICharge{
	
	private Arrow arrow;
	//Si le joueur a tir�
	private boolean shot;
	//Si le tir a été annullé
	private boolean cancelled;
	
	//
	
	private final BowChargeManager bowChargeManager;
	private final Player user;
	private final String chargeName;
	private final long duration;
	private boolean activated;
	private final boolean displayOnExpBar;
	private final boolean withAudio;
	private final long audioBipNumber;
	private final IBowCharge iBowCharge;
	
	//
	
	public BowCharge(BowChargeManager bowChargeManager, Player user, String chargeName, long duration, boolean activated, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, 
			IBowCharge iBowCharge) {
		this.bowChargeManager = bowChargeManager;
		this.user = user;
		this.chargeName = chargeName;
		this.duration = duration;
		this.activated = activated;
		this.displayOnExpBar = displayOnExpBar;
		this.withAudio = withAudio;
		this.audioBipNumber = audioBipNumber;
		this.iBowCharge = iBowCharge;
		
		this.arrow = null;
		this.shot = false;
		
		if(activated){
			bowChargeManager.AddCharge(user, chargeName, duration, displayOnExpBar, withAudio, audioBipNumber, this);
		}
	}
	
	//
	
	protected boolean Update() {
		if(shot) {
			iBowCharge.Released(this.user, chargeName, GetTimeLeft(), arrow);
			SetCancelled();
			return true;
		}
		if(activated && !UtilGear.HasInAnyHand(user, Material.BOW)) cancelled = true;
		if(cancelled) {
			iBowCharge.Cancelled(this.user, chargeName, GetTimeLeft());
			SetCancelled();
			return true;
		}
		
		
		return false;
	}
	
	//

	public void ShootBow(EntityShootBowEvent e) {
		if(!activated) return;
		if(shot) return;
		if(!user.equals(e.getEntity())) return;

		shot = true;
		arrow = (Arrow)e.getProjectile();
	}
	public void LoadBow(PlayerReadyArrowEvent e) {
		if(shot) return;
		if(!user.equals(e.getPlayer())) return;
		
		if(activated) {
			cancelled = true;
			return;
		}

		bowChargeManager.AddCharge(user, chargeName, duration, displayOnExpBar, withAudio, audioBipNumber, this);
		activated = true;
	}
	public void OnDrop(PlayerDropItemEvent e) {
		if(shot) return;
		if(!user.equals(e.getPlayer())) return;
		if(!activated) return;
		
		cancelled = true;
	}
	
	//
	
	@Override
	public void Ended(String user, String chargeName) {
		iBowCharge.Ended(this.user, chargeName);
	}
	@Override
	public void Cancelled(String user, String chargeName, long left) {
	}
	
	//
	
	private long GetTimeLeft() {
		return Math.max(0, bowChargeManager.GetTimeLeft(user, chargeName));
	}
	private void SetCancelled() {
		bowChargeManager.SetCancelled(user, chargeName, true);
	}
	
	//
	
	public IBowCharge GetInterface() {
		return iBowCharge;
	}
	public Player GetPlayer() {
		return user;
	}
}
