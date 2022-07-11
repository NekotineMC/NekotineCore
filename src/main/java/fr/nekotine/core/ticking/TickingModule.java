package fr.nekotine.core.ticking;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "TickingModule")
public class TickingModule extends PluginModule{
	
	TickEventRunnable task;
	
	@Override
	protected void onEnable() {
		super.onEnable();
		new TickEventRunnable();
	}
	
	@Override
	protected void onDisable() {
		super.onDisable();
	}
	
	private class TickEventRunnable extends BukkitRunnable{
		
		private TickingModule module;
		
		private TickEventRunnable(TickingModule module) {
			this.module = module;
		}
		
		@Override
		public void run() {
			
		}
		
	}
	
}
