package fr.nekotine.core.pathfinding.astar.heuristic;

import fr.nekotine.core.pathfinding.PathNode;
import fr.nekotine.core.pathfinding.astar.AstarHeuristic;

public class ManhattanAstarHeuristic implements AstarHeuristic {

	@Override
	public double calculate(PathNode currentNode, PathNode targetNode) {
		var cur = currentNode.getLocation().toVector();
		var target = targetNode.getLocation().toVector();
		return Math.abs(target.getX() - cur.getX()) + Math.abs(target.getY() - cur.getY()) + Math.abs(target.getZ() - cur.getZ());
	}

}
