package fr.nekotine.core.charge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.module.PluginModule;

public class ChargeManager extends PluginModule{
	
	//Cl� 1 = nom de ce qui utilise la charge
	//Cl� 2 = nom de la charge
	private HashMap<String, HashMap<String, Charger>> charges = new HashMap<String, HashMap<String, Charger>>();
	
	public ChargeManager(JavaPlugin plugin) {
		super(plugin, "ChargeManager");
	}
	
	public boolean AddCharge(String user, String chargeName, long duration, ICharge iCharge) {
		Charger charger = new Charger(user, chargeName, duration, iCharge);
		if(!charges.containsKey(user)) {
			HashMap<String, Charger> toPut = new HashMap<String, Charger>();
			toPut.put(chargeName, charger);
			charges.put(user, toPut);
			return true;
		}
		HashMap<String, Charger> toPut = charges.get(user);
		if(!toPut.containsKey(user)) {
			toPut.put(chargeName, charger);
			return true;
		}
		return false;
	}
	
	public boolean SetCancelled(String user, String chargeName, boolean cancelled) {
		if(Exist(user, chargeName)) {
			Get(user, chargeName).setCancelled(cancelled);
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void Tick(/* inserer tick event ici */) {
		for (HashMap<String, Charger> maps : charges.values()){
			for (Iterator<Entry<String, Charger>> iterator = maps.entrySet().iterator(); iterator.hasNext();){
				Entry<String, Charger> entry = iterator.next();
				if(entry.getValue().Charged()) iterator.remove();
			}
		}
	}
	
	private boolean Exist(String user, String chargeName) {
		return (charges.containsKey(user) && charges.get(user).containsKey(chargeName));
	}
	private Charger Get(String user, String chargeName) {
		return charges.get(user).get(chargeName);
	}
}
