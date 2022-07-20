package fr.nekotine.core.itemcharge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.Pair;

import fr.nekotine.core.charge.ChargeManager;
import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import fr.nekotine.core.ticking.event.TickElapsedEvent;
import fr.nekotine.core.util.CustomAction;

@ModuleNameAnnotation(Name = "SwordChargeManager")
public class SwordChargeManager extends PluginModule{
	
	private ChargeManager chargeManager;
	private final HashMap<Pair<Player, String>, SwordCharge> swordCharges = new HashMap<Pair<Player, String>, SwordCharge>();
	private final HashMap<Pair<Player, String>, SwordCharge> swordChargesBuffer = new HashMap<Pair<Player, String>, SwordCharge>();
	
	//
	
	/**
	 * ! Le couple (user, chargeName) doit �tre unique !
	 * @param user Le joueur
	 * @param chargeName Le nom de la charge
	 * @param duration La dur�e en ms de la charge
	 * @param activated Si la charge a d�j� �t� commenc�e avant
	 * @param action L'action pour d�clencher la charge
	 * @param bindToItem Si la charge ne peut �tre d�clench�e que par un item
	 * @param bindItem L'item en question
	 * @param iSwordCharge Interface
	 * @return Si l'ajout � �t� bien pris en compte
	 */
	public boolean AddSwordCharge(Player user, String chargeName, long duration, boolean activated, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, 
			CustomAction action, boolean bindToItem, ItemStack bindItem, ISwordCharge iSwordCharge) {
		if(BufferExist(user, chargeName)) return false;
		
		SwordCharge swordCharge = new SwordCharge(this, user, chargeName, duration, activated, displayOnExpBar, withAudio, audioBipNumber, action, bindToItem, bindItem, iSwordCharge);
		swordChargesBuffer.put(new Pair<Player, String>(user, chargeName), swordCharge);
		return true;
	}
	public void DestroyFromPlayer(Player player) {
		for(Iterator<Entry<Pair<Player, String>, SwordCharge>> iterator = swordCharges.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, SwordCharge> entry = iterator.next();
			if(entry.getValue().GetPlayer().equals(player)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
		for(Iterator<Entry<Pair<Player, String>, SwordCharge>> iterator = swordChargesBuffer.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, SwordCharge> entry = iterator.next();
			if(entry.getValue().GetPlayer().equals(player)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
	}
	public void DestroyFromInterface(ISwordCharge iSwordCharge) {
		for(Iterator<Entry<Pair<Player, String>, SwordCharge>> iterator = swordCharges.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, SwordCharge> entry = iterator.next();
			if(entry.getValue().GetInterface().equals(iSwordCharge)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
		for(Iterator<Entry<Pair<Player, String>, SwordCharge>> iterator = swordChargesBuffer.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, SwordCharge> entry = iterator.next();
			if(entry.getValue().GetInterface().equals(iSwordCharge)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
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
	
	@EventHandler
	public void Tick(TickElapsedEvent e) {	
		TransferBuffer();
		
		for (Iterator<Entry<Pair<Player, String>, SwordCharge>> iterator = swordCharges.entrySet().iterator(); iterator.hasNext();){
			Entry<Pair<Player, String>, SwordCharge> entry = iterator.next();
			if(entry.getValue().Update()) iterator.remove();
		}
		
		TransferBuffer();
	}
	@EventHandler
	public void Action(PlayerInteractEvent e) {
		swordCharges.values().forEach( (charge) -> charge.Action(e));
	}
	@Override
	public void onEnable() {
		super.onEnable();
		this.chargeManager = GetPluginModule(ChargeManager.class);
	}
	
	//
	
	private boolean Exist(Pair<Player, String> keys) {
		return swordCharges.containsKey(keys);
	}
	private boolean BufferExist(Player player, String chargeName) {
		return BufferExist(new Pair<Player, String>(player, chargeName));
	}
	private boolean BufferExist(Pair<Player, String> keys) {
		return swordChargesBuffer.containsKey(keys);
	}
	private void TransferBuffer() {
		for(Iterator<Entry<Pair<Player, String>, SwordCharge>> iterator = swordChargesBuffer.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, SwordCharge> entry = iterator.next();
			if(!Exist(entry.getKey())) {
				swordCharges.put(entry.getKey(), entry.getValue());
				iterator.remove();
			}
		}
	}
}
