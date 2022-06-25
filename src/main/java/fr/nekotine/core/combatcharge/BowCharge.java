package fr.nekotine.core.combatcharge;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.nekotine.core.charge.ChargeManager;
import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.util.CustomAction;
import fr.nekotine.core.util.UtilEvent;
import fr.nekotine.core.util.UtilGear;

public class BowCharge implements ICharge{
	
	private ChargeManager chargeManager;
	private Player user;
	private String chargeName;
	
	//True si la personne a bandé son arc
	private boolean activated;
	
	private IBowCharge iBowCharge;
	
	//La flèche à renvoyer lors du tir
	private Arrow arrow;
	
	public BowCharge(ChargeManager chargeManager, Player user, String chargeName, long duration, boolean activated, IBowCharge iBowCharge) {
		this.chargeManager = chargeManager;
		this.user = user;
		this.chargeName = chargeName;
		this.activated = activated;
		this.iBowCharge = iBowCharge;
		
		this.arrow = null;
				
		//Si le joueur avait déjà bandé l'arc
		if(activated){
			chargeManager.AddCharge(user.getName(), chargeName, duration, this);
		}
	}
	
	@EventHandler
	public void Tick(/* Tick event ici*/) {
		if(!activated) return;
		if(UtilGear.HasInAnyHand(user, Material.BOW)) return;
		
		chargeManager.SetCancelled(user.getName(), chargeName, true);
	}
	@EventHandler
	public void LoadBow(PlayerInteractEvent e) {
		if(activated) return;
		if(!user.equals(e.getPlayer())) return;
		if(!UtilGear.IsMaterial(e.getItem(), Material.BOW)) return;
		if(!UtilEvent.IsAction(e.getAction(), CustomAction.RIGHT_CLICK)) return;
		if(!UtilGear.HasMaterial(user, Material.ARROW)) return;
		
		activated = true;
	}
	@EventHandler
	public void ShootBow(EntityShootBowEvent e) {
		if(!activated) return;
		if(!user.equals(e.getEntity())) return;
		
		arrow = (Arrow)e.getProjectile();
		chargeManager.SetCancelled(user.getName(), chargeName, true);
	}
	
	
	@Override
	public void Ended(String user, String chargeName) {
		iBowCharge.Ended(this.user, chargeName);
	}

	@Override
	public void Cancelled(String user, String chargeName, long left) {
		//arrow != null => le joueur a tiré
		if(arrow != null) {
			iBowCharge.Released(this.user, chargeName, left, arrow);
		}else {
			iBowCharge.Cancelled(this.user, chargeName, left);
		}
	}

}
