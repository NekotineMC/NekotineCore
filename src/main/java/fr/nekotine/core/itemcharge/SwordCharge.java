package fr.nekotine.core.itemcharge;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.util.CustomAction;
import fr.nekotine.core.util.UtilEvent;
import fr.nekotine.core.util.UtilGear;
import fr.nekotine.core.util.UtilTime;

public class SwordCharge implements ICharge{
	
	//D�lai maximal entre deux appel d'Interract Event afin de constater la fin de la charge
	private final long RELEASE_DELAY_MS = 500;
	
	//Si le joueur a arr�t� l'action
	private boolean released;
	//Derni�re fois que l'action �tait r�alis�e
	private long lastFired;
	//
	
	private final SwordChargeManager swordChargeManager;
	private final Player user;
	private final String chargeName;
	private final long duration;
	private boolean activated;
	private final CustomAction action;
	private final boolean bindToItem;
	private final ItemStack bindItem;
	private final ISwordCharge iSwordCharge;
	
	public SwordCharge(SwordChargeManager swordChargeManager, Player user, String chargeName, long duration, boolean activated, CustomAction action,
			boolean bindToItem, ItemStack bindItem, ISwordCharge iSwordCharge) {
		this.swordChargeManager = swordChargeManager;
		this.user = user;
		this.chargeName = chargeName;
		this.duration = duration;
		this.activated = activated;
		this.action = action;
		this.bindToItem = bindToItem;
		this.bindItem = bindItem;
		this.iSwordCharge = iSwordCharge;
		
		this.released = false;
		
		if(activated){
			lastFired = UtilTime.GetTime();
			swordChargeManager.AddCharge(user, chargeName, duration, this);
		}
	}
	
	//
	
	public boolean Update() {
		if(activated && bindToItem && !UtilGear.HasInAnyHand(user, bindItem)) released = true;
		if(activated && UtilTime.Passed(lastFired) > RELEASE_DELAY_MS) {
			released = true;
		}
		
		if(released) {
			SetCancelled();
			iSwordCharge.Released(user, chargeName, GetTimeLeft());
			return true;
		}
		
		return false;
	}
	
	//
	
	@EventHandler
	public void Action(PlayerInteractEvent e) {
		if(released) return;
		if(!user.equals(e.getPlayer())) return;
		if(!UtilEvent.IsAction(e.getAction(), action)) return;
		if(bindToItem && !UtilGear.IsItem(bindItem, e.getItem())) return;
		
		if(!activated) {
			activated = true;
			swordChargeManager.AddCharge(user, chargeName, duration, this);
		}
		lastFired = UtilTime.GetTime();
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
