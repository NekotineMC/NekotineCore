package fr.nekotine.core.util;

import java.util.HashMap;
import java.util.Map;

public class CollectionUtil {

	/**
	 * Trim une map en utilisant le constructeur d'une HashMap
	 * @param <A>
	 * @param <B>
	 * @param map
	 * @return
	 */
	public static <A,B> HashMap<A,B> trimHashMap(Map<A,B> map){
		return new HashMap<>(map); // Un peut nul
	}
	
}
