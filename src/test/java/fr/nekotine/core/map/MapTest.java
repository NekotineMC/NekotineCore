package fr.nekotine.core.map;

import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.annotation.MapElementTyped;
import fr.nekotine.core.map.element.MapBlockLocationElement;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.map.element.MapPositionElement;

public class MapTest {

	private MapPositionElement notComposing = new MapPositionElement();
	
	@ComposingMap
	private MapTestComposite composite = new MapTestComposite();
	
	@ComposingMap()
	private MapPositionElement poseUnnamed = new MapPositionElement();
	
	@ComposingMap("blockPoseRenamed")
	private MapBlockLocationElement blockPoseToRename = new MapBlockLocationElement();
	
	@MapElementTyped(MapBlockLocationElement.class)
	@ComposingMap()
	private MapDictionaryElement<MapBlockLocationElement> poseList = new MapDictionaryElement<>();
	
	@MapElementTyped(MapTestComposite.class)
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

	public MapBlockLocationElement getBlockPoseToRename() {
		return blockPoseToRename;
	}

	public void setBlockPoseToRename(MapBlockLocationElement blockPoseToRename) {
		this.blockPoseToRename = blockPoseToRename;
	}

	public MapDictionaryElement<MapBlockLocationElement> getPoseList() {
		return poseList;
	}

	public void setPoseList(MapDictionaryElement<MapBlockLocationElement> poseList) {
		this.poseList = poseList;
	}

	public MapDictionaryElement<MapTestComposite> getCompositeList() {
		return compositeList;
	}

	public void setCompositeList(MapDictionaryElement<MapTestComposite> compositeList) {
		this.compositeList = compositeList;
	}
	
}
