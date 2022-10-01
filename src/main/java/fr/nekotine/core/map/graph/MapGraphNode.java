package fr.nekotine.core.map.graph;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.logging.Level;

import fr.nekotine.core.map.MapModule;
import fr.nekotine.core.map.component.MapComponent;
import fr.nekotine.core.module.ModuleManager;

public class MapGraphNode {

	protected final Field _fromLastNode;
	
	public MapGraphNode(Field fromLastNode) {
		_fromLastNode = fromLastNode;
	}
	
	public MapComponent applyNode(MapComponent lastNode, LinkedList<Object> args) {
		try {
			if (_fromLastNode != null) {
				return (MapComponent) _fromLastNode.get(lastNode);
			}
		} catch (Exception e) {
			ModuleManager.GetModule(MapModule.class).logException(Level.SEVERE, "Une erreur est survenue lors du parcours du graph de commande lors de son execution", e);
		}
		return lastNode;
	}
	
}
