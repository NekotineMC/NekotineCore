package fr.nekotine.core.map.command.generator;

import org.bukkit.Location;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.wrappers.Rotation;
import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.map.element.MapPositionElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PositionCommandGenerator extends MapElementCommandGenerator{

	private static final String nodeName = "MapPositionElementNode";
	private static final String rotationNodeName = "MapPositionRotationNode";

	protected MapCommandBranch[] generateFor(Class<?> elementType) {
		var arguments = new Argument<?>[] {new LocationArgument(nodeName, LocationType.PRECISE_POSITION),
			new RotationArgument(rotationNodeName)};
		MapCommandExecutor executor = (element, sender, args) ->{
			var pos = (Location)args.get(nodeName);
			var rot = (Rotation)args.get(rotationNodeName);
			var e = (MapPositionElement)element;
			e.fromLocation(pos);
			e.setYaw(rot.getYaw());
			e.setPitch(rot.getPitch());
			sender.sendMessage(Component.text("La position à bien été définie.", NamedTextColor.GREEN));
		};
		//TODO normaliser les messages de commande
		NekotineCore.LOGGER.info("PositionCommandGenerator.generateFor utilise des messages de commande non-normalise");
		return new MapCommandBranch[] {new MapCommandBranch(arguments, executor)};
	}

}
