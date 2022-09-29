package fr.nekotine.core.map;

import java.util.LinkedList;
import java.util.List;

import dev.jorel.commandapi.arguments.Argument;

public class MapCommand {

	private List<Argument<?>> argumentStack;
	
	private List<MapCommandGraphNode> nodeStack;
	
	public MapCommand() {
		argumentStack = new LinkedList<>();
		nodeStack = new LinkedList<>();
	}
	
	public MapCommand(List<Argument<?>> arguments, List<MapCommandGraphNode> nodes) {
		argumentStack = new LinkedList<>(arguments);
		nodeStack = new LinkedList<>(nodes);
	}
	
	public List<Argument<?>> getArgumentList(){
		return argumentStack;
	}
	
	public List<MapCommandGraphNode> getNodeStack() {
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
