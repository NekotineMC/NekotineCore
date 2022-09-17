package fr.nekotine.core.map;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.component.MapComponent;

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
		//
		var baseCommand = new CommandAPICommand(mapTypeId.getId())
				.withArguments(mapArgument("mapName", mapTypeId));
		//TODO la suite
		list.addAll(generateForComponent(mapTypeId.getMapTypeClass(),baseCommand,null));
		//
		return list;
	}
	
	private static List<CommandAPICommand> generateForComponent(Class<? extends MapComponent> clazz, CommandAPICommand base, Field[] executorStack){
		var list = new LinkedList<CommandAPICommand>();
		//
		for (var componentField : clazz.getDeclaredFields()) {
			ComposingMap annotation = componentField.getAnnotation(ComposingMap.class);
			if (annotation != null) {
				
			}
		}
		//
		return list;
	}
	
}
