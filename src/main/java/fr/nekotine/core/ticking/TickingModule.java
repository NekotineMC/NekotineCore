package fr.nekotine.core.ticking;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.ticking.event.TickElapsedEvent;

public class TickingModule extends PluginModule{
	
	TickEventRunnable runningTask;
	
	Map<TickTimeStamp, Integer> stamps = new HashMap<>();
	
	public TickingModule() {
		runningTask = new TickEventRunnable(this);
		runningTask.runTaskTimer(NekotineCore.getAttachedPlugin(), 0, 1);
	}

	@Override
	protected void unload() {
		try {
			runningTask.cancel();
		}catch(Exception e) {
			LOGGER.log(Level.WARNING, "Erreur lors de l'arret de l'horloge", e);
		}
		super.unload();
	}
	
	private void Tick() {
		Map<TickTimeStamp, Boolean> stampStatus = new HashMap<>();
		for (TickTimeStamp stamp : TickTimeStamp.values()) {
			if (stamps.get(stamp)!=null && stamps.get(stamp) > stamp.getNumberOfTick()) {
				stamps.put(stamp, 0);
				stampStatus.put(stamp, true);
			}else {
				stampStatus.put(stamp, false);
			}
		}
		Event tickEvent = new TickElapsedEvent(stampStatus);
		NekotineCore.getAttachedPlugin().getServer().getPluginManager().callEvent(tickEvent);
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
