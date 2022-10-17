package fr.nekotine.core.map.component;

import java.io.Serializable;

import fr.nekotine.core.map.GameMap;

public abstract class MapComponent implements Serializable{

	private String _name;
	
	private GameMap _map;
	
	/**
	 * Un constructeur utilisé lors de la construction de l'arbre de commands, NE PAS SURCHARGER DANS LES CLASSES ENFANTS
	 * @param owner
	 * @param name
	 */
	public MapComponent(GameMap map, String name) {
		_name = name;
		_map = map;
	}
	
	public String getName() {
		return _name;
	}
	
	public GameMap getMap() {
		if (_map == null && GameMap.class.isAssignableFrom(getClass())) {
			_map = (GameMap) this;
		}
		return _map;
	}
}
