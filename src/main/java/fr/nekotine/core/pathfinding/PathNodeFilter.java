package fr.nekotine.core.pathfinding;

@FunctionalInterface
public interface PathNodeFilter {

	public boolean isValid(PathNode node);
	
}
