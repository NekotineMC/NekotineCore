package fr.nekotine.core.ticking;

import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import fr.nekotine.core.ticking.event.TickElapsedEvent;

@ModuleNameAnnotation(Name = "TickingModule")
public class TickingModule extends PluginModule{
	
	TickEventRunnable runningTask;
	
	@Override
	protected void onEnable() {
		super.onEnable();
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
		Event tickEvent = new TickElapsedEvent();
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
