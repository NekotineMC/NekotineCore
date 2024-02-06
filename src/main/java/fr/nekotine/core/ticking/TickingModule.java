package fr.nekotine.core.ticking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.ticking.event.TickElapsedEvent;

public class TickingModule implements IPluginModule{
	
	private Logger logger = new NekotineLogger(getClass());
	
	TickEventRunnable runningTask;
	
	Map<TickTimeStamp, Integer> stamps = new HashMap<>();
	
	public TickingModule() {
		runningTask = new TickEventRunnable(this);
		try {
			runningTask.runTaskTimer(Ioc.resolve(JavaPlugin.class), 0, 1);
		}catch(IllegalPluginAccessException e) {
			throw new IllegalStateException("Impossible de charger le TickingModule avant que le plugin soit activé (OnEnable)", e);
		}
	}

	@Override
	public void unload() {
		try {
			runningTask.cancel();
		}catch(Exception e) {
			logger.log(Level.WARNING, "Erreur lors de l'arret de l'horloge", e);
		}
	}
	
	private void Tick() {
		var reachedStamps = new HashSet<TickTimeStamp>();
		for (TickTimeStamp stamp : TickTimeStamp.values()) {
			if (stamps.compute(stamp, (s,v) -> v == null?0:++v) > stamp.getNumberOfTick()) {
				stamps.put(stamp, 0);
				reachedStamps.add(stamp);
			}
		}
		Event tickEvent = new TickElapsedEvent(reachedStamps);
		Ioc.resolve(JavaPlugin.class).getServer().getPluginManager().callEvent(tickEvent);
	}
	
	private class TickEventRunnable extends BukkitRunnable{
		
		private TickingModule module;
		
		private TickEventRunnable(TickingModule module) {
			this.module = module;
		}
		
		@Override
		public void run() {
			module.Tick();
		}
		
	}
	
}
