package fr.nekotine.core.map.command.generator;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.logging.Level;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapCommandGenerator;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.util.CollectionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DictionaryCommandGenerator extends MapElementCommandGenerator{

	private static final String nodeNameSuffix = "Name";
	
	private Class<?> nestedElementType;
	
	private String nodeName;
	
	private MapCommandGenerator globalGenerator;
	
	public DictionaryCommandGenerator(MapCommandGenerator generator) {
		super(false);
		globalGenerator = generator;
	}

	protected MapCommandBranch[] generateFor(Class<?> elementType) {
		var constructionList = new LinkedList<MapCommandBranch>();
		constructionList.add(makeAddCommand());
		constructionList.add(makeRemoveCommand());
		// Commande Edit
		final var finalNodeName = nodeName + nodeNameSuffix;
		var nodeArg = new LiteralArgument("edit");
		var nameArg = new StringArgument(finalNodeName);
		var generator = globalGenerator.getGeneratorResolver().resolve(nestedElementType);
		for (var branch : generator.getGenerated(nestedElementType)) {
			var branchArgs = CollectionUtil.linkedList(branch.arguments());
			branchArgs.add(0, nameArg);
			branchArgs.add(0, nodeArg);
			MapCommandExecutor executor = (element, sender, args) ->{
				var mapKey = (String)args.get(finalNodeName);
				try {
					@SuppressWarnings("unchecked")
					var e = (MapDictionaryElement<Object>)element;
					if (!e.backingMap().containsKey(mapKey)) {
						sender.sendMessage(Component.text("Ce nom d'élément ("+mapKey+") n'existe pas"));
						return;
					}
					branch.consumer().accept(e.backingMap().get(mapKey), sender, args);
				}catch(Exception e) {
					var ex = new RuntimeException("Impossible d'acceder a la valeur "+mapKey+" du dictionnaire "
				+finalNodeName+" de la classe "+elementType.getName(),e);
					NekotineCore.LOGGER.throwing("DictionaryCommandGenerator", "MapCommandBranch[] generateFor(Class<?> elementType)", ex);
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
				NekotineCore.LOGGER.logp(Level.SEVERE, "DictionaryCommandGenerator", "makeAddCommand",
						"Impossible d'instancier le nouvel element de carte a ajouter au dictionnaire "+nodeName + " du type "+element.getClass().getName(),
						ex);
			}
			
		};
		//TODO normaliser les messages de commande
		NekotineCore.LOGGER.info("DictionaryCommandGenerator.makeRemoveCommand utilise des messages de commande non-normalise");
		return new MapCommandBranch(arguments, executor);
	}
	
	private MapCommandBranch makeRemoveCommand() {
		var arguments = new Argument<?>[] {new LiteralArgument("remove"),new StringArgument("itemName")};
		MapCommandExecutor executor = (element, sender, args) ->{
			var mapKey = (String)args.get("itemName");
			var e = (MapDictionaryElement<?>)element;
			e.backingMap().remove(mapKey);
			sender.sendMessage(Component.text("La suppression à bien été faite.", NamedTextColor.GREEN));
		};
		//TODO normaliser les messages de commande
		NekotineCore.LOGGER.info("DictionaryCommandGenerator.makeRemoveCommand utilise des messages de commande non-normalise");
		return new MapCommandBranch(arguments, executor);
	}
	
	public void setNestedElementType(Class<?> nestedElementType) {
		this.nestedElementType = nestedElementType;
	}
	
	public void setNodeName(String name) {
		this.nodeName = name;
	}

}
