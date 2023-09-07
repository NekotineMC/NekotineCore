package fr.nekotine.core.util.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TypeHashMap implements TypeMap{

	Map<Object, Object> backing = new HashMap<>();
	
	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public boolean containsKey(Class<?> key) {
		return backing.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return backing.containsValue(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T remove(Class<T> key) {
		return (T)backing.remove(key);
	}

	@Override
	public void clear() {
		backing.clear();
	}

	@Override
	public Set<Object> keySet() {
		return backing.keySet();
	}

	@Override
	public Collection<Object> values() {
		return backing.values();
	}

	@Override
	public Set<Entry<Object,Object>> entrySet() {
		return backing.entrySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T put(T value) {
		return (T)backing.put(value.getClass(), value);
	}

	@Override
	public <T> T get(Class<T> type) {
		return type.cast(backing.get(type));
	}

	@Override
	public <T> T put(Class<T> type, T value) {
		return type.cast(backing.put(type, value));
	}

}
