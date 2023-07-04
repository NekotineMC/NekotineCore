package fr.nekotine.core.map.element;

import java.util.HashMap;
import java.util.Map;

/**
 * Une Map d'éléments. Les champs de ce type étant sujet à la génération de carte doivent être annoté avec l'annotation MapDictionaryElementType
 * pour contrer le type eerasure et ainsi permettre la génération en runtime.
 * @author XxGoldenbluexX
 *
 */
public class MapDictionaryElement<T> {

	private Map<String, T> backingMap = new HashMap<>();
	
	public MapDictionaryElement() {}
	
	public Map<String, T> backingMap(){
		return backingMap;
	}
	
}
