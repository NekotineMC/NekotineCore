package fr.nekotine.core.map;

import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.element.MapBlockPositionElement;
import fr.nekotine.core.map.element.MapPositionElement;

public class MapTestComposite {

	@ComposingMap
	private MapPositionElement position;
	
	@ComposingMap("blockPositionRename")
	private MapBlockPositionElement blockPosition;

	public MapPositionElement getPosition() {
		return position;
	}

	public void setPosition(MapPositionElement position) {
		this.position = position;
	}

	public MapBlockPositionElement getBlockPosition() {
		return blockPosition;
	}

	public void setBlockPosition(MapBlockPositionElement blockPosition) {
		this.blockPosition = blockPosition;
	}
	
}
