package fr.nekotine.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
	
	/**
	 * Crée une LinkedList à partir d'une array
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static <T> LinkedList<T> linkedList(T[] array){
		var list = new LinkedList<T>();
		for (var item : array) {
			list.add(item);
		}
		return list;
	}
	
	/**
	 * Crée une LinkedList à partir d'une array
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static <T> ArrayList<T> arrayList(T[] array){
		var list = new ArrayList<T>();
		for (var item : array) {
			list.add(item);
		}
		return list;
	}
	
}
