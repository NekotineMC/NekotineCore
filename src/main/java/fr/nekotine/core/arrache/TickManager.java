package fr.nekotine.core.arrache;

import org.bukkit.scheduler.BukkitRunnable;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "TickManager")
public class TickManager extends PluginModule{
	private BukkitRunnable ticker;
	
	private void tick() {
		new TickEvent().callEvent();
	}
	
	@Override
	protected void onEnable() {
		super.onEnable();
		ticker = (new BukkitRunnable() {
			public void run() {
				tick();
			}
		});
		ticker.runTaskTimer(getPlugin(), 0L, 1L);
	}

	@Override
	protected void onDisable() {
		ticker.cancel();
		super.onDisable();
	}
	
}
