package fr.nekotine.core.map.graph;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import fr.nekotine.core.map.MapModule;
import fr.nekotine.core.map.component.MapComponent;
import fr.nekotine.core.map.component.MapComponentList;
import fr.nekotine.core.module.ModuleManager;

public final class MapComponentListGraphNode extends MapGraphNode {

	public MapComponentListGraphNode(Field fromLastNode) {
		super(fromLastNode);
	}
	
	public static List<Argument<?>> getArguments(){
		var list = new LinkedList<Argument<?>>();
		list.add(new LocationArgument("place", LocationType.PRECISE_POSITION));
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public MapComponent applyNode(MapComponent lastNode, LinkedList<Object> args) {
		try {
			MapComponentList<MapComponent> node = (MapComponentList<MapComponent>) lastNode;
			if (_fromLastNode != null) {
				node = (MapComponentList<MapComponent>)_fromLastNode.get(lastNode);
			}
			var componentName = (String)args.pollFirst();
		} catch (Exception e) {
			ModuleManager.GetModule(MapModule.class).logException(Level.SEVERE, "Une erreur est survenue lors du parcours du graph de commande lors de son execution", e);
		}
		return lastNode;
	}
	
}
