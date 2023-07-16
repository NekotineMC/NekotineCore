package fr.nekotine.core.map.finder;

import java.util.List;

import fr.nekotine.core.map.MapMetadata;

public interface IMapFinder {

	public List<MapMetadata> list();
	
	/**
	 * Ajoute la carte à l'index
	 * @return Si la carte a été ajoutée, false si elle existait déja.
	 */
	public boolean add(MapMetadata identifier);
	
	/**
	 * Supprime la carte.
	 * @param identifier
	 * @return Si la carte a bien été supprimée, false si elle n'existait pas.
	 */
	public boolean delete(MapMetadata identifier);
	
	public MapMetadata findByName(Class<?> mapType, String name);
	
}
