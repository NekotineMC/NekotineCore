package fr.nekotine.core.map;

import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.annotation.MapDictionaryElementType;
import fr.nekotine.core.map.element.MapBlockPositionElement;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.map.element.MapPositionElement;

public class MapTest {

	private MapPositionElement notComposing = new MapPositionElement();
	
	@ComposingMap
	private MapTestComposite composite = new MapTestComposite();
	
	@ComposingMap()
	private MapPositionElement poseUnnamed = new MapPositionElement();
	
	@ComposingMap("blockPoseRenamed")
	private MapBlockPositionElement blockPoseToRename = new MapBlockPositionElement();
	
	@MapDictionaryElementType(MapBlockPositionElement.class)
	@ComposingMap()
	private MapDictionaryElement<MapBlockPositionElement> poseList = new MapDictionaryElement<>();
	
	@MapDictionaryElementType(MapTestComposite.class)
	@ComposingMap()
	private MapDictionaryElement<MapTestComposite> compositeList = new MapDictionaryElement<>();

	public MapPositionElement getNotComposing() {
		return notComposing;
	}

	public void setNotComposing(MapPositionElement notComposing) {
		this.notComposing = notComposing;
	}

	public MapTestComposite getComposite() {
		return composite;
	}

	public void setComposite(MapTestComposite composite) {
		this.composite = composite;
	}

	public MapPositionElement getPoseUnnamed() {
		return poseUnnamed;
	}

	public void setPoseUnnamed(MapPositionElement poseUnnamed) {
		this.poseUnnamed = poseUnnamed;
	}

	public MapBlockPositionElement getBlockPoseToRename() {
		return blockPoseToRename;
	}

	public void setBlockPoseToRename(MapBlockPositionElement blockPoseToRename) {
		this.blockPoseToRename = blockPoseToRename;
	}

	public MapDictionaryElement<MapBlockPositionElement> getPoseList() {
		return poseList;
	}

	public void setPoseList(MapDictionaryElement<MapBlockPositionElement> poseList) {
		this.poseList = poseList;
	}

	public MapDictionaryElement<MapTestComposite> getCompositeList() {
		return compositeList;
	}

	public void setCompositeList(MapDictionaryElement<MapTestComposite> compositeList) {
		this.compositeList = compositeList;
	}
	
}
