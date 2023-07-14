package fr.nekotine.core.map.finder;

import java.util.List;

import fr.nekotine.core.map.MapIdentifier;

public interface IMapFinder {

	public List<MapIdentifier> list();
	
	/**
	 * Ajoute la carte à l'index
	 * @return Si la carte a été ajoutée, false si elle existait déja.
	 */
	public boolean add(MapIdentifier identifier);
	
	/**
	 * Supprime la carte.
	 * @param identifier
	 * @return Si la carte a bien été supprimée, false si elle n'existait pas.
	 */
	public boolean delete(MapIdentifier identifier);
	
	public MapIdentifier findByName(String name);
	
}
