package fr.nekotine.core.map.finder;

import java.util.List;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import fr.nekotine.core.map.MapHandle;

public interface IMapFinder {

	public List<MapHandle<?>> list();
	
	/**
	 * Ajoute la carte à l'index
	 */
	public <T extends ConfigurationSerializable> MapHandle<T> add(Class<T> mapConfigType, String name);
	
	/**
	 * Supprime la carte.
	 * @param identifier
	 * @return Si la carte a bien été supprimée, false si elle n'existait pas.
	 */
	public <T extends ConfigurationSerializable> boolean delete(MapHandle<T> handle);
	
	public <T extends ConfigurationSerializable> MapHandle<T> findByName(Class<T> mapType, String name);
	
}
