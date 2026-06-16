package fr.nekotine.core.pathfinding;

public class PathEdge {

	private final PathNode from;
	
	private final PathNode to;

	private final double cost;
	
	public PathEdge(PathNode from, PathNode to, double cost) {
		this.from = from;
		this.to = to;
		this.cost = cost;
	}
	
	public PathEdge(PathNode from, PathNode to) {
		this.from = from;
		this.to = to;
		this.cost = 1;
	}
	
	public PathEdge(PathEdge other) {
		this.from = other.from;
		this.to = other.to;
		this.cost = other.cost;
	}
	
	public PathNode getFrom() {
		return from;
	}

	public PathNode getTo() {
		return to;
	}
	
	public double getCost() {
		return cost;
	}
	
}
