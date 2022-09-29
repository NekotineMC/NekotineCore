package fr.nekotine.core.map.graph;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import fr.nekotine.core.map.MapCommandGraphNode;
import fr.nekotine.core.map.MapModule;
import fr.nekotine.core.map.component.MapComponent;
import fr.nekotine.core.map.component.PlaceMapElement;
import fr.nekotine.core.module.ModuleManager;

public class MapPlaceGraphNode extends MapCommandGraphNode{

	public MapPlaceGraphNode(Field fromLastNode) {
		super(fromLastNode);
	}
	
	public static List<Argument<?>> getArguments(){
		var list = new LinkedList<Argument<?>>();
		list.add(new LocationArgument("place", LocationType.PRECISE_POSITION));
		return list;
	}
	
	@Override
	public MapComponent applyNode(MapComponent lastNode, LinkedList<Object> args) {
		try {
			var node = (PlaceMapElement)_fromLastNode.get(lastNode);
			node.setValue((Location)args.get(0));
		} catch (Exception e) {
			ModuleManager.GetModule(MapModule.class).logException(Level.SEVERE, "Une erreur est survenue lors du parcours du graph de commande lors de son execution", e);
		}
		return super.applyNode(lastNode, args);
	}
	
}
