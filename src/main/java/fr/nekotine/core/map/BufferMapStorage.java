package fr.nekotine.core.map;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BufferMapStorage implements IMapStorage{

	private Map<Class<?>,Map<String, Object>> mapBuffer = new HashMap<>();
	
	@Override
	public <T> T get(Class<T> type, String name) {
		if (mapBuffer.containsKey(type)) {
			return type.cast(mapBuffer.get(type).get(name));
		}
		throw new IllegalArgumentException("La carte n'existe pas");
	}

	@Override
	public <T> Set<Entry<String, Object>> allOf(Class<T> type) {
		return mapBuffer.get(type).entrySet();
	}

	@Override
	public <T> void add(String name, T map) {
		var type = map.getClass();
		if (!mapBuffer.containsKey(type)){
			mapBuffer.put(type, new HashMap<String, Object>());
		}
		mapBuffer.get(type).put(name, map);
	}

	@Override
	public <T> void remove(Class<T> type, String name) {
		if (mapBuffer.containsKey(type)) {
			mapBuffer.get(type).remove(name);
		}
	}

	@Override
	public <T> void clear(Class<T> type) {
		if (mapBuffer.containsKey(type)) {
			mapBuffer.get(type).clear();
		}
	}

	@Override
	public void clear() {
		mapBuffer.clear();
	}

	@Override
	public List<Object> all() {
		var list = new LinkedList<Object>();
		for (var item : mapBuffer.values()) {
			for (var map : item.values()) {
				list.add(map);
			}
		}
		return list;
	}
}
