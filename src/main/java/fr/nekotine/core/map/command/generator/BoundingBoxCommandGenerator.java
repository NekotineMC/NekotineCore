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
import fr.nekotine.core.map.element.MapBoundingBoxElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BoundingBoxCommandGenerator implements MapElementCommandGenerator{

	private static final String nodeName = "MapBoundingBoxElementNode";

	private Logger logger = new NekotineLogger(getClass());
	
	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var arguments = new Argument<?>[] {
			new LocationArgument(nodeName+"1", LocationType.PRECISE_POSITION),
			new LocationArgument(nodeName+"2", LocationType.PRECISE_POSITION),
			};
		MapCommandExecutor executor = (element, sender, args) ->{
			var pos1 = (Location)args.get(nodeName+1);
			var pos2 = (Location)args.get(nodeName+2);
			var e = (MapBoundingBoxElement)element;
			e.get().resize(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
			sender.sendMessage(Component.text("La location à bien été définie.", NamedTextColor.GREEN));
			return element;
		};
		//TODO normaliser les messages de commande
		logger.info("BoundingBoxCommandGenerator.generateFor utilise des messages de commande non-normalise");
		return new MapCommandBranch[] {new MapCommandBranch(arguments, executor)};
	}

}
