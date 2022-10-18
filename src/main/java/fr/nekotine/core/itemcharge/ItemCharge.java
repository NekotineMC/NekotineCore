package fr.nekotine.core.itemcharge;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.util.CustomAction;
import fr.nekotine.core.util.EventUtil;
import fr.nekotine.core.util.GearUtil;
import fr.nekotine.core.util.TimeUtil;

public class ItemCharge implements ICharge{
	
	//D�lai maximal entre deux appel d'Interract Event afin de constater la fin de la charge
	private final long RELEASE_DELAY_MS = 350;
	
	private boolean released;
	private long lastFired;
	//
	
	private final ItemChargeModule swordChargeManager;
	private final Player user;
	private final String chargeName;
	private final long duration;
	private boolean activated;
	private final boolean displayOnExpBar;
	private final boolean withAudio;
	private final long audioBipNumber;
	private final CustomAction action;
	private final boolean bindToItem;
	private final ItemStack bindItem;
	private final IItemCharge iSwordCharge;
	
	public ItemCharge(ItemChargeModule swordChargeManager, Player user, String chargeName, long duration, boolean activated, boolean displayOnExpBar, boolean withAudio, long audioBipNumber,
			CustomAction action, boolean bindToItem, ItemStack bindItem, IItemCharge iSwordCharge) {
		this.swordChargeManager = swordChargeManager;
		this.user = user;
		this.chargeName = chargeName;
		this.duration = duration;
		this.activated = activated;
		this.displayOnExpBar = displayOnExpBar;
		this.withAudio = withAudio;
		this.audioBipNumber = audioBipNumber;
		this.action = action;
		this.bindToItem = bindToItem;
		this.bindItem = bindItem;
		this.iSwordCharge = iSwordCharge;
		
		this.released = false;
		
		if(activated){
			lastFired = TimeUtil.GetTime();
			swordChargeManager.AddCharge(user, chargeName, duration, displayOnExpBar, withAudio, audioBipNumber, this);
		}
	}
	
	//
	
	public boolean Update() {
		if(activated && TimeUtil.Passed(lastFired) > RELEASE_DELAY_MS) released = true;
		if(activated && bindToItem && !GearUtil.HasInAnyHand(user, bindItem)) released = true;
		
		if(released) {
			iSwordCharge.Released(user, chargeName, GetTimeLeft());
			SetCancelled();
			return true;
		}
		
		return false;
	}
	
	//
	
	@EventHandler
	public void Action(PlayerInteractEvent e) {
		if(released) return;
		if(!user.equals(e.getPlayer())) return;
		if(!EventUtil.IsAction(e.getAction(), action)) return;
		if(bindToItem && !GearUtil.IsItem(bindItem, e.getItem())) return;
		
		if(!activated) {
			activated = true;
			swordChargeManager.AddCharge(user, chargeName, duration, displayOnExpBar, withAudio, audioBipNumber, this);
		}
		lastFired = TimeUtil.GetTime();
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
	
	//
	
	public IItemCharge GetInterface() {
		return iSwordCharge;
	}
	public Player GetPlayer() {
		return user;
	}

}
