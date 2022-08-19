package fr.nekotine.core.itemcharge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.Pair;

import fr.nekotine.core.charge.ChargeModule;
import fr.nekotine.core.charge.ICharge;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import fr.nekotine.core.ticking.event.TickElapsedEvent;
import fr.nekotine.core.util.CustomAction;

@ModuleNameAnnotation(Name = "ItemChargeModule")
public class ItemChargeModule extends PluginModule{
	
	private ChargeModule chargeManager;
	private final HashMap<Pair<Player, String>, ItemCharge> itemCharges = new HashMap<Pair<Player, String>, ItemCharge>();
	private final HashMap<Pair<Player, String>, ItemCharge> itemChargesBuffer = new HashMap<Pair<Player, String>, ItemCharge>();
	
	//
	
	/**
	 * LE COUPLE (user, chargeName) DOIT ETRE UNIQUE
	 * @param user Joueur qui charge
	 * @param chargeName Nom de la charge
	 * @param duration Durée de la charge en ms
	 * @param activated Si la charge avait déjà été commencée
	 * @param displayOnExpBar Si la charge doit s'afficher dans la barre d'expérience
	 * @param withAudio Si la charge doit faire du bruit au joueur
	 * @param audioBipNumber Nombre de bruits joués (sans compter celui de début & celui de fin)
	 * @param action L'action que doit faire le joueur pour que la charge continue
	 * @param bindToItem Si la charge est liée à un item
	 * @param bindItem L'objet en question
	 * @param iItemCharge
	 * @return True si la charge a été ajoutée
	 */
	public boolean AddItemCharge(Player user, String chargeName, long duration, boolean activated, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, 
			CustomAction action, boolean bindToItem, ItemStack bindItem, IItemCharge iItemCharge) {
		if(BufferExist(user, chargeName)) return false;
		
		ItemCharge itemCharge = new ItemCharge(this, user, chargeName, duration, activated, displayOnExpBar, withAudio, audioBipNumber, action, bindToItem, bindItem, iItemCharge);
		itemChargesBuffer.put(new Pair<Player, String>(user, chargeName), itemCharge);
		return true;
	}
	/**
	 * Détruit toutes les charges liées au joueur
	 * @param player
	 */
	public void DestroyFromPlayer(Player player) {
		TransferBuffer();
		for(Iterator<Entry<Pair<Player, String>, ItemCharge>> iterator = itemCharges.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, ItemCharge> entry = iterator.next();
			if(entry.getValue().GetPlayer().equals(player)) {
				SetCancelled(entry.getKey().getFirst(), entry.getKey().getSecond(), true);
				iterator.remove();
			}
		}
	}
	/**
	 * Détruit toutes les charges liées à l'interface
	 * @param iItemCharge
	 */
	public void DestroyFromInterface(IItemCharge iItemCharge) {
		TransferBuffer();
		for(Iterator<Entry<Pair<Player, String>, ItemCharge>> iterator = itemCharges.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, ItemCharge> entry = iterator.next();
			if(entry.getValue().GetInterface().equals(iItemCharge)) {
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
		for (Iterator<Entry<Pair<Player, String>, ItemCharge>> iterator = itemCharges.entrySet().iterator(); iterator.hasNext();){
			Entry<Pair<Player, String>, ItemCharge> entry = iterator.next();
			if(entry.getValue().Update()) iterator.remove();
		}
	}
	@EventHandler
	public void Action(PlayerInteractEvent e) {
		TransferBuffer();
		itemCharges.values().forEach( (charge) -> charge.Action(e));
	}
	@Override
	public void onEnable() {
		super.onEnable();
		this.chargeManager = GetPluginModule(ChargeModule.class);
	}
	
	//
	
	private boolean MainExist(Pair<Player, String> keys) {
		return itemCharges.containsKey(keys);
	}
	private boolean BufferExist(Player player, String chargeName) {
		return BufferExist(new Pair<Player, String>(player, chargeName));
	}
	private boolean BufferExist(Pair<Player, String> keys) {
		return itemChargesBuffer.containsKey(keys);
	}
	private void TransferBuffer() {
		for(Iterator<Entry<Pair<Player, String>, ItemCharge>> iterator = itemChargesBuffer.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<Player, String>, ItemCharge> entry = iterator.next();
			if(!MainExist(entry.getKey())) itemCharges.put(entry.getKey(), entry.getValue());
		}
		itemChargesBuffer.clear();
	}
}
