package fr.nekotine.core.pathfinding.astar;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.pathfinding.PathEdge;
import fr.nekotine.core.pathfinding.PathNetwork;
import fr.nekotine.core.pathfinding.PathNode;
import fr.nekotine.core.pathfinding.TraversalAlgorithm;
import fr.nekotine.core.pathfinding.astar.heuristic.EuclidianAstarHeuristic;
import fr.nekotine.core.util.AssertUtil;

public class AstarTraversal implements TraversalAlgorithm {

	
	private LinkedList<PathNodeInfo> toCheck = new LinkedList<PathNodeInfo>();
	
	private HashMap<PathNode, PathNodeInfo> nodeList = new HashMap<PathNode, PathNodeInfo>();
	
	private Collection<Entity> lastDisplay = new LinkedList<Entity>();
	
	private AstarHeuristic heuristic;
	
	public AstarTraversal(AstarHeuristic heuristic) {
		this.heuristic = heuristic;
	}
	
	public AstarTraversal() {
		this.heuristic = new EuclidianAstarHeuristic();
	}
	
	public List<PathEdge> getPathFirst(PathNetwork network, PathNode startNode, PathNode targetNode) {
		
		nodeList.put(startNode, new PathNodeInfo(startNode, null, 0));
		toCheck.add(nodeList.get(startNode));
		
		for (var node : network.getNodes()) {
			var world = Ioc.resolve(World.class);
			world.spawnEntity(node.getLocation(), EntityType.BLOCK_DISPLAY, SpawnReason.CUSTOM, e -> {
				e.setPersistent(false);
				if (e instanceof BlockDisplay dis) {
					var trans = dis.getTransformation();
					trans.getScale().mul(0.5f);
					dis.setTransformation(trans);
					dis.setBlock(Bukkit.createBlockData(Material.SPONGE));
				}
			});
		}
		
		return List.of(); // No path
		}
	
	@Override
	public List<PathEdge> getPath(PathNetwork network, PathNode startNode, PathNode targetNode) {
		AssertUtil.nonNull(startNode, "La position de départ ne peut pas être null");
		AssertUtil.nonNull(targetNode, "La position d'arrivée ne peut pas être null");
		if (startNode == targetNode) {
			return null; // no need to path
		}
		
		// Initial setup
		nodeList.clear();
		toCheck.clear();
		nodeList.put(startNode, new PathNodeInfo(startNode, null, 0));
		toCheck.add(nodeList.get(startNode));
		
		while (!toCheck.isEmpty()) {
			
			toCheck.sort((a, b) -> Double.compare(a.cumuledCost + heuristic.calculate(a.node, targetNode), b.cumuledCost + heuristic.calculate(b.node, targetNode)));
			var current = toCheck.removeFirst();
			
			// PATH FOUND
			if (current.node.equals(targetNode)) {
				var pathList = new LinkedList<PathEdge>();
				var backTracked = current.node;
				System.out.println("START IS="+startNode.getLocation().toVector());
				do {
					System.out.println("SIZE BEFORE ADDITION="+pathList.size());
					System.out.println("ADDED IS="+backTracked.getLocation().toVector());
					var arc = nodeList.get(backTracked).commingFrom;
					if (arc == null) {
						System.out.println("ARC IS NULL FOR NODE "+backTracked.getLocation().toVector());
						System.out.println("BACKTRACKED IS START: "+backTracked.equals(startNode)+ " ("+startNode.getLocation().toVector());
						System.out.println("BACKTRACKED IS TARGET: "+backTracked.equals(targetNode)+ " ("+targetNode.getLocation().toVector());
					}
					System.out.println("ARC LENGHT IS="+arc.getCost());
					pathList.addFirst(arc);
					backTracked = arc.getFrom();
				} while (!backTracked.equals(startNode));
				return pathList;
			}
			
			//var neighborEdges = network.edgesFromNode(current.node); // Filter/Edit using JPS
			edgeloop:for (var edge : network.getEdges()) {
				if (!edge.getFrom().equals(current.node)) {
					continue edgeloop;
				}
				var neighNode = edge.getTo();
				var costUsingThisEdge = current.cumuledCost + edge.getCost() + neighNode.getCost();
				var savedNeigh = nodeList.get(neighNode);
				
				if (savedNeigh == null || costUsingThisEdge < savedNeigh.cumuledCost) {
					var newBestValue = new PathNodeInfo(neighNode, edge, costUsingThisEdge);
					nodeList.put(neighNode, newBestValue);
					if (!toCheck.contains(newBestValue)) {
						toCheck.add(newBestValue);
					}
				}
			}
		}
		
		
		return null; // No path
	}
	
	public List<PathEdge> getPathStep(PathNetwork network, PathNode startNode, PathNode targetNode) {
		
		//network.edgesFromNode(startNode).stream().map(e -> new PathNodeInfo(e.getTo(), e, e.getCost()+e.getTo().getCost())).distinct().forEach(n -> toCheck.add(n));
		
		if (!toCheck.isEmpty()) {
			var world = Ioc.resolve(World.class);
			for (var dis : lastDisplay) {
				dis.remove();
			}
			
			for (var tc : toCheck) {
				lastDisplay.add(world.spawnEntity(tc.node.getLocation().clone().add(-0.2,0.0,-0.2), EntityType.BLOCK_DISPLAY, SpawnReason.CUSTOM, e -> {
					e.setPersistent(false);
					if (e instanceof BlockDisplay dis) {
						var trans = dis.getTransformation();
						trans.getScale().mul(0.3f);
						dis.setTransformation(trans);
						dis.setBlock(Bukkit.createBlockData(Material.LIGHT_BLUE_STAINED_GLASS));
					}
				}));
			}
			
			toCheck.sort((a, b) -> Double.compare(a.cumuledCost + heuristic.calculate(a.node, targetNode), b.cumuledCost + heuristic.calculate(b.node, targetNode)));
			var current = toCheck.removeFirst();
			if (current.node.equals(targetNode)) {
				var pathList = new LinkedList<PathEdge>();
				var backTracked = current.node;
				while (!backTracked.equals(startNode)) {
					var arc = nodeList.get(backTracked).commingFrom;
					pathList.addLast(arc);
					backTracked = arc.getFrom();
				}
				return pathList;
			}
			lastDisplay.add(world.spawnEntity(current.node.getLocation().clone().add(0, 0.2, 0), EntityType.BLOCK_DISPLAY, SpawnReason.CUSTOM, e -> {
				e.setPersistent(false);
				if (e instanceof BlockDisplay dis) {
					var trans = dis.getTransformation();
					trans.getScale().mul(0.5f);
					dis.setTransformation(trans);
					dis.setBlock(Bukkit.createBlockData(Material.DIAMOND_BLOCK));
				}
			}));
			
			world.spawnEntity(current.node.getLocation(), EntityType.BLOCK_DISPLAY, SpawnReason.CUSTOM, e -> {
				e.setPersistent(false);
				if (e instanceof BlockDisplay dis) {
					var trans = dis.getTransformation();
					trans.getScale().mul(0.5f);
					dis.setTransformation(trans);
					dis.setBlock(Bukkit.createBlockData(Material.RED_WOOL));
				}
			});
			System.out.println("CUMULED="+current.cumuledCost+" loc="+current.node.getLocation().toVector());
			System.out.println("ToCheck="+toCheck.size()+" nodeList="+nodeList.size());
			current.node.getLocation().getWorld().spawnParticle(Particle.CRIT, current.node.getLocation(), 1);
			
			var neighborEdges = network.edgesFromNode(current.node); // Filter/Edit using JPS
			System.out.println("nbNeighbor = "+neighborEdges.size());
			for (var edge : neighborEdges) {
				var neighNode = edge.getTo();
				var costUsingThisEdge = current.cumuledCost + edge.getCost() + neighNode.getCost();
				var savedNeigh = nodeList.get(neighNode);
				
				if (savedNeigh == null || costUsingThisEdge < savedNeigh.cumuledCost - 0.5) {
					var newBestValue = new PathNodeInfo(neighNode, edge, costUsingThisEdge);
					nodeList.put(current.node, newBestValue);
					
					lastDisplay.add(world.spawnEntity(neighNode.getLocation().clone().add(0, 0.2, 0), EntityType.BLOCK_DISPLAY, SpawnReason.CUSTOM, e -> {
						e.setPersistent(false);
						if (e instanceof BlockDisplay dis) {
							var trans = dis.getTransformation();
							trans.getScale().mul(0.5f);
							dis.setTransformation(trans);
							dis.setBlock(Bukkit.createBlockData(Material.EMERALD_BLOCK));
						}
					}));
					if (!toCheck.contains(newBestValue)) {
						toCheck.add(newBestValue);
					}
				}else {
					lastDisplay.add(world.spawnEntity(neighNode.getLocation().clone().add(0, 0.2, 0), EntityType.BLOCK_DISPLAY, SpawnReason.CUSTOM, e -> {
						e.setPersistent(false);
						if (e instanceof BlockDisplay dis) {
							var trans = dis.getTransformation();
							trans.getScale().mul(0.5f);
							dis.setTransformation(trans);
							dis.setBlock(Bukkit.createBlockData(Material.REDSTONE_BLOCK));
						}
					}));
				}
			}
		}
		
		return List.of(); // No path
	}

	private static class PathNodeInfo{
		
		private final double cumuledCost;
		
		private final PathNode node;
		
		private final PathEdge commingFrom;
		
		private PathNodeInfo(PathNode node, PathEdge commingFrom, double cumuledCost) {
			this.node = node;
			this.commingFrom = commingFrom;
			this.cumuledCost = cumuledCost;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PathNodeInfo info) {
				return node.equals(info.node);
			}
			return node.equals(obj);
		}
		
		@Override
		public int hashCode() {
			return node.hashCode();
		}
		
	}
	
}
