package fr.nekotine.core.pathfinding;

import java.util.Collection;

@FunctionalInterface
public interface PathEdgeGenerator {

	public Collection<PathEdge> generateEdgesFor(PathNode node, PathNetwork network);
	
}
