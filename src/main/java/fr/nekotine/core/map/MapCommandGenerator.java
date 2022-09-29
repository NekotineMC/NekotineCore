package fr.nekotine.core.map;

import java.lang.reflect.Field;
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
import fr.nekotine.core.map.component.MapComponent;
import fr.nekotine.core.map.component.MapElement;
import fr.nekotine.core.map.component.PlaceMapElement;
import fr.nekotine.core.map.graph.MapPlaceGraphNode;
import fr.nekotine.core.module.ModuleManager;

public class MapCommandGenerator {
	
	private static Argument<MapIdentifier> mapArgument(String nodeName, MapTypeIdentifier typeFilter){
		return new CustomArgument<MapIdentifier, String>(new StringArgument(nodeName), (info) -> {
			return MapModule.getMap(info.input());
		}).replaceSuggestions(ArgumentSuggestions.strings(info -> {
			return MapModule.getMapsForType(typeFilter).stream().map(MapIdentifier::name).toArray(String[]::new);
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
					.withArguments(mapArgument("mapName", mapTypeId))
					.withArguments(mapCom.getArgumentList())
					.executes((sender, args) -> {
						var argList = new LinkedList<Object>();
						argList.addAll(Arrays.asList(args));
						MapIdentifier mapId = (MapIdentifier) argList.pollFirst();
						var map = mapId.type().generateTypedMap(mapId);
						map.load();
						MapComponent prevNode = map;
						for (var node : mapCom.getNodeStack()) {
							prevNode = node.applyNode(prevNode, argList);
						}
						map.save();
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
				System.out.println("Valid component field = " + annotation.Name());
				var litArg = new LiteralArgument(annotation.Name());
				try {
					componentField.setAccessible(true);
					if (MapComponent.class.isAssignableFrom(componentField.getType())) {
						System.out.println("phase 1");
						List<MapCommand> fieldCommands = null;
						if (MapElement.class.isAssignableFrom(componentField.getType())) {
							System.out.println("phase 2");
							fieldCommands = generateForElement((Class<? extends MapComponent>) componentField.getType(), componentField);
							for (var fieldCommand : fieldCommands) {
								System.out.println("phase 5 - oui");
								fieldCommand.getArgumentList().add(0, litArg);
								list.add(fieldCommand);
							}
						}else {
							fieldCommands = generateForComponent((Class<? extends MapComponent>) componentField.getType());
							for (var fieldCommand : fieldCommands) {
								System.out.println("phase 5 - oui");
								fieldCommand.getArgumentList().add(0, litArg);
								fieldCommand.getNodeStack().add(0, new MapCommandGraphNode(componentField));
								list.add(fieldCommand);
							}
						}
					}
				}catch(Exception e) {
					System.out.println("wtf");
					ModuleManager.GetModule(MapModule.class).logException(Level.WARNING,
							"Erreur lors de la génération de commande pour le champ" + annotation.Name() + "de la classe " + clazz.getName(), e);
				}
			}
		}
		//
		return list;
	}
	
	private static List<MapCommand> generateForElement(Class<? extends MapComponent> clazz, Field transitionField){
		var list = new LinkedList<MapCommand>();
		if (PlaceMapElement.class.isAssignableFrom(clazz)) {
			System.out.println("phase 4");
			var com = new MapCommand();
			com.getArgumentList().addAll(MapPlaceGraphNode.getArguments());
			com.getNodeStack().add(new MapPlaceGraphNode(transitionField));
			list.add(com);
		}
		return list;
	}
	
}
