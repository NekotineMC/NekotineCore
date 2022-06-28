package fr.nekotine.core.itemcharge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.charge.ChargeManager;
import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.util.CustomAction;

public class SwordChargeManager extends PluginModule{
	
	private ChargeManager chargeManager;
	private HashMap<Player, SwordCharge> swordCharges = new HashMap<Player,SwordCharge>();
	
	public SwordChargeManager(JavaPlugin plugin, ModuleManager manager) {
		super(plugin,"SwordChargeManager", manager);
		this.chargeManager = (ChargeManager)GetPluginModule(ChargeManager.GetName());
	}

	//
	
	/**
	 * ! Le couple (user, chargeName) doit être unique !
	 * @param user Le joueur
	 * @param chargeName Le nom de la charge
	 * @param duration La durée en ms de la charge
	 * @param activated Si la charge a déjà été commencée avant
	 * @param action L'action pour déclencher la charge
	 * @param bindToItem Si la charge ne peut être déclenchée que par un item
	 * @param bindItem L'item en question
	 * @param iSwordCharge Interface
	 * @return Si l'ajout à été bien pris en compte
	 */
	public boolean AddSwordCharge(Player user, String chargeName, long duration, boolean activated, CustomAction action, boolean bindToItem, ItemStack bindItem, 
			ISwordCharge iSwordCharge) {
		if(!Exist(user)) {
			SwordCharge swordCharge = new SwordCharge(this, user, chargeName, duration, activated, action, bindToItem, bindItem, iSwordCharge);
			swordCharges.put(user, swordCharge);
			return true;
		}
		return false;
	}
	
	//
	
	protected long GetTimeLeft(Player user, String chargeName) {
		return chargeManager.GetTimeLeft(chargeName, chargeName);
	}
	protected boolean SetCancelled(Player user, String chargeName, boolean cancelled) {
		return chargeManager.SetCancelled(user.getName(), chargeName, cancelled);
	}
	protected boolean AddCharge(Player user, String chargeName, long duration, ICharge iCharge) {
		return chargeManager.AddCharge(user.getName(), chargeName, duration, iCharge);
	}
	
	//
	
	@EventHandler
	public void Tick(/* inserer tick event ici */) {
		for (Iterator<Entry<Player, SwordCharge>> iterator = swordCharges.entrySet().iterator(); iterator.hasNext();){
			Entry<Player, SwordCharge> entry = iterator.next();
			if(entry.getValue().Update()) iterator.remove();
		}
	}
	@EventHandler
	public void Action(PlayerInteractEvent e) {
		swordCharges.values().forEach( (charge) -> charge.Action(e));
	}
	
	//
	
	private boolean Exist(Player player) {
		return swordCharges.containsKey(player);
	}
}
