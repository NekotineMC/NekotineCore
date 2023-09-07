package fr.nekotine.core.util.map;

import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Une Map spécifique, dont la clef est le type de la valeur.
 * @author XxGoldenbluexX
 *
 */
public interface TypeMap{
	
	public boolean containsKey(Class<?> key);
	
	public boolean containsValue(Object key);
	
	public int size();
	
	public boolean isEmpty();
	
	public Set<Object> keySet();
	
	public Collection<Object> values();
	
	public Set<Entry<Object,Object>> entrySet();
	
	public void clear();
	
	public <T> T remove(Class<T> key);
	
	public <T> T get(Class<T> type);
	
	public <T> T put(Class<T> type, T value);
	
	public <T> T put(T value);
	
}
