package fr.nekotine.core.map;

import fr.nekotine.core.map.annotation.GenerateCommandFor;
import fr.nekotine.core.map.element.MapBlockLocationElement;
import fr.nekotine.core.map.element.MapPositionElement;

public class MapTestComposite {

	@GenerateCommandFor
	private MapPositionElement position;
	
	@GenerateCommandFor("blockPositionRename")
	private MapBlockLocationElement blockPosition;

	public MapPositionElement getPosition() {
		return position;
	}

	public void setPosition(MapPositionElement position) {
		this.position = position;
	}

	public MapBlockLocationElement getBlockPosition() {
		return blockPosition;
	}

	public void setBlockPosition(MapBlockLocationElement blockPosition) {
		this.blockPosition = blockPosition;
	}
	
}
