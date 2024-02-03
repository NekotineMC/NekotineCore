package fr.nekotine.core.map;

import fr.nekotine.core.map.annotation.GenerateCommandFor;
import fr.nekotine.core.map.element.MapBlockLocationElement;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.map.element.MapPositionElement;
import fr.nekotine.core.reflexion.annotation.GenericBiTyped;

public class MapTest {

	private MapPositionElement notComposing = new MapPositionElement();
	
	@GenerateCommandFor
	private MapTestComposite composite = new MapTestComposite();
	
	@GenerateCommandFor()
	private MapPositionElement poseUnnamed = new MapPositionElement();
	
	@GenerateCommandFor("blockPoseRenamed")
	private MapBlockLocationElement blockPoseToRename = new MapBlockLocationElement();
	
	@GenericBiTyped(MapBlockLocationElement.class)
	@GenerateCommandFor()
	private MapDictionaryElement<MapBlockLocationElement> poseList = new MapDictionaryElement<>();
	
	@GenericBiTyped(MapTestComposite.class)
	@GenerateCommandFor()
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
