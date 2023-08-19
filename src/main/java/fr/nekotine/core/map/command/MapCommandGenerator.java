package fr.nekotine.core.map.command;

import java.util.logging.Level;

import org.bukkit.Location;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.executors.ExecutorType;
import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.map.MapModule;
import fr.nekotine.core.map.command.generator.BlockPositionCommandGenerator;
import fr.nekotine.core.map.command.generator.DefaultMapElementCommandGenerator;
import fr.nekotine.core.map.command.generator.DictionaryCommandGenerator;
import fr.nekotine.core.map.command.generator.LocationCommandGenerator;
import fr.nekotine.core.map.command.generator.PositionCommandGenerator;
import fr.nekotine.core.map.element.MapBlockPositionElement;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.map.element.MapPositionElement;
import net.kyori.adventure.text.Component;

public class MapCommandGenerator implements IMapCommandGenerator {

	private CommandAPICommand mapCommand;

	private final IMapElementCommandGeneratorResolver generatorResolver;

	public MapCommandGenerator() {
		generatorResolver = new MapElementCommandGeneratorResolver(new DefaultMapElementCommandGenerator(this))
				.registerGenerator(MapDictionaryElement.class, new DictionaryCommandGenerator(this))
				.registerGenerator(MapPositionElement.class, new PositionCommandGenerator())
				.registerGenerator(MapBlockPositionElement.class, new BlockPositionCommandGenerator())
				.registerGenerator(Location.class, new LocationCommandGenerator());
	}

	public IMapElementCommandGeneratorResolver getGeneratorResolver() {
		return generatorResolver;
	}

	@Override
	public void register() {
		mapCommand.register();
	}

	public void generateFor(Class<?>... mapTypes) {
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
			for (var mapType : mapTypes) {
				try {
					// EDIT
					var mapTypeName = mapType.getSimpleName();
					var mapNameArgument = makeMapArgument(mapType);
					var generator = generatorResolver.resolve(mapType);
					for (var branch : generator.generateFor(mapType)) {
						var command = new CommandAPICommand(mapTypeName);
						command.withArguments(mapNameArgument);
						command.withArguments(branch.arguments());
						CommandExecutor executor = (sender, args) -> {
							branch.consumer().accept(args.get(0)/* map */, sender, args);
						};
						command.executes(executor, ExecutorType.ALL);
						editCommand.withSubcommand(command);
					}
					// ADD
					var typedAddCommand = new CommandAPICommand(mapTypeName);
					typedAddCommand.withArguments(new StringArgument("mapName"));
					typedAddCommand.executes((CommandExecutor) (sender, args) -> {
						NekotineCore.MODULES.get(MapModule.class).addMapAsync(mapType, (String) args.get("mapName"),
								handle -> sender.sendMessage(Component.text("La carte e bien été créé")));
					}, ExecutorType.ALL);// TODO standardiser command messages
					addCommand.withSubcommand(typedAddCommand);
					// REMOVE
					var typedRemoveCommand = new CommandAPICommand(mapTypeName);
					typedRemoveCommand.withArguments(new StringArgument("mapName"));
					typedRemoveCommand.executes((CommandExecutor) (sender, args) -> {
						NekotineCore.MODULES.get(MapModule.class).addMapAsync(mapType, (String) args.get("mapName"));
					}, ExecutorType.ALL);
					addCommand.withSubcommand(typedAddCommand);
					NekotineCore.LOGGER
							.info("[MapCommandGenerator] Commandes générées pour le type de carte " + mapTypeName);
				} catch (Exception e) {
					throw new Exception(
							"[MapCommandGenerator] Erreur lors de la génération de commande pour le type de map "
									+ mapType.getName(),
							e);
				}
			}
			mapCommand.withSubcommand(editCommand);

			NekotineCore.LOGGER.info(
					"[MapCommandGenerator] Des commandes ont été automatiquement générées pour gérer des types cartes.");
		} catch (Exception e) {
			NekotineCore.LOGGER.logp(Level.SEVERE, "MapCommandGenerator",
					"void generateFor(Class<? extends MapElement> ...element)",
					"[MapCommandGenerator] Une erreur est survenue lors de la génération des commandes de map", e);
		}
	}

	private <T> Argument<T> makeMapArgument(Class<T> mapType) {
		return new CustomArgument<T, String>(new StringArgument("mapName"), info -> {
			T map;
			try {
				var id = NekotineCore.MODULES.get(MapModule.class).getMapFinder().findByName(mapType,
						info.currentInput());
				var untypedMap = id.loadConfig();
				map = mapType.cast(untypedMap);
			} catch (Exception e) {
				NekotineCore.LOGGER.logp(Level.WARNING, "MapCommandGenerator",
						"<T> Argument<T> makeMapArgument(Class<T> mapType)",
						"Erreur lors de la récupération de la carte", e);
				throw CustomArgumentException.fromString("Erreur interne lors de la récupération de la carte.");
			}
			if (map == null) {
				throw CustomArgumentException
						.fromMessageBuilder(new MessageBuilder("Carte inconnue: ").appendArgInput().appendHere());
			}
			return map;
		});
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
