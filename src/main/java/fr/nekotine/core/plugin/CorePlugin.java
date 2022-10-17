package fr.nekotine.core.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class CorePlugin extends JavaPlugin {
	
	public static String CORE_PLUGIN_NAME = "NekotineCore";
	
	public static @Nullable Plugin getCorePluginInstance() {
		return Bukkit.getPluginManager().getPlugin(CORE_PLUGIN_NAME);
	}
	
}
