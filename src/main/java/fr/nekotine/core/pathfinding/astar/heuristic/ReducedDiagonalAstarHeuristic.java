package fr.nekotine.core.pathfinding.astar.heuristic;

import fr.nekotine.core.pathfinding.PathNode;
import fr.nekotine.core.pathfinding.astar.AstarHeuristic;

public class ReducedDiagonalAstarHeuristic implements AstarHeuristic {

	private static final double SQRT2 = Math.sqrt(2);
	
	@Override
	public double calculate(PathNode currentNode, PathNode targetNode) {
		var cur = currentNode.getLocation().toVector();
		var target = targetNode.getLocation().toVector();
		var dx = Math.abs(target.getX() - cur.getX());
		var dy = Math.abs(target.getY() - cur.getY());
		var dz = Math.abs(target.getZ() - cur.getZ());
		
		var min = Math.min(dx, Math.min(dy, dz));
		var max = Math.max(dx, Math.max(dy, dz));
		var manhattan = dx + dy + dz;
		var mid = manhattan - min - max;
		return manhattan + (mid * SQRT2) - (mid * 2);
	}

}
