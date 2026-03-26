package fr.nekotine.core.map.command.generator;

import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.util.BlockVector;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.text.Colors;
import net.kyori.adventure.text.Component;

public class LocationCommandGenerator implements MapElementCommandGenerator{

	private static final String nodeName = "MapLocationElementNode";
	
	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var arguments = new Argument<?>[] {new LocationArgument(nodeName, LocationType.PRECISE_POSITION)};
		MapCommandExecutor executor = (element, sender, args) ->{
			var pos = (Location)args.get(nodeName);
			var e = (BlockVector)element;
			e.setX(pos.getX());
			e.setY(pos.getY());
			e.setZ(pos.getZ());
			sender.sendMessage(Component.text("La position à bien été définie.", Colors.Command.SUCCESS));
			return element;
		};
		return new MapCommandBranch[] {new MapCommandBranch(arguments, executor)};
	}

}
