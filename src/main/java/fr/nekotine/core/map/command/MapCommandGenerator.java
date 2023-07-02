package fr.nekotine.core.map.command;

import java.util.logging.Level;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.executors.ExecutorType;
import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.map.BufferMapStorage;
import fr.nekotine.core.map.IMapStorage;
import fr.nekotine.core.map.element.MapBlockPositionElement;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.map.element.MapPositionElement;
import fr.nekotine.core.map.generator.BlockPositionCommandGenerator;
import fr.nekotine.core.map.generator.DefaultMapElementCommandGenerator;
import fr.nekotine.core.map.generator.DictionaryCommandGenerator;
import fr.nekotine.core.map.generator.PositionCommandGenerator;
import fr.nekotine.core.util.AssertUtil;

public class MapCommandGenerator {
	
	private static boolean needToAddDefaultResolvers = true;
	
	@SafeVarargs
	public static void generateFor(Class<?> ...mapTypes) {
		try {
			if (needToAddDefaultResolvers) {
				setupDefaultResolver();
			}
			var mapCommand = makeMapCommand();
			var editCommand = new CommandAPICommand("edit");
			editCommand.executes((CommandExecutor)(sender, args) -> sender.sendMessage("Usage: /map edit <mapType> <mapName>"), ExecutorType.ALL);
			for (var mapType : mapTypes) {
				try {
					
					var mapTypeName = mapType.getSimpleName();
					var mapTypeArgument = makeMapArgument(mapType);
					var generatorResolverOptional = NekotineCore.IOC.tryResolve(IMapElementCommandGeneratorResolver.class);
					var generatorResolver = AssertUtil.nonNull(generatorResolverOptional,
							"Veillez à bien définir un résolveur de générateur de commande ou à initialiser celui par défaut avec MapCommandGenerator.setupDefaultResolver()");
					var generator = generatorResolver.resolve(mapType);
					for (var branch : generator.generateFor(mapType)) {
						var command = new CommandAPICommand(mapTypeName);
						command.withArguments(mapTypeArgument);
						command.withArguments(branch.arguments());
						CommandExecutor executor = (sender, args)->{
							branch.consumer().accept(args.get(0)/*map*/, sender, args);
						};
						command.executes(executor, ExecutorType.ALL);
						editCommand.withSubcommand(command);
					}
					NekotineCore.LOGGER.info("[MapCommandGenerator] Commandes générées pour le type de carte "+mapTypeName);
				}catch(Exception e) {
					throw new Exception("[MapCommandGenerator] Erreur lors de la génération de commande pour le type de map "+mapType.getName(), e);
				}
			}
			mapCommand.withSubcommand(editCommand);
			mapCommand.register();
			
			NekotineCore.LOGGER.info("[MapCommandGenerator] Des commandes ont été automatiquement générées pour gérer des types cartes.");
		}catch(Exception e) {
			NekotineCore.LOGGER.logp(Level.SEVERE,
					"MapCommandGenerator",
					"void generateFor(Class<? extends MapElement> ...element)",
					"[MapCommandGenerator] Une erreur est survenue lors de la génération des commandes de map",
					e);
		}
	}
	
	private static <T> Argument<T> makeMapArgument(Class<T> mapType){
		return new CustomArgument<T,String>(new StringArgument("mapName"),info ->{
			T map;
			try {
				var storage = NekotineCore.IOC.tryResolve(IMapStorage.class);
				if (storage.isPresent()) {
					map = storage.get().get(mapType, info.currentInput());
				}else {
					var newStorage = new BufferMapStorage();
					map = newStorage.get(mapType, info.currentInput());
					NekotineCore.IOC.registerSingletonAs(newStorage, IMapStorage.class);
				}
			}catch(Exception e) {
				NekotineCore.LOGGER.logp(Level.WARNING,
						"MapCommandGenerator",
						"<T> Argument<T> makeMapArgument(Class<T> mapType)",
						"Erreur lors de la récupération de la carte", e);
				throw CustomArgumentException.fromString("Erreur interne lors de la récupération de la carte.");
			}
			if (map == null) {
				throw CustomArgumentException.fromMessageBuilder(new MessageBuilder("Carte inconnue: ").appendArgInput().appendHere());
			}
			return map;
		});
	}

	/**
	 * Create map command with add,remove and list subcommands
	 * @return
	 */
	private static CommandAPICommand makeMapCommand() {
		var command = new CommandAPICommand("map");
		command.executes((CommandExecutor)(sender, args) -> sender.sendMessage("Usage: /map <action>"), ExecutorType.ALL);
		return command;
	}
	
	public static void setupDefaultResolver() {
		needToAddDefaultResolvers = false;
		NekotineCore.IOC.registerSingletonAs(
				new MapElementCommandGeneratorResolver(new DefaultMapElementCommandGenerator())
				.registerGenerator(MapDictionaryElement.class, new DictionaryCommandGenerator())
				.registerGenerator(MapPositionElement.class, new PositionCommandGenerator())
				.registerGenerator(MapBlockPositionElement.class, new BlockPositionCommandGenerator()),
				IMapElementCommandGeneratorResolver.class);
	}
	
	/*OLD*/
	/*
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
					list.addAll(generateForNode(annotation.value(), (Class<? extends MapComponent>) componentField.getType(), componentField));
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
	*/
}
