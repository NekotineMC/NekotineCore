package fr.nekotine.core.eventguard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.ticking.TickingModule;
import fr.nekotine.core.ticking.event.TickElapsedEvent;
import fr.nekotine.core.util.EventUtil;

public class PlayerDoubleEventGuard extends PluginModule implements Listener{
	private HashMap<Player, Set<String>> calledEvent = new HashMap<Player, Set<String>>();
	public PlayerDoubleEventGuard() {
		Ioc.resolve(ModuleManager.class).tryLoad(TickingModule.class);
		EventUtil.register(this);
	}
	public boolean call(Player player, Event evt) {
		if(alreadyCalled(player, evt)) {
			return false;
		}
		if(!calledEvent.containsKey(player)) {
			calledEvent.put(player, new HashSet<String>());
		}
		calledEvent.get(player).add(evt.getEventName());
		return true;
	}
	public boolean alreadyCalled(Player player, Event evt) {
		return calledEvent.containsKey(player) && calledEvent.get(player).contains(evt.getEventName());
	}
	
	//

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent evt) {
		call(evt.getPlayer(), evt);
	}
	@EventHandler
	public void onTick(TickElapsedEvent evt) {
		calledEvent.clear();
	}
}
