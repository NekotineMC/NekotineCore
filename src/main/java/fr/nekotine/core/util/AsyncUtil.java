package fr.nekotine.core.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.NekotineCore;

public class AsyncUtil {

	/**
	 * Execute le runnable sur le prochain tick minecraft.
	 * 
	 * @param runnable
	 */
	public static BukkitTask runAsync(Runnable runnable) {
		return Bukkit.getScheduler().runTaskAsynchronously(NekotineCore.getAttachedPlugin(), runnable);
	}

	/**
	 * Execute le runnable sur le thread server au prochain tick
	 * 
	 * @param runnable
	 */
	public static BukkitTask runSync(Runnable runnable) {
		return Bukkit.getScheduler().runTask(NekotineCore.getAttachedPlugin(), runnable);
	}
	
	public static <T> @NotNull Runnable thenSync(@NotNull Supplier<T> producer, Consumer<T> consumer) {
		return () -> {
			final var result = producer.get();
			if (consumer != null) {
				runSync(() -> consumer.accept(result));
			}
		};
	}
	
	public static <T> @NotNull Runnable thenSync(@NotNull Runnable first, Runnable then) {
		return () -> {
			first.run();
			runSync(then);
		};
	}
	
	public static <T> Runnable then(Supplier<T> first, Consumer<T> then) {
		return () -> then.accept(first.get());
	}
	
	public static Runnable then(Runnable first, Runnable then) {
		return () -> {first.run();then.run();};
	}
	
	public static <T> Runnable then(Supplier<T> first, Runnable then) {
		return () -> {first.get();then.run();};
	}
}
