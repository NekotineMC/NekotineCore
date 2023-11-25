package fr.nekotine.core.charge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.comphenix.protocol.wrappers.Pair;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.ticking.TickingModule;
import fr.nekotine.core.ticking.event.TickElapsedEvent;
import fr.nekotine.core.util.EventUtil;

public class ChargeModule extends PluginModule implements Listener{
	private static final String NAME = "ChargeModule";
	
	public ChargeModule() {
		EventUtil.register(this);
		Ioc.resolve(ModuleManager.class).tryLoad(TickingModule.class);
	}
	
	@Override
	protected void unload() {
		EventUtil.unregister(this);
		super.unload();
	}
	
	private final HashMap<Pair<String, String>, Charge> charges = new HashMap<Pair<String, String>, Charge>();
	private final HashMap<Pair<String, String>, Charge> chargesBuffer = new HashMap<Pair<String, String>, Charge>();
	
	//
	
	/**
	 * LE COUPLE (user, chargeName) DOIT ETRE UNIQUE
	 * @param user Nom de l'utilisateur de la charge
	 * @param chargeName Nom de la charge
	 * @param duration Durée de la charge en ms
	 * @param displayOnExpBar Si la charge doit s'afficher dans la barre d'expérience
	 * @param withAudio Si la charge doit faire du bruit au joueur
	 * @param audioBipNumber Nombre de bruits joués (sans compter celui de début & celui de fin)
	 * @param iCharge
	 * @return True si la charge a été ajoutée
	 */
	public boolean AddCharge(String user, String chargeName, long duration, boolean displayOnExpBar, boolean withAudio, long audioBipNumber, ICharge iCharge) {
		if(BufferExist(user, chargeName)) return false;
		
		Charge charge = new Charge(user, chargeName, duration, displayOnExpBar, withAudio, audioBipNumber, iCharge);
		chargesBuffer.put(new Pair<String, String>(user, chargeName), charge);
		return true;
	}
	
	//
	
	/**
	 * Annule la charge
	 * @param user
	 * @param chargeName
	 * @param cancelled
	 * @return True si la charge a été annulée
	 */
	public boolean SetCancelled(String user, String chargeName, boolean cancelled) {
		if(Exist(user, chargeName)) {
			Get(user, chargeName).SetCancelled(cancelled);
			return true;
		}
		return false;
	}
	/**
	 * Met en pause la charge
	 * @param user
	 * @param chargeName
	 * @param paused
	 * @return True si la charge a été mise en pause
	 */
	public boolean SetPaused(String user, String chargeName, boolean paused) {
		if(Exist(user, chargeName)) {
			Get(user, chargeName).SetPaused(paused);
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @param user
	 * @param chargeName
	 * @return Le temps restant de la charge en ms (-1 si la charge n'existe pas)
	 */
	public long GetTimeLeft(String user, String chargeName) {
		if(Exist(user, chargeName)) {
			return Get(user, chargeName).GetTimeLeft();
		}
		return -1;
	}
	/**
	 * 
	 * @param user
	 * @param chargeName
	 * @return True si la charge existe
	 */
	public boolean Exist(String user, String chargeName) {
		return Exist(new Pair<String, String>(user, chargeName));
	}
	/**
	 * 
	 * @param keys
	 * @return True si la charge existe
	 */
	public boolean Exist(Pair<String, String> keys) {
		return MainExist(keys) ||  BufferExist(keys);
	}
	
	//
	
	@EventHandler
	public void Tick(TickElapsedEvent e) {
		TransferBuffer();
		for (Iterator<Entry<Pair<String, String>, Charge>> iterator = charges.entrySet().iterator(); iterator.hasNext();){
			Entry<Pair<String, String>, Charge> entry = iterator.next();
			if(entry.getValue().Update()) iterator.remove();
		}
	}
	
	//
	
	private Charge Get(String user, String chargeName) {
		if(MainExist(user, chargeName)) return charges.get(new Pair<String, String>(user, chargeName));
		 return chargesBuffer.get(new Pair<String, String>(user, chargeName));
	}
	private boolean BufferExist(String user, String chargeName) {
		return BufferExist(new Pair<String, String>(user, chargeName));
	}
	private boolean BufferExist(Pair<String, String> keys) {
		return chargesBuffer.containsKey(keys);
	}
	private boolean MainExist(String user, String chargeName) {
		return MainExist(new Pair<String, String>(user, chargeName));
	}
	private boolean MainExist(Pair<String, String> keys) {
		return charges.containsKey(keys);
	}
	private void TransferBuffer() {
		for(Iterator<Entry<Pair<String, String>, Charge>> iterator = chargesBuffer.entrySet().iterator() ; iterator.hasNext() ; ) {
			Entry<Pair<String, String>, Charge> entry = iterator.next();
			if(!MainExist(entry.getKey())) charges.put(entry.getKey(), entry.getValue());
		}
		chargesBuffer.clear();
	}
	
	//
	
	public static String GetName() {
		return NAME;
	}
}
