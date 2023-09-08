package fr.nekotine.core.util.lambda;

import java.util.function.Consumer;

public class LambdaUtil {

	/**
	 * Crée un Consumer à partir d'un runnable. La valeur d'entrée du consumer est ignorée.
	 * @param runnable
	 */
	public static <T> Consumer<T> toConsumer(Runnable runnable) {
		return (T t) -> runnable.run();
	}
	
}
