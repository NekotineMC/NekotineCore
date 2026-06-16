package fr.nekotine.core.pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class PathNetwork {

	private final ArrayList<PathNode> nodes = new ArrayList<PathNode>();
	
	private final ArrayList<PathEdge> edges = new ArrayList<PathEdge>();

	public PathNetwork() {
		
	}
	
	public PathNetwork(PathNetwork other) {
		for (var node : other.nodes) {
			this.nodes.add(node);
		}
		for (var arc : other.edges) {
			edges.add(new PathEdge(nodes.stream().filter(n -> arc.getFrom().equals(n)).findFirst().get(),nodes.stream().filter(n -> arc.getTo().equals(n)).findFirst().get(),1));
		}
	}
	
	public Collection<PathNode> getNodes() {
		return nodes;
	}
	
	public Collection<PathEdge> getEdges() {
		return edges;
	}
	
	public Collection<PathEdge> edgesFromNode(PathNode node){
		return edges.stream().filter(e -> e.getFrom().equals(node)).collect(Collectors.toList());
	}
	
	public Collection<PathEdge> edgesToNode(PathNode node){
		return edges.stream().filter(e -> e.getTo().equals(node)).collect(Collectors.toList());
	}
	
	public PathNode nearestNodeFrom(Location location) {
		if (nodes.isEmpty()) {
			return null;
		}
		nodes.sort((a,b) -> Double.compare(a.getLocation().distanceSquared(location), b.getLocation().distanceSquared(location)));
		return nodes.getFirst();
	}
	
	public PathNode nearestNodeFrom(Vector location) {
		if (nodes.isEmpty()) {
			return null;
		}
		nodes.sort((a,b) -> Double.compare(a.getLocation().toVector().distanceSquared(location), b.getLocation().toVector().distanceSquared(location)));
		return nodes.getFirst();
	}
	
	public void generateEdges(PathEdgeGenerator generator) {
		edges.clear();
		for (var node : nodes) {
			edges.addAll(generator.generateEdgesFor(node, this));
		}
	}
	
}
