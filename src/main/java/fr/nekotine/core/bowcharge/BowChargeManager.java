package fr.nekotine.core.bowcharge;

import java.util.HashMap;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.nekotine.core.arrache.TickEvent;
import fr.nekotine.core.charge.ChargeManager;
import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "BowChargeManager")
public class BowChargeManager extends PluginModule{
	
	private ChargeManager chargeManager;
	private final HashMap<Player, BowCharge> bowCharges = new HashMap<Player,BowCharge>();
	
	//
	
	/**
	 * ! Le couple (user, chargeName) doit �tre unique !
	 * @param user Le joueur qui tire
	 * @param chargeName Le nom de la charge
	 * @param duration La dur�e maximale de la charge en ms
	 * @param activated Si le joueur avais d�j� commenc� � bander son arc
	 * @param iBowCharge Interface
	 * @return Si l'ajout � �t� bien pris en compte
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
	public void Tick(TickEvent e) {
		for (Iterator<Entry<Player, BowCharge>> iterator = bowCharges.entrySet().iterator(); iterator.hasNext();){
			Entry<Player, BowCharge> entry = iterator.next();
			if(entry.getValue().Update()) iterator.remove();
		}
	}
	
	@EventHandler
	public void LoadBow(PlayerInteractEvent e) {
		bowCharges.values().forEach( (charge) -> charge.LoadBow(e));
	}
	
	@EventHandler
	public void ShootBow(EntityShootBowEvent e) {
		bowCharges.values().forEach( (charge) -> charge.ShootBow(e));
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		this.chargeManager = GetPluginModule(ChargeManager.class);
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
	
	private boolean Exist(Player player) {
		return bowCharges.containsKey(player);
	}

}
