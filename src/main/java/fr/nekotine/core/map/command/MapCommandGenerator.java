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
import fr.nekotine.core.map.command.generator.BlockPositionCommandGenerator;
import fr.nekotine.core.map.command.generator.DefaultMapElementCommandGenerator;
import fr.nekotine.core.map.command.generator.DictionaryCommandGenerator;
import fr.nekotine.core.map.command.generator.PositionCommandGenerator;
import fr.nekotine.core.map.element.MapBlockPositionElement;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.map.element.MapPositionElement;

public class MapCommandGenerator implements IMapCommandGenerator{
	
	private CommandAPICommand mapCommand;
	
	private final IMapElementCommandGeneratorResolver generatorResolver;
	
	public MapCommandGenerator() {
		generatorResolver = new MapElementCommandGeneratorResolver(new DefaultMapElementCommandGenerator())
				.registerGenerator(MapDictionaryElement.class, new DictionaryCommandGenerator())
				.registerGenerator(MapPositionElement.class, new PositionCommandGenerator())
				.registerGenerator(MapBlockPositionElement.class, new BlockPositionCommandGenerator());
	}
	
	public IMapElementCommandGeneratorResolver getGeneratorResolver() {
		return generatorResolver;
	}
	
	@Override
	public void register() {
		mapCommand.register();
	}
	
	public void generateFor(Class<?> ...mapTypes) {
		try {
			if (mapCommand == null) {
				makeMapCommand();
			}
			var editCommand = new CommandAPICommand("edit");
			editCommand.executes((CommandExecutor)(sender, args) -> sender.sendMessage("Usage: /map edit <mapType> <mapName>"), ExecutorType.ALL);
			for (var mapType : mapTypes) {
				try {
					
					var mapTypeName = mapType.getSimpleName();
					var mapTypeArgument = makeMapArgument(mapType);
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
			
			NekotineCore.LOGGER.info("[MapCommandGenerator] Des commandes ont été automatiquement générées pour gérer des types cartes.");
		}catch(Exception e) {
			NekotineCore.LOGGER.logp(Level.SEVERE,
					"MapCommandGenerator",
					"void generateFor(Class<? extends MapElement> ...element)",
					"[MapCommandGenerator] Une erreur est survenue lors de la génération des commandes de map",
					e);
		}
	}
	
	private <T> Argument<T> makeMapArgument(Class<T> mapType){
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
	private void makeMapCommand() {
		mapCommand = new CommandAPICommand("map");
		mapCommand.executes((CommandExecutor)(sender, args) -> sender.sendMessage("Usage: /map <action>"), ExecutorType.ALL);
	}
}
