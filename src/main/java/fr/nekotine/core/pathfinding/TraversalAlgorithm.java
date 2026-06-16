package fr.nekotine.core.pathfinding;

import java.util.List;

@FunctionalInterface
public interface TraversalAlgorithm {

	public List<PathEdge> getPath(PathNetwork network, PathNode startNode, PathNode targetNode);
	
}
