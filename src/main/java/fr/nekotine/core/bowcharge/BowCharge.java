package fr.nekotine.core.bowcharge;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.util.CustomAction;
import fr.nekotine.core.util.UtilEvent;
import fr.nekotine.core.util.UtilGear;

public class BowCharge implements ICharge{
	
	private Arrow arrow;
	//Si le joueur a tir�
	private boolean shot;
	
	//
	
	private final BowChargeManager bowChargeManager;
	private final Player user;
	private final String chargeName;
	private final long duration;
	private boolean activated;
	private final IBowCharge iBowCharge;
	
	//
	
	public BowCharge(BowChargeManager bowChargeManager, Player user, String chargeName, long duration, boolean activated, IBowCharge iBowCharge) {
		this.bowChargeManager = bowChargeManager;
		this.user = user;
		this.chargeName = chargeName;
		this.duration = duration;
		this.activated = activated;
		this.iBowCharge = iBowCharge;
		
		this.arrow = null;
		this.shot = false;
		
		if(activated){
			bowChargeManager.AddCharge(user, chargeName, duration, this);
		}
	}
	
	//
	
	protected boolean Update() {
		if(shot) {
			SetCancelled();
			iBowCharge.Released(this.user, chargeName, GetTimeLeft(), arrow);
			return true;
		}
		if(activated && !UtilGear.HasInAnyHand(user, Material.BOW)) {
			SetCancelled();
			iBowCharge.Cancelled(this.user, chargeName, GetTimeLeft());
			return true;
		}
		return false;
	}
	
	//
	
	public void LoadBow(PlayerInteractEvent e) {
		if(activated) return;
		if(shot) return;
		if(!user.equals(e.getPlayer())) return;
		if(!UtilGear.IsMaterial(e.getItem(), Material.BOW)) return;
		if(!UtilEvent.IsAction(e.getAction(), CustomAction.RIGHT_CLICK)) return;
		if(!UtilGear.HasMaterial(user, Material.ARROW)) return;
		
		bowChargeManager.AddCharge(user, chargeName, duration, this);
		activated = true;
	}
	public void ShootBow(EntityShootBowEvent e) {
		if(!activated) return;
		if(shot) return;
		if(!user.equals(e.getEntity())) return;

		shot = true;
		arrow = (Arrow)e.getProjectile();
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
}
