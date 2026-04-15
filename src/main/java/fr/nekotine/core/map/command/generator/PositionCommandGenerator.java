package fr.nekotine.core.map.command.generator;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.Rotation;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.text.Colors;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public class PositionCommandGenerator implements MapElementCommandGenerator {

	private static final String nodeName = "MapPositionElementNode";
	private static final String rotationNodeName = "MapPositionRotationNode";

	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var arguments = new Argument<?>[]{new LocationArgument(nodeName, LocationType.PRECISE_POSITION),
				new RotationArgument(rotationNodeName)};
		MapCommandExecutor executor = (element, sender, args) -> {
			var pos = (Location) args.get(nodeName);
			var rot = (Rotation) args.get(rotationNodeName);
			var e = (Location) element;
			e.set(pos.getX(), pos.getY(), pos.getZ());
			e.setYaw(rot.getYaw());
			e.setPitch(rot.getPitch());
			sender.sendMessage(Component.text("La position à bien été définie.", Colors.Command.SUCCESS));
			return element;
		};
		return new MapCommandBranch[]{new MapCommandBranch(arguments, executor)};
	}
}
