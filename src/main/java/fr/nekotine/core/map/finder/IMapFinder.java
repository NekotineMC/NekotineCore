package fr.nekotine.core.map.finder;

import java.util.List;

import fr.nekotine.core.map.MapHandle;

public interface IMapFinder {

	public List<MapHandle<?>> list();
	
	/**
	 * Ajoute la carte à l'index
	 */
	public <T> MapHandle<T> add(Class<T> mapConfigType, String name);
	
	/**
	 * Supprime la carte.
	 * @param identifier
	 * @return Si la carte a bien été supprimée, false si elle n'existait pas.
	 */
	public <T> boolean delete(MapHandle<T> handle);
	
	public <T> MapHandle<T> findByName(Class<T> mapType, String name);
	
}
