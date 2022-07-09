package fr.nekotine.core.arrache;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.module.PluginModule;

public class TickManager extends PluginModule{
	private BukkitRunnable ticker;
	public TickManager(JavaPlugin plugin, ModuleManager manager) {
		super(plugin, "TickManager", manager);
		
		ticker = (new BukkitRunnable() {
			public void run() {
				tick();
			}
		});
	}
	
	private void tick() {
		new TickEvent().callEvent();
	}
	
	@Override
	protected void onEnable() {
		super.onEnable();
		ticker.runTaskTimer(getPlugin(), 0L, 1L);
	}

	@Override
	protected void onDisable() {
		ticker.cancel();
		super.onDisable();
	}
	
}
