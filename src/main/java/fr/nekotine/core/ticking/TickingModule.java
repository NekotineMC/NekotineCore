package fr.nekotine.core.ticking;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import fr.nekotine.core.ticking.event.TickElapsedEvent;

@ModuleNameAnnotation(Name = "TickingModule")
public class TickingModule extends PluginModule{
	
	TickEventRunnable runningTask;
	
	Map<TickTimeStamp, Integer> stamps;
	
	@Override
	protected void onEnable() {
		super.onEnable();
		stamps = new HashMap<>();
		runningTask = new TickEventRunnable(this);
		runningTask.runTaskTimer(getPlugin(), 0, 1);
	}
	
	@Override
	protected void onDisable() {
		try {
			runningTask.cancel();
		}catch(Exception e) {
		}
		super.onDisable();
	}
	
	private void Tick() {
		Map<TickTimeStamp, Boolean> stampStatus = new HashMap<>();
		for (TickTimeStamp stamp : TickTimeStamp.values()) {
			if (stamps.get(stamp) > stamp.getNumberOfTick()) {
				stamps.put(stamp, 0);
				stampStatus.put(stamp, true);
			}else {
				stampStatus.put(stamp, false);
			}
		}
		Event tickEvent = new TickElapsedEvent(stampStatus);
		getPlugin().getServer().getPluginManager().callEvent(tickEvent);
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
