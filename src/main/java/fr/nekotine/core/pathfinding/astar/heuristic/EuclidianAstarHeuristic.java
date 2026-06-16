package fr.nekotine.core.pathfinding.astar.heuristic;

import fr.nekotine.core.pathfinding.PathNode;
import fr.nekotine.core.pathfinding.astar.AstarHeuristic;

public class EuclidianAstarHeuristic implements AstarHeuristic {

	@Override
	public double calculate(PathNode currentNode, PathNode targetNode) {
		return currentNode.getLocation().distance(targetNode.getLocation());
	}

}
