package fr.nekotine.core.map.command.generator;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapCommandGenerator;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.util.CollectionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DictionaryCommandGenerator implements MapElementCommandGenerator{

	private static final String nodeNameSuffix = "Name";
	
	private Logger logger = new NekotineLogger(getClass());
	
	private Class<?> nestedElementType;
	
	private String nodeName;
	
	private MapCommandGenerator globalGenerator;
	
	public DictionaryCommandGenerator(MapCommandGenerator generator) {
		globalGenerator = generator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var constructionList = new LinkedList<MapCommandBranch>();
		constructionList.add(makeAddCommand());
		constructionList.add(makeRemoveCommand(pipeline));
		// Commande Edit
		final var finalNodeName = nodeName + nodeNameSuffix;
		var nodeArg = new LiteralArgument("edit");
		var nameArg = new StringArgument(finalNodeName);
		nameArg.includeSuggestions(ArgumentSuggestions.stringCollectionAsync(i -> CompletableFuture.supplyAsync(() ->
				((MapDictionaryElement<Object>)pipeline.apply(i.previousArgs())).backingMap().keySet()
				)));
		var generator = globalGenerator.getGeneratorResolver().resolve(nestedElementType);
		Function<CommandArguments, Object> pip = a -> ((MapDictionaryElement<Object>)pipeline.apply(a)).backingMap().get(finalNodeName);
		for (var branch : generator.generateFor(pip, nestedElementType)) {
			var branchArgs = CollectionUtil.linkedList(branch.arguments());
			branchArgs.add(0, nameArg);
			branchArgs.add(0, nodeArg);
			MapCommandExecutor executor = (element, sender, args) ->{
				var mapKey = (String)args.get(finalNodeName);
				try {
					var e = (MapDictionaryElement<Object>)element;
					if (!e.backingMap().containsKey(mapKey)) {
						sender.sendMessage(Component.text("Ce nom d'élément ("+mapKey+") n'existe pas"));
						return;
					}
					branch.consumer().accept(e.backingMap().get(mapKey), sender, args);
				}catch(Exception e) {
					var ex = new RuntimeException("Impossible d'acceder a la valeur "+mapKey+" du dictionnaire "
				+finalNodeName+" de la classe "+elementType.getName(),e);
					logger.throwing("DictionaryCommandGenerator", "MapCommandBranch[] generateFor(Class<?> elementType)", ex);
					throw ex;
				}
			};
			constructionList.add(new MapCommandBranch(branchArgs.toArray(Argument<?>[]::new), executor));
		}
		return constructionList.toArray(MapCommandBranch[]::new);
	}
	
	private MapCommandBranch makeAddCommand() {
		Constructor<?> tempConstructor = null;
		try {
			tempConstructor = nestedElementType.getConstructor();
		}catch(Exception e) {
			throw new IllegalArgumentException("Aucun constructeur par defaut pour le type "+nestedElementType.getName()+
					". La generation de la commande \"add\" du dictionnaire a echouee.");
		}
		final var constructor = tempConstructor;
		var arguments = new Argument<?>[] {new LiteralArgument("add"),new StringArgument("itemName")};
		MapCommandExecutor executor = (element, sender, args) ->{
			var mapKey = (String)args.get("itemName");
			@SuppressWarnings("unchecked")
			var e = (MapDictionaryElement<Object>)element;
			try {
				e.backingMap().put(mapKey, constructor.newInstance());
				sender.sendMessage(Component.text("L'ajout à bien été fait.", NamedTextColor.GREEN));
				
			} catch (Exception ex) {
				logger.logp(Level.SEVERE, "DictionaryCommandGenerator", "makeAddCommand",
						"Impossible d'instancier le nouvel element de carte a ajouter au dictionnaire "+nodeName + " du type "+element.getClass().getName(),
						ex);
			}
			
		};
		//TODO normaliser les messages de commande
		logger.info("DictionaryCommandGenerator.makeRemoveCommand utilise des messages de commande non-normalise");
		return new MapCommandBranch(arguments, executor);
	}
	
	@SuppressWarnings("unchecked")
	private MapCommandBranch makeRemoveCommand(Function<CommandArguments, Object> pipeline) {
		var nameArg = new StringArgument("itemName");
		nameArg.includeSuggestions(ArgumentSuggestions.stringCollectionAsync(i -> CompletableFuture.supplyAsync(() ->
		((MapDictionaryElement<Object>)pipeline.apply(i.previousArgs())).backingMap().keySet()
		)));
		var arguments = new Argument<?>[] {new LiteralArgument("remove"),nameArg};
		MapCommandExecutor executor = (element, sender, args) ->{
			var mapKey = (String)args.get("itemName");
			var e = (MapDictionaryElement<?>)element;
			if (!e.backingMap().containsKey(mapKey)) {
				sender.sendMessage(Component.text("Ce nom d'élément ("+mapKey+") n'est pas présent.", NamedTextColor.GREEN));
			}
			e.backingMap().remove(mapKey);
			sender.sendMessage(Component.text("La suppression à bien été faite.", NamedTextColor.GREEN));
		};
		//TODO normaliser les messages de commande
		logger.info("DictionaryCommandGenerator.makeRemoveCommand utilise des messages de commande non-normalise");
		return new MapCommandBranch(arguments, executor);
	}
	
	public void setNestedElementType(Class<?> nestedElementType) {
		this.nestedElementType = nestedElementType;
	}
	
	public void setNodeName(String name) {
		this.nodeName = name;
	}

}
