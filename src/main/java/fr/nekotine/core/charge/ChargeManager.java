package fr.nekotine.core.charge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.module.PluginModule;

public class ChargeManager extends PluginModule{
	//Clé 1 = nom de ce qui utilise la charge
	//Clé 2 = nom de la charge
	private HashMap<String, HashMap<String, Charger>> charges = new HashMap<String, HashMap<String, Charger>>();
	
	public ChargeManager(JavaPlugin plugin) {
		super(plugin, "ChargeManager");
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
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if(charges.containsKey(e.getPlayer().getName())) charges.get(e.getPlayer().getName()).clear();
	}
	
}
