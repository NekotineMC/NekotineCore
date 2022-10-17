package fr.nekotine.core.map.component;

import java.util.List;

import fr.nekotine.core.map.GameMap;

public final class MapComponentList<ComponentType extends MapComponent> extends MapComponent {
	
	private Class<ComponentType> _contentType;
	
	public MapComponentList(GameMap map, String name, Class<ComponentType> contentType) {
		super(map, name);
		_contentType = contentType;
	}
	
	public Class<ComponentType> getContentType(){
		return _contentType;
	}

	private List<ComponentType> _content;
	
	public List<ComponentType> getContentList(){
		return _content;
	}
	
}
