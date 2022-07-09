package fr.nekotine.core.charge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.arrache.TickEvent;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;

public class ChargeManager extends PluginModule{
	private static final String NAME = "ChargeManager";
	
	//Cl� 1 = nom de ce qui utilise la charge
	//Cl� 2 = nom de la charge
	private HashMap<String, HashMap<String, Charge>> charges = new HashMap<String, HashMap<String, Charge>>();
	
	//
	
	public ChargeManager(JavaPlugin plugin, ModuleManager manager) {
		super(plugin,"ChargeManager", manager);
	}
	
	//
	
	/**
	 * ! Le couple (user, chargeName) doit �tre unique !
	 * @param user L'utilisateur de la charge
	 * @param chargeName Le nom de la charge
	 * @param duration Dur�e en ms
	 * @param iCharge Interface
	 * @return True si la charge a �t� ajout�e
	 */
	public boolean AddCharge(String user, String chargeName, long duration, ICharge iCharge) {
		Charge charge = new Charge(user, chargeName, duration, iCharge);
		
		if(!charges.containsKey(user)) {
			HashMap<String, Charge> toPut = new HashMap<String, Charge>();
			toPut.put(chargeName, charge);
			charges.put(user, toPut);
			return true;
		}
		HashMap<String, Charge> toPut = charges.get(user);
		if(!toPut.containsKey(user)) {
			toPut.put(chargeName, charge);
			return true;
		}
		return false;
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
	
	//
	
	@EventHandler
	public void Tick(TickEvent e) {
		for (HashMap<String, Charge> maps : charges.values()){
			for (Iterator<Entry<String, Charge>> iterator = maps.entrySet().iterator(); iterator.hasNext();){
				Entry<String, Charge> entry = iterator.next();
				if(entry.getValue().Update()) iterator.remove();
			}
		}
	}
	
	//
	
	private Charge Get(String user, String chargeName) {
		return charges.get(user).get(chargeName);
	}
	private boolean Exist(String user, String chargeName) {
		return (charges.containsKey(user) && charges.get(user).containsKey(chargeName));
	}
	
	//
	
	public static String GetName() {
		return NAME;
	}
}
