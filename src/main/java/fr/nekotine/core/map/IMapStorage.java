package fr.nekotine.core.map;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public interface IMapStorage {
	
	public <T> T get(Class<T> type, String name);
	
	public <T> Set<Entry<String,Object>> allOf(Class<T> type);
	
	public List<Object> all();
	
	public <T> void add(String name, T map);
	
	public <T> void remove(Class<T> type, String name);
	
	public <T> void clear(Class<T> type);
	
	public void clear();
	
}
