package fr.nekotine.core.map.component;

import java.io.Serializable;

import fr.nekotine.core.map.Map;

public abstract class MapComponent implements Serializable{

	private String _name;
	
	private Map _map;
	
	/**
	 * Un constructeur utilisé lors de la construction de l'arbre de commands, NE PAS SURCHARGER DANS LES CLASSES ENFANTS
	 * @param owner
	 * @param name
	 */
	public MapComponent(Map map, String name) {
		_name = name;
		_map = map;
	}
	
	public String getName() {
		return _name;
	}
	
	public Map getMap() {
		if (_map == null && Map.class.isAssignableFrom(getClass())) {
			_map = (Map) this;
		}
		return _map;
	}
}
