package fr.nekotine.core.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.ioc.Ioc;

public class AsyncUtil {

	/**
	 * Execute le runnable sur le prochain tick minecraft.
	 * 
	 * @param runnable
	 */
	public static BukkitTask runAsync(Runnable runnable) {
		return Bukkit.getScheduler().runTaskAsynchronously(Ioc.resolve(JavaPlugin.class), runnable);
	}
	
	/**
	 * Execute le runnable sur le prochain tick minecraft.
	 * 
	 * @param runnable
	 */
	public static BukkitTask runAsync(Runnable runnable, Consumer<Exception> onException) {
		return Bukkit.getScheduler().runTaskAsynchronously(Ioc.resolve(JavaPlugin.class), AsyncUtil.catchException(runnable, onException));
	}

	/**
	 * Execute le runnable sur le thread server au prochain tick
	 * 
	 * @param runnable
	 */
	public static BukkitTask runSync(Runnable runnable) {
		return Bukkit.getScheduler().runTask(Ioc.resolve(JavaPlugin.class), runnable);
	}
	
	/**
	 * Execute le runnable sur le thread server au prochain tick
	 * 
	 * @param runnable
	 */
	public static BukkitTask runSync(Runnable runnable, Consumer<Exception> onException) {
		return Bukkit.getScheduler().runTask(Ioc.resolve(JavaPlugin.class), AsyncUtil.catchException(runnable, onException));
	}
	
	public static <T> @NotNull Runnable thenAsync(@NotNull Supplier<T> producer, Consumer<T> consumer) {
		return () -> {
			final var result = producer.get();
			if (consumer != null) {
				runAsync(() -> consumer.accept(result));
			}
		};
	}
	
	public static @NotNull Runnable thenAsync(@NotNull Runnable first, Runnable then) {
		return () -> {
			first.run();
			runAsync(then);
		};
	}
	
	public static <T> @NotNull Runnable thenAsync(@NotNull Supplier<T> producer, Consumer<T> consumer, Consumer<Exception> onException) {
		return () -> {
			final var result = producer.get();
			if (consumer != null) {
				runAsync(() -> consumer.accept(result), onException);
			}
		};
	}
	
	public static @NotNull Runnable thenAsync(@NotNull Runnable first, Runnable then, Consumer<Exception> onException) {
		return () -> {
			first.run();
			runAsync(then, onException);
		};
	}
	
	public static <T> @NotNull Runnable thenSync(@NotNull Supplier<T> producer, Consumer<T> consumer) {
		return () -> {
			final var result = producer.get();
			if (consumer != null) {
				runSync(() -> consumer.accept(result));
			}
		};
	}
	
	public static @NotNull Runnable thenSync(@NotNull Runnable first, Runnable then) {
		return () -> {
			first.run();
			runSync(then);
		};
	}
	
	public static <T> @NotNull Runnable thenSync(@NotNull Supplier<T> producer, Consumer<T> consumer, Consumer<Exception> onException) {
		return () -> {
			final var result = producer.get();
			if (consumer != null) {
				runSync(() -> consumer.accept(result), onException);
			}
		};
	}
	
	public static <T> @NotNull Runnable thenSync(@NotNull Runnable first, Runnable then, Consumer<Exception> onException) {
		return () -> {
			first.run();
			runSync(then, onException);
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
	
	public static <T> Runnable then(Supplier<T> first, Consumer<T> then, Consumer<Exception> onException) {
		return catchException(() -> then.accept(first.get()), onException);
	}
	
	public static Runnable then(Runnable first, Runnable then, Consumer<Exception> onException) {
		return catchException(() -> {first.run();then.run();}, onException);
	}
	
	public static <T> Runnable then(Supplier<T> first, Runnable then, Consumer<Exception> onException) {
		return catchException(() -> {first.get();then.run();}, onException);
	}
	
	public static Runnable catchException(Runnable action, Consumer<Exception> onException) {
		return () -> {
			try {
				action.run();
			}catch(Exception e) {
				onException.accept(e);
			}
		};
	}
	
	public static AsyncPipeline pipe() {
		return new AsyncPipeline();
	}
	
	public static AsyncPipeline pipe(Consumer<Exception> exceptionCallback) {
		return new AsyncPipeline(exceptionCallback);
	}
	
	public static class AsyncPipeline{
		
		Runnable action = () -> {};
		
		Consumer<Exception> onException;
		
		private AsyncPipeline() {}
		
		private AsyncPipeline(Consumer<Exception> exceptionCallback) {
			onException = exceptionCallback;
		}
		
		public void run() {
			action.run();
		}
		
		public AsyncPipeline async(Runnable async) {
			if (onException != null) {
				action = AsyncUtil.thenAsync(action, async, onException);
			}else {
				action = AsyncUtil.thenAsync(action, async);
			}
			return this;
		}
		
		public AsyncPipeline sync(Runnable sync) {
			if (onException != null) {
				action = AsyncUtil.thenSync(action, sync, onException);
			}else {
				action = AsyncUtil.thenSync(action, sync);
			}
			return this;
		}
		
		public AsyncPipeline async(Runnable async, Consumer<Exception> exceptionCallback) {
			action = AsyncUtil.thenAsync(action, async, exceptionCallback);
			return this;
		}
		
		public AsyncPipeline sync(Runnable sync, Consumer<Exception> exceptionCallback) {
			action = AsyncUtil.thenAsync(action, sync, exceptionCallback);
			return this;
		}
		
		public AsyncPipeline then(Runnable sync) {
			if (onException != null) {
				action = AsyncUtil.then(action, sync, onException);
			}else {
				action = AsyncUtil.then(action, sync);
			}
			return this;
		}
		
		public AsyncPipeline then(Runnable async, Consumer<Exception> exceptionCallback) {
			action = AsyncUtil.then(action, async, exceptionCallback);
			return this;
		}
		
	}
	
}
