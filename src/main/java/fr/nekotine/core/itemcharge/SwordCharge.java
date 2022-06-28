package fr.nekotine.core.itemcharge;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.util.CustomAction;
import fr.nekotine.core.util.UtilEvent;
import fr.nekotine.core.util.UtilGear;
import fr.nekotine.core.util.UtilTime;

public class SwordCharge implements ICharge{
	
	//Délai maximal entre deux appel d'Interract Event afin de constater la fin de la charge
	private final long RELEASE_DELAY_MS = 100;
	
	//Si le joueur a arrêté l'action
	private boolean released;
	//Dernière fois que l'action était réalisée
	private long lastFired;
	//
	
	private SwordChargeManager swordChargeManager;
	private Player user;
	private String chargeName;
	private long duration;
	private boolean activated;
	private CustomAction action;
	private boolean bindToItem;
	private ItemStack bindItem;
	private ISwordCharge iSwordCharge;
	
	public SwordCharge(SwordChargeManager swordChargeManager, Player user, String chargeName, long duration, boolean activated, CustomAction action,
			boolean bindToItem, ItemStack bindItem, ISwordCharge iSwordCharge) {
		this.swordChargeManager = swordChargeManager;
		this.user = user;
		this.chargeName = chargeName;
		this.duration = duration;
		this.activated = activated;
		this.bindToItem = bindToItem;
		this.bindItem = bindItem;
		this.iSwordCharge = iSwordCharge;
		
		this.released = false;
		
		if(activated){
			swordChargeManager.AddCharge(user, chargeName, duration, this);
		}
	}
	
	//
	
	public boolean Update() {
		if(released) {
			SetCancelled();
			iSwordCharge.Released(user, chargeName, GetTimeLeft());
			return true;
		}
		return false;
	}
	
	//
	
	public void Action(PlayerInteractEvent e) {
		if(released) return;
		if(!user.equals(e.getPlayer())) return;
		
		if(!activated) {
			if(!UtilEvent.IsAction(e.getAction(), action)) return;
			if(bindToItem && !UtilGear.IsItem(bindItem, e.getItem())) return;
			
			activated = true;
			swordChargeManager.AddCharge(user, chargeName, duration, this);
			
		}else {
			
			if(bindToItem && !UtilGear.IsItem(bindItem, e.getItem())) {
				released = true;
				
			}else {
				if(UtilEvent.IsAction(e.getAction(), action)) return;
				if(UtilTime.Passed(lastFired) <= RELEASE_DELAY_MS) return;
				
				released = true;
			}
		}
	}
	
	//
	
	@Override
	public void Ended(String user, String chargeName) {
		iSwordCharge.Ended(this.user, chargeName);
	}

	@Override
	public void Cancelled(String user, String chargeName, long left) {
	}
	
	//
	
	private long GetTimeLeft() {
		return Math.max(0, swordChargeManager.GetTimeLeft(user, chargeName));
	}
	private void SetCancelled() {
		swordChargeManager.SetCancelled(user, chargeName, true);
	}

}
