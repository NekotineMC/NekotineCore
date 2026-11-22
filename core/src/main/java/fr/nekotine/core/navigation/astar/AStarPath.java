package fr.nekotine.core.navigation.astar;

import fr.nekotine.core.navigation.HintedPathResult;
import fr.nekotine.core.navigation.PathMove;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * An immutable path of {@link AStarPathFinder}: every getter returns fresh
 * {@link Location} copies; the next point index is always 0.
 */
final class AStarPath implements HintedPathResult {

	private final List<Location> points;

	private final List<PathMove> moves;

	private final boolean reached;

	AStarPath(World world, AStarSearch.Result result) {
		var points = new ArrayList<Location>(result.size());
		for (int i = 0; i < result.size(); i++) {
			points.add(new Location(world, result.xs()[i], result.ys()[i], result.zs()[i]));
		}
		this.points = List.copyOf(points);
		this.moves = List.of(result.moves());
		this.reached = result.reached();
	}

	@Override
	public List<Location> getPoints() {
		return points.stream().map(Location::clone).toList();
	}

	@Override
	public int getNextPointIndex() {
		return 0;
	}

	@Override
	public Location getNextPoint() {
		return points.getFirst().clone();
	}

	@Override
	public Location getFinalPoint() {
		return points.getLast().clone();
	}

	@Override
	public boolean canReachFinalPoint() {
		return reached;
	}

	@Override
	public PathMove getMove(int index) {
		return moves.get(index);
	}

	@Override
	public String toString() {
		return "AStarPath[reached=" + reached + ", points=" + points.size() + "]";
	}
}
