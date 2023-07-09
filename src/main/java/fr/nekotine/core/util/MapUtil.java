package fr.nekotine.core.util;

import java.util.Map;

public class MapUtil {

	/**
	 * Merge une Map dans une autre, écrasant au passage les valeurs déja présentes.
	 * @param <K>
	 * @param <V>
	 * @param destination
	 * @param toAdd
	 */
	public static <K,V> void merge(Map<K,V> destination, Map<K,V> toAdd) {
		for (var key : toAdd.keySet()) {
			destination.put(key, toAdd.get(key));
		}
	}
	
}
