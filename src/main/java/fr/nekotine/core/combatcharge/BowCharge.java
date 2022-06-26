package fr.nekotine.core.combatcharge;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.util.CustomAction;
import fr.nekotine.core.util.UtilEvent;
import fr.nekotine.core.util.UtilGear;

public class BowCharge implements ICharge{
	
	private Arrow arrow;
	//Si le joueur a tiré
	private boolean shot;
	//Si la charge est terminée
	private boolean ended;
	
	//
	
	private BowChargeManager bowChargeManager;
	private Player user;
	private String chargeName;
	private long duration;
	private boolean activated;
	private IBowCharge iBowCharge;
	
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
		this.ended = false;
		
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
	
	@EventHandler
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
	@EventHandler
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
		ended = true;
		iBowCharge.Ended(this.user, chargeName);
	}
	@Override
	public void Cancelled(String user, String chargeName, long left) {
	}
	
	//
	
	private long GetTimeLeft() {
		if(ended) return 0;
		return bowChargeManager.GetTimeLeft(user, chargeName);
	}
	private void SetCancelled() {
		if(!ended) bowChargeManager.SetCancelled(user, chargeName, true);
	}
}
