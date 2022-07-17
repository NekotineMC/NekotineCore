package fr.nekotine.core.charge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.event.EventHandler;

import com.comphenix.protocol.wrappers.Pair;

import fr.nekotine.core.arrache.TickEvent;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "ChargeManager")
public class ChargeManager extends PluginModule{
	private static final String NAME = "ChargeManager";
	
	private final HashMap<Pair<String, String>, Charge> charges = new HashMap<Pair<String, String>, Charge>();
	private final HashMap<Pair<String, String>, Charge> chargesBuffer = new HashMap<Pair<String, String>, Charge>();
	
	//
	
	/**
	 * ! Le couple (user, chargeName) doit �tre unique !
	 * @param user L'utilisateur de la charge
	 * @param chargeName Le nom de la charge
	 * @param duration Dur�e en ms
	 * @param displayOnExpBar Si la charge doit être affichée dans la barre d'exp
	 * @param withAudio Si la charge doit jouer un audio
	 * @param audioBipNumber Le nombre de bips audio par charge
	 * @param iCharge Interface
	 * @return True si la charge a �t� ajout�e
	 */
	public boolean AddCharge(String user, String chargeName, long duration, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, ICharge iCharge) {
		if(BufferExist(user, chargeName)) return false;
		
		Charge charge = new Charge(user, chargeName, duration, displayOnExpBar, withAudio, audioBipNumber, iCharge);
		chargesBuffer.put(new Pair<String, String>(user, chargeName), charge);
		return true;
	}
	
	//
	
	public boolean SetCancelled(String user, String chargeName, boolean cancelled) {
		if(Exist(user, chargeName)) {
			Get(user, chargeName).SetCancelled(cancelled);
			return true;
		}
		return false;
	}
	public long GetTimeLeft(String user, String chargeName) {
		if(Exist(user, chargeName)) {
			return Get(user, chargeName).GetTimeLeft();
		}
		return -1;
	}
	public boolean Exist(String user, String chargeName) {
		return Exist(new Pair<String, String>(user, chargeName));
	}
	public boolean Exist(Pair<String, String> keys) {
		return charges.containsKey(keys);
	}
	
	//
	
	@EventHandler
	public void Tick(TickEvent e) {
		TransferBuffer();
		
		for (Iterator<Entry<Pair<String, String>, Charge>> iterator = charges.entrySet().iterator(); iterator.hasNext();){
			Entry<Pair<String, String>, Charge> entry = iterator.next();
			if(entry.getValue().Update()) iterator.remove();
		}
	}
	
	//
	
	private Charge Get(String user, String chargeName) {
		return charges.get(new Pair<String, String>(user, chargeName));
	}
	private boolean BufferExist(String user, String chargeName) {
		return BufferExist(new Pair<String, String>(user, chargeName));
	}
	private boolean BufferExist(Pair<String, String> keys) {
		return chargesBuffer.containsKey(keys);
	}
	private void TransferBuffer() {
		for(Iterator<Entry<Pair<String, String>, Charge>> iterator = chargesBuffer.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<String, String>, Charge> entry = iterator.next();
			if(!Exist(entry.getKey())) {
				charges.put(entry.getKey(), entry.getValue());
				iterator.remove();
			}
		}
	}
	
	//
	
	public static String GetName() {
		return NAME;
	}
}
