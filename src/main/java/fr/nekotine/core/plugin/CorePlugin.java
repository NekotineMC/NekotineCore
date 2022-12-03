package fr.nekotine.core.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.game.GameModeModule;
import fr.nekotine.core.lobby.LobbyModule;
import fr.nekotine.core.module.ModuleManager;

public class CorePlugin extends JavaPlugin {
	
	public static String CORE_PLUGIN_NAME = "NekotineCore";
	
	public static @Nullable Plugin getCorePluginInstance() {
		return Bukkit.getPluginManager().getPlugin(CORE_PLUGIN_NAME);
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		ModuleManager.Load(this,
				GameModeModule.class,
				LobbyModule.class
				);
	}
	
}
