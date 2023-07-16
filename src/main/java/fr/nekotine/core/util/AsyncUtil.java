package fr.nekotine.core.util;

import org.bukkit.Bukkit;

import fr.nekotine.core.NekotineCore;

public class AsyncUtil {

	/**
	 * Execute le runnable sur le prochain tick minecraft.
	 * 
	 * @param runnable
	 */
	public void RunAsync(Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(NekotineCore.getAttachedPlugin(), runnable);
	}

	/**
	 * Execute le runnable sur le thread server au prochain tick
	 * 
	 * @param runnable
	 */
	public void RunSync(Runnable runnable) {
		Bukkit.getScheduler().runTask(NekotineCore.getAttachedPlugin(), runnable);
	}
	
}
