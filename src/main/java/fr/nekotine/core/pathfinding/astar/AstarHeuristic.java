package fr.nekotine.core.pathfinding.astar;

import fr.nekotine.core.pathfinding.PathNode;

@FunctionalInterface
public interface AstarHeuristic {

	public double calculate(PathNode currentNode, PathNode targetNode);
	
}
