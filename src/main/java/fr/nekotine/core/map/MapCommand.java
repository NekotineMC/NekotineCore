package fr.nekotine.core.map;

import java.util.LinkedList;
import java.util.List;

import dev.jorel.commandapi.arguments.Argument;
import fr.nekotine.core.map.graph.MapGraphNode;

public class MapCommand {

	private List<Argument<?>> argumentStack;
	
	private List<MapGraphNode> nodeStack;
	
	public MapCommand() {
		argumentStack = new LinkedList<>();
		nodeStack = new LinkedList<>();
	}
	
	public MapCommand(List<Argument<?>> arguments, List<MapGraphNode> nodes) {
		argumentStack = new LinkedList<>(arguments);
		nodeStack = new LinkedList<>(nodes);
	}
	
	public List<Argument<?>> getArgumentList(){
		return argumentStack;
	}
	
	public List<MapGraphNode> getNodeStack() {
		return nodeStack;
	}
	
	@Override
	protected Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
}
