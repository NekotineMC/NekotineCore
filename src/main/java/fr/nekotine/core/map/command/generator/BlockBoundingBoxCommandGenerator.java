package fr.nekotine.core.map.command.generator;

import java.util.function.Function;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

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

public class BlockBoundingBoxCommandGenerator implements MapElementCommandGenerator{

	private static final String nodeName = "MapBlockBoundingBoxElementNode";

	private Logger logger = new NekotineLogger(getClass());
	
	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var arguments = new Argument<?>[] {
			new LocationArgument(nodeName+"1", LocationType.BLOCK_POSITION),
			new LocationArgument(nodeName+"2", LocationType.BLOCK_POSITION),
			};
		MapCommandExecutor executor = (element, sender, args) ->{
			var pos1 = ((Location)args.get(nodeName+1)).toVector();
			var pos2 = ((Location)args.get(nodeName+2)).toVector();
			var min = Vector.getMinimum(pos1, pos2);
			var max = Vector.getMaximum(pos1,  pos2);
			var e = (BoundingBox)element;
			e.resize(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX()+1, max.getBlockY()+1, max.getBlockZ()+1);
			sender.sendMessage(Component.text("La location à bien été définie.", NamedTextColor.GREEN));
			return element;
		};
		//TODO normaliser les messages de commande
		logger.info("BlockBoundingBoxCommandGenerator.generateFor utilise des messages de commande non-normalise");
		return new MapCommandBranch[] {new MapCommandBranch(arguments, executor)};
	}

}
