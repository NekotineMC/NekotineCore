package fr.nekotine.core.bowcharge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

import com.comphenix.protocol.wrappers.Pair;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;

import fr.nekotine.core.arrache.TickEvent;
import fr.nekotine.core.charge.ChargeManager;
import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "BowChargeManager")
public class BowChargeManager extends PluginModule{
	
	private ChargeManager chargeManager;
	private final HashMap<Pair<Player, String>, BowCharge> bowCharges = new HashMap<Pair<Player, String>, BowCharge>();
	private final HashMap<Pair<Player, String>, BowCharge> bowChargesBuffer = new HashMap<Pair<Player, String>, BowCharge>();
	
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
	public boolean AddBowCharge(Player user, String chargeName, long duration, boolean activated, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, IBowCharge iBowCharge) {	
		if(BufferExist(user, chargeName)) return false;
		
		BowCharge bowCharge = new BowCharge(this, user, chargeName, duration, activated, displayOnExpBar, withAudio, audioBipNumber, iBowCharge);
		bowChargesBuffer.put(new Pair<Player, String>(user, chargeName), bowCharge);
		return true;
	}
	public void DestroyFromPlayer(Player player) {
		for(Iterator<Entry<Pair<Player, String>, BowCharge>> iterator = bowCharges.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, BowCharge> entry = iterator.next();
			if(entry.getValue().GetPlayer().equals(player)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
		for(Iterator<Entry<Pair<Player, String>, BowCharge>> iterator = bowChargesBuffer.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, BowCharge> entry = iterator.next();
			if(entry.getValue().GetPlayer().equals(player)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
	}
	public void DestroyFromInterface(IBowCharge iBowCharge) {
		for(Iterator<Entry<Pair<Player, String>, BowCharge>> iterator = bowCharges.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, BowCharge> entry = iterator.next();
			if(entry.getValue().GetInterface().equals(iBowCharge)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
		for(Iterator<Entry<Pair<Player, String>, BowCharge>> iterator = bowChargesBuffer.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, BowCharge> entry = iterator.next();
			if(entry.getValue().GetInterface().equals(iBowCharge)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
	}
	
	//
	
	@EventHandler
	public void Tick(TickEvent e) {
		TransferBuffer();
		
		for (Iterator<Entry<Pair<Player, String>, BowCharge>> iterator = bowCharges.entrySet().iterator(); iterator.hasNext();){
			Entry<Pair<Player, String>, BowCharge> entry = iterator.next();
			if(entry.getValue().Update()) iterator.remove();
		}
		
		TransferBuffer();
	}
	@EventHandler
	public void ShootBow(EntityShootBowEvent e) {
		bowCharges.values().forEach( (charge) -> charge.ShootBow(e));
	}
	@EventHandler
	public void LoadBow(PlayerReadyArrowEvent e) {
		bowCharges.values().forEach( (charge) -> charge.LoadBow(e));
	}
	
	//
	
	@Override
	public void onEnable() {
		super.onEnable();
		this.chargeManager = GetPluginModule(ChargeManager.class);
	}
	
	//
	
	protected long GetTimeLeft(Player user, String chargeName) {
		return chargeManager.GetTimeLeft(user.getName(), chargeName);
	}
	protected boolean SetCancelled(Player user, String chargeName, boolean cancelled) {
		return chargeManager.SetCancelled(user.getName(), chargeName, cancelled);
	}
	protected boolean AddCharge(Player user, String chargeName, long duration, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, ICharge iCharge) {
		return chargeManager.AddCharge(user.getName(), chargeName, duration, displayOnExpBar, withAudio, audioBipNumber, iCharge);
	}
	
	//
	
	private boolean Exist(Pair<Player, String> keys) {
		return bowCharges.containsKey(keys);
	}
	private boolean BufferExist(Player player, String chargeName) {
		return BufferExist(new Pair<Player, String>(player, chargeName));
	}
	private boolean BufferExist(Pair<Player, String> keys) {
		return bowChargesBuffer.containsKey(keys);
	}
	private void TransferBuffer() {
		for(Iterator<Entry<Pair<Player, String>, BowCharge>> iterator = bowChargesBuffer.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, BowCharge> entry = iterator.next();
			if(!Exist(entry.getKey())) {
				bowCharges.put(entry.getKey(), entry.getValue());
				iterator.remove();
			}
		}
	}
}
