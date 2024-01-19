package fr.nekotine.core.map.command.generator;

import java.util.function.Function;
import java.util.logging.Logger;

import org.bukkit.Location;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Générateur de commande pour un champ de type {@link org.bukkit.Location Location}
 * Il permet de définir les coordonées en arrondissant au block
 * 
 * @author XxGoldenbluexX
 *
 */
public class BlockLocationCommandGenerator implements MapElementCommandGenerator{

	private static final String nodeName = "MapBlockLocationElementNode";

	private Logger logger = new NekotineLogger(getClass());
	
	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var arguments = new Argument<?>[] {new LocationArgument(nodeName, LocationType.BLOCK_POSITION)};
		MapCommandExecutor executor = (element, sender, args) ->{
			var pos = (Location)args.get(nodeName);
			var e = (Location)element;
			e.set(pos.getX(), pos.getY(), pos.getZ());
			sender.sendMessage(Component.text("La position du block à bien été définie.", NamedTextColor.GREEN));
			return e;
		};
		//TODO normaliser les messages de commande
		logger.info("BlockLocationCommandGenerator.generateFor utilise des messages de commande non-normalise");
		return new MapCommandBranch[] {new MapCommandBranch(arguments, executor)};
	}

}
