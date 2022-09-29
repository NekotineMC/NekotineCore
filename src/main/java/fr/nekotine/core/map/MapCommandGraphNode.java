package fr.nekotine.core.map;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.logging.Level;

import fr.nekotine.core.map.component.MapComponent;
import fr.nekotine.core.module.ModuleManager;

public class MapCommandGraphNode {

	protected final Field _fromLastNode;
	
	public MapCommandGraphNode(Field fromLastNode) {
		_fromLastNode = fromLastNode;
	}
	
	public MapComponent applyNode(MapComponent lastNode, LinkedList<Object> args) {
		try {
			return (MapComponent) _fromLastNode.get(lastNode);
		} catch (Exception e) {
			ModuleManager.GetModule(MapModule.class).logException(Level.SEVERE, "Une erreur est survenue lors du parcours du graph de commande lors de son execution", e);
		}
		return null;
	}
	
}
