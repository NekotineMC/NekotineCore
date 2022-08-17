package fr.nekotine.core.bowcharge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.comphenix.protocol.wrappers.Pair;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;

import fr.nekotine.core.charge.ChargeModule;
import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import fr.nekotine.core.ticking.event.TickElapsedEvent;

@ModuleNameAnnotation(Name = "BowChargeModule")
public class BowChargeModule extends PluginModule{
	
	private ChargeModule chargeManager;
	private final HashMap<Pair<Player, String>, BowCharge> bowCharges = new HashMap<Pair<Player, String>, BowCharge>();
	private final HashMap<Pair<Player, String>, BowCharge> bowChargesBuffer = new HashMap<Pair<Player, String>, BowCharge>();
	
	//
	
	/**
	 * LE COUPLE (user, chargeName) DOIT ETRE UNIQUE
	 * @param user Joueur qui tir
	 * @param chargeName Nom de la charge
	 * @param duration Durée de la charge en ms
	 * @param activated Si la charge avait déjà commencée
	 * @param displayOnExpBar Si la charge doit s'afficher dans la barre d'expérience
	 * @param withAudio Si la charge doit faire du bruit au joueur
	 * @param audioBipNumber Nombre de bruits joués (sans compter celui de début & celui de fin)
	 * @param iBowCharge
	 * @return True si la charge a été ajoutée
	 */
	public boolean AddBowCharge(Player user, String chargeName, long duration, boolean activated, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, IBowCharge iBowCharge) {	
		if(BufferExist(user, chargeName)) return false;
		
		BowCharge bowCharge = new BowCharge(this, user, chargeName, duration, activated, displayOnExpBar, withAudio, audioBipNumber, iBowCharge);
		bowChargesBuffer.put(new Pair<Player, String>(user, chargeName), bowCharge);
		return true;
	}
	/**
	 * Détruit toutes les charges liées au joueur
	 * @param player
	 */
	public void DestroyFromPlayer(Player player) {
		TransferBuffer();
		for(Iterator<Entry<Pair<Player, String>, BowCharge>> iterator = bowCharges.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, BowCharge> entry = iterator.next();
			if(entry.getValue().GetPlayer().equals(player)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
	}
	/**
	 * Détruit toutes les charges liées à l'interface
	 * @param iBowCharge
	 */
	public void DestroyFromInterface(IBowCharge iBowCharge) {
		TransferBuffer();
		for(Iterator<Entry<Pair<Player, String>, BowCharge>> iterator = bowCharges.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, BowCharge> entry = iterator.next();
			if(entry.getValue().GetInterface().equals(iBowCharge)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
	}
	
	//
	
	@EventHandler
	public void Tick(TickElapsedEvent e) {
		TransferBuffer();
		for (Iterator<Entry<Pair<Player, String>, BowCharge>> iterator = bowCharges.entrySet().iterator(); iterator.hasNext();){
			Entry<Pair<Player, String>, BowCharge> entry = iterator.next();
			if(entry.getValue().Update()) iterator.remove();
		}
	}
	@EventHandler
	public void ShootBow(EntityShootBowEvent e) {
		TransferBuffer();
		bowCharges.values().forEach( (charge) -> charge.ShootBow(e));
	}
	@EventHandler
	public void LoadBow(PlayerReadyArrowEvent e) {
		TransferBuffer();
		bowCharges.values().forEach( (charge) -> charge.LoadBow(e));
	}
	@EventHandler
	public void OnDrop(PlayerDropItemEvent e) {
		TransferBuffer();
		bowCharges.values().forEach( (charge) -> charge.OnDrop(e));
	}
	
	//
	
	@Override
	public void onEnable() {
		super.onEnable();
		this.chargeManager = GetPluginModule(ChargeModule.class);
	}
	
	//
	
	public long GetTimeLeft(Player user, String chargeName) {
		return chargeManager.GetTimeLeft(user.getName(), chargeName);
	}
	protected boolean SetCancelled(Player user, String chargeName, boolean cancelled) {
		return chargeManager.SetCancelled(user.getName(), chargeName, cancelled);
	}
	protected boolean AddCharge(Player user, String chargeName, long duration, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, ICharge iCharge) {
		return chargeManager.AddCharge(user.getName(), chargeName, duration, displayOnExpBar, withAudio, audioBipNumber, iCharge);
	}
	
	//
	
	private boolean MainExist(Pair<Player, String> keys) {
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
			if(!MainExist(entry.getKey())) bowCharges.put(entry.getKey(), entry.getValue());
		}
		bowChargesBuffer.clear();
	}
}
