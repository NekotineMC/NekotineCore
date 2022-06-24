package fr.nekotine.core.wrapper;

import java.util.HashMap;

public class AutoWrapper {

	private static HashMap<Object, HashMap<Class<?>, WrapperBase<? extends Object>>> store = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static <T extends WrapperBase<? extends Object>> T Get(Object source, Class<T> wrapperType){
		HashMap<Class<?>, WrapperBase<? extends Object>> map = store.get(source);
		if (map!=null) {
			return (T) map.get(wrapperType);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends WrapperBase<? extends Object>> T GetOrCreate(Object source, Class<T> wrapperType){
		HashMap<Class<?>, WrapperBase<? extends Object>> map = store.get(source);
		if (map!=null) {
			if (map.containsKey(wrapperType)) {
				return (T) map.get(wrapperType);
			}else {
				try {
					T wrapper = wrapperType.getConstructor().newInstance(source);
					map.put(wrapperType, (WrapperBase<? extends Object>)wrapper);
					return wrapper;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}
		return null;
	}
}