package fr.nekotine.core.combatcharge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.charge.ChargeManager;
import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.module.PluginModule;

public class BowChargeManager extends PluginModule{
	
	private ChargeManager chargeManager;
	private HashMap<Player, BowCharge> bowCharges = new HashMap<Player,BowCharge>();
	
	//
	
	public BowChargeManager(JavaPlugin plugin, ChargeManager chargeManager) {
		super(plugin, "BowChargeManager");
		this.chargeManager = chargeManager;
	}
	
	//
	
	/**
	 * ! Le couple (user, chargeName) doit être unique !
	 * @param user Le joueur qui tire
	 * @param chargeName Le nom de la charge
	 * @param duration La durée maximale de la charge en ms
	 * @param activated Si le joueur avais déjà commencé à bander son arc
	 * @param iBowCharge Interface
	 * @return Si l'ajout à été bien pris en compte
	 */
	public boolean AddBowCharge(Player user, String chargeName, long duration, boolean activated, IBowCharge iBowCharge) {	
		if(!Exist(user)) {
			BowCharge bowCharge = new BowCharge(this, user, chargeName, duration, activated, iBowCharge);
			bowCharges.put(user, bowCharge);
			return true;
		}
		return false;
	}
	
	//
	
	@EventHandler
	public void Tick(/* inserer tick event ici */) {
		for (Iterator<Entry<Player, BowCharge>> iterator = bowCharges.entrySet().iterator(); iterator.hasNext();){
			Entry<Player, BowCharge> entry = iterator.next();
			if(entry.getValue().Update()) iterator.remove();
		}
	}
	
	//
	
	protected long GetTimeLeft(Player user, String chargeName) {
		if(Exist(user)) {
			return chargeManager.GetTimeLeft(chargeName, chargeName);
		}
		return -1;
	}
	protected boolean SetCancelled(Player user, String chargeName, boolean cancelled) {
		return chargeManager.SetCancelled(user.getName(), chargeName, cancelled);
	}
	protected boolean AddCharge(Player user, String chargeName, long duration, ICharge iCharge) {
		return chargeManager.AddCharge(user.getName(), chargeName, duration, iCharge);
	}
	
	//
	
	private boolean Exist(Player player) {
		return bowCharges.containsKey(player);
	}

}
