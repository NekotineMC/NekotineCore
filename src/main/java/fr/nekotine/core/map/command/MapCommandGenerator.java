package fr.nekotine.core.map.command;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.executors.ExecutorType;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.map.MapHandle;
import fr.nekotine.core.map.MapModule;
import fr.nekotine.core.map.command.generator.BlockBoundingBoxCommandGenerator;
import fr.nekotine.core.map.command.generator.BlockLocationCommandGenerator;
import fr.nekotine.core.map.command.generator.BoundingBoxCommandGenerator;
import fr.nekotine.core.map.command.generator.DefaultMapElementCommandGenerator;
import fr.nekotine.core.map.command.generator.DictionaryCommandGenerator;
import fr.nekotine.core.map.command.generator.LocationCommandGenerator;
import fr.nekotine.core.map.command.generator.PositionCommandGenerator;
import fr.nekotine.core.map.command.generator.StringCommandGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MapCommandGenerator implements IMapCommandGenerator {

	private Logger logger = new NekotineLogger(getClass());
	
	private CommandAPICommand mapCommand;

	private final IMapElementCommandGeneratorResolver generatorResolver;

	public MapCommandGenerator() {
		generatorResolver = new MapElementCommandGeneratorResolver(new DefaultMapElementCommandGenerator())
				.registerGenerator(Map.class, new DictionaryCommandGenerator())
				.registerGenerator(Location.class, new PositionCommandGenerator())
				.registerGenerator(BlockVector.class, new LocationCommandGenerator())
				.registerGenerator(BlockVector.class, new BlockLocationCommandGenerator())
				.registerGenerator(BoundingBox.class, new BoundingBoxCommandGenerator())
				.registerGenerator(BoundingBox.class, new BlockBoundingBoxCommandGenerator())
				.registerGenerator(String.class, new StringCommandGenerator());
		Ioc.getProvider().registerSingletonAs(generatorResolver,IMapElementCommandGeneratorResolver.class);
	}

	@Override
	public void register() {
		mapCommand.register();
	}

	public void generateFor(Class<?>... mapTypes) {
		var mapModule = Ioc.resolve(MapModule.class);
		try {
			if (mapCommand == null) {
				makeMapCommand();
			}
			var editCommand = new CommandAPICommand("edit");
			editCommand.executes(
					(CommandExecutor) (sender, args) -> sender.sendMessage("Usage: /map edit <mapType> <mapName>"),
					ExecutorType.ALL);
			var addCommand = new CommandAPICommand("add");
			addCommand.executes(
					(CommandExecutor) (sender, args) -> sender.sendMessage("Usage: /map add <mapType> <mapName>"),
					ExecutorType.ALL);
			var removeCommand = new CommandAPICommand("remove");
			removeCommand.executes(
					(CommandExecutor) (sender, args) -> sender.sendMessage("Usage: /map remove <mapType> <mapName>"),
					ExecutorType.ALL);
			for (var mapType : mapTypes) {
				try {
					// EDIT
					var mapTypeName = mapType.getSimpleName();
					var mapNameArgument = makeMapArgument(mapType);
					var generator = generatorResolver.resolveFor(mapType);
					@SuppressWarnings("unchecked")
					Function<CommandArguments, Object> pipeline = a -> ((MapHandle<Object>)a.get("mapName")).loadConfig();
					for (var branch : generator.generateFor(pipeline, mapType)) {
						var command = new CommandAPICommand(mapTypeName);
						command.withArguments(mapNameArgument);
						command.withArguments(branch.arguments());
						CommandExecutor executor = (sender, args) -> {
							@SuppressWarnings("unchecked")
							var handle = (MapHandle<Object>) args.get(0);
							var config = handle.loadConfig();
							var newConf = branch.consumer().accept(config, sender, args);
							sender.sendMessage(Component.text("Sauvegarde de la carte...", NamedTextColor.BLUE));
							mapModule.saveMapConfigAsync(handle, newConf,
									() -> sender.sendMessage(Component.text("Sauvegarde effectuée.", NamedTextColor.GREEN)));
						};
						command.executes(executor, ExecutorType.ALL);
						editCommand.withSubcommand(command);
					}
					// ADD
					var typedAddCommand = new CommandAPICommand(mapTypeName);
					typedAddCommand.withArguments(new StringArgument("mapName"));
					typedAddCommand.executes((CommandExecutor) (sender, args) -> {
						Ioc.resolve(MapModule.class).addMapAsync(mapType, (String) args.get("mapName"),
								handle -> sender.sendMessage(Component.text("La carte a bien été créé")),
								e -> sender.sendMessage(Component.text("Une erreur est survenue lors de l'ajout:", NamedTextColor.DARK_RED)
										.appendNewline()
										.append(Component.text(e.getMessage(), NamedTextColor.RED))));
						sender.sendMessage(Component.text("Ajout de la map en cours..."));
					}, ExecutorType.ALL);// TODO standardiser command messages
					addCommand.withSubcommand(typedAddCommand);
					// REMOVE
					var typedRemoveCommand = new CommandAPICommand(mapTypeName);
					typedRemoveCommand.withArguments(new StringArgument("mapName"));
					typedRemoveCommand.executes((CommandExecutor) (sender, args) -> {
						try {
							Ioc.resolve(MapModule.class).deleteMapAsync(mapType, (String) args.get("mapName"),
								() -> sender.sendMessage(Component.text("La carte a bien été supprimée")),
								e -> sender.sendMessage(Component.text("Une erreur est survenue lors de la suppression:", NamedTextColor.DARK_RED)
										.appendNewline()
										.append(Component.text(e.getMessage(), NamedTextColor.RED))));
						sender.sendMessage(Component.text("Suppression de la map en cours..."));
						}catch(Exception e) {
							throw new RuntimeException(e);
						}
					}, ExecutorType.ALL);// TODO standardiser command messages
					removeCommand.withSubcommand(typedRemoveCommand);
					logger
							.info("[MapCommandGenerator] Commandes générées pour le type de carte " + mapTypeName);
				} catch (Exception e) {
					throw new Exception(
							"[MapCommandGenerator] Erreur lors de la génération de commande pour le type de map "
									+ mapType.getName(),
							e);
				}
			}
			mapCommand.withSubcommands(editCommand, addCommand, removeCommand);
			
			logger.info(
					"[MapCommandGenerator] Des commandes ont été automatiquement générées pour gérer des types cartes.");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, "MapCommandGenerator",
					"void generateFor(Class<? extends MapElement> ...element)",
					"[MapCommandGenerator] Une erreur est survenue lors de la génération des commandes de map", e);
		}
	}

	private <T> Argument<MapHandle<T>> makeMapArgument(Class<T> mapType) {
		return new CustomArgument<MapHandle<T>, String>(new StringArgument("mapName"), info -> {
			try {
				return Ioc.resolve(MapModule.class).getMapFinder().findByName(mapType,
						info.currentInput());
			} catch (Exception e) {
				logger.log(Level.WARNING, "Erreur lors de la récupération de la carte", e);
				throw CustomArgumentException.fromString("Erreur interne lors de la récupération de la carte.");
			}
		}).replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> CompletableFuture.supplyAsync(
				() -> {
					return Ioc.resolve(MapModule.class).getMapFinder().list()
							.stream()
							.filter(handle -> handle.getConfigType() == mapType)
							.map(handle -> handle.getName())
							.toArray(String[]::new);
				})));
	}

	/**
	 * Create map command with add,remove and list subcommands
	 * 
	 * @return
	 */
	private void makeMapCommand() {
		mapCommand = new CommandAPICommand("map");
		mapCommand.executes((CommandExecutor) (sender, args) -> sender.sendMessage("Usage: /map <action>"),
				ExecutorType.ALL);
	}
}
