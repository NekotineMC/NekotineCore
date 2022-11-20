package fr.nekotine.core.map;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.component.MapBlockPlaceElement;
import fr.nekotine.core.map.component.MapComponent;
import fr.nekotine.core.map.component.MapComponentList;
import fr.nekotine.core.map.component.MapElement;
import fr.nekotine.core.map.component.MapPlaceElement;
import fr.nekotine.core.map.component.MapRectangleAreaElement;
import fr.nekotine.core.map.graph.MapBlockPlaceGraphNode;
import fr.nekotine.core.map.graph.MapComponentListGraphNode;
import fr.nekotine.core.map.graph.MapGraphNode;
import fr.nekotine.core.map.graph.MapPlaceGraphNode;
import fr.nekotine.core.map.graph.MapRectangleAreaGraphNode;
import fr.nekotine.core.module.ModuleManager;

public class MapCommandGenerator {
	
	protected static Argument<MapIdentifier> existingMapArgument(String nodeName, MapTypeIdentifier typeFilter){
		return new CustomArgument<MapIdentifier, String>(new StringArgument(nodeName), (info) -> {
			return MapModule.getMap(info.input());
		}).replaceSuggestions(ArgumentSuggestions.strings(info -> {
			return MapModule.getMapsForType(typeFilter).stream().map(MapIdentifier::name).toArray(String[]::new);
		}));
	}
	
	protected static Argument<MapIdentifier> existingMapArgument(String nodeName){
		return new CustomArgument<MapIdentifier, String>(new StringArgument(nodeName), (info) -> {
			return MapModule.getMap(info.input());
		}).replaceSuggestions(ArgumentSuggestions.strings(info -> {
			return MapModule.getAvailableMaps().stream().map(MapIdentifier::name).toArray(String[]::new);
		}));
	}
	
	protected static Argument<MapTypeIdentifier> registeredMapTypeArgument(String nodeName) {
		return new CustomArgument<MapTypeIdentifier, String>(new StringArgument(nodeName), (info) -> {
			return MapModule.getMapTypeById(info.input());
	}).replaceSuggestions(ArgumentSuggestions.strings(info -> {
		return MapModule.getAvailableMapTypes().stream().map(MapTypeIdentifier::getId).toArray(String[]::new);
	}));
	}
	
	public static List<CommandAPICommand> generateFor(MapTypeIdentifier mapTypeId){
		var list = new LinkedList<CommandAPICommand>();
		var mapComList = new LinkedList<MapCommand>();
		//
		mapComList.addAll(generateForComponent(mapTypeId.getMapTypeClass()));
		//
		for (var mapCom : mapComList) {
			list.add(
					new CommandAPICommand(mapTypeId.getId())
					.withArguments(existingMapArgument("mapName", mapTypeId))
					.withArguments(mapCom.getArgumentList())
					.executes((sender, args) -> {
						var argList = new LinkedList<Object>();
						argList.addAll(Arrays.asList(args));
						MapIdentifier mapId = (MapIdentifier) argList.pollFirst();
						var map = mapId.loadMap();
						MapComponent prevNode = map;
						for (var node : mapCom.getNodeStack()) {
							prevNode = node.applyNode(prevNode, argList);
						}
						map.saveAsync();
					})
					);
		}
		for (var item : list) {
			ModuleManager.GetModule(MapModule.class).log(Level.INFO, "COMMAND ITEM = " + item.getName());
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private static List<MapCommand> generateForComponent(Class<? extends MapComponent> clazz){
		var list = new LinkedList<MapCommand>();
		//
		for (var componentField : clazz.getDeclaredFields()) {
			ComposingMap annotation = componentField.getAnnotation(ComposingMap.class);
			if (annotation != null) {
				if (MapComponent.class.isAssignableFrom(componentField.getType())) {
					componentField.trySetAccessible();
					list.addAll(generateForNode(annotation.Name(), (Class<? extends MapComponent>) componentField.getType(), componentField));
				}
			}
		}
		//
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private static List<MapCommand> generateForNode(String nodeName, Class<? extends MapComponent> nodeType, Field gotoNode) {
		var list = new LinkedList<MapCommand>();
		LiteralArgument litArg = null;
		if (nodeName != null) {
			litArg = new LiteralArgument(nodeName);
		}
		try {
			List<MapCommand> fieldCommands = null;
			if (MapElement.class.isAssignableFrom(nodeType)) {
				fieldCommands = generateForElement(nodeType, gotoNode);
				for (var fieldCommand : fieldCommands) {
					if (litArg != null) {
						fieldCommand.getArgumentList().add(0, litArg);
					}
					list.add(fieldCommand);
				}
			}else {
				if (MapComponentList.class.isAssignableFrom(nodeType)) {
					var genericType = (Class<? extends MapComponent>)((ParameterizedType)nodeType.getGenericSuperclass()).getActualTypeArguments()[0];
					fieldCommands = generateForNode(null, genericType, null);
					for (var fieldCommand : fieldCommands) {
						if (litArg != null) {
							fieldCommand.getArgumentList().add(0, litArg);
						}
						fieldCommand.getNodeStack().add(0, new MapComponentListGraphNode(gotoNode));
						list.add(fieldCommand);
					}
				}else {
					fieldCommands = generateForComponent((Class<? extends MapComponent>) nodeType);
					for (var fieldCommand : fieldCommands) {
						if (litArg != null) {
							fieldCommand.getArgumentList().add(0, litArg);
						}
						fieldCommand.getNodeStack().add(0, new MapGraphNode(gotoNode));
						list.add(fieldCommand);
					}
				}
			}
		}catch(Exception e) {
			ModuleManager.GetModule(MapModule.class).logException(Level.WARNING,
					"Erreur lors de la génération de commande pour le champ " + nodeName + " de type " + nodeType.getName(), e);
		}
		return list;
	}
	
	private static List<MapCommand> generateForElement(Class<? extends MapComponent> clazz, Field transitionField){
		var list = new LinkedList<MapCommand>();
		if (MapPlaceElement.class.isAssignableFrom(clazz)) { // pas encore le type switch (voir update java 17)
			var com = new MapCommand();
			com.getArgumentList().addAll(MapPlaceGraphNode.getArguments());
			com.getNodeStack().add(new MapPlaceGraphNode(transitionField));
			list.add(com);
		}else if (MapBlockPlaceElement.class.isAssignableFrom(clazz)) {
			var com = new MapCommand();
			com.getArgumentList().addAll(MapBlockPlaceGraphNode.getArguments());
			com.getNodeStack().add(new MapBlockPlaceGraphNode(transitionField));
			list.add(com);
		}else if (MapRectangleAreaElement.class.isAssignableFrom(clazz)) {
			var com = new MapCommand();
			com.getArgumentList().addAll(MapRectangleAreaGraphNode.getArguments());
			com.getNodeStack().add(new MapRectangleAreaGraphNode(transitionField));
			list.add(com);
		}
		return list;
	}
	
}
