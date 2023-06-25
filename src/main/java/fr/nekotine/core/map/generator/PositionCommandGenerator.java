package fr.nekotine.core.map.generator;

import org.bukkit.Location;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.map.MapCommandBranch;
import fr.nekotine.core.map.MapCommandExecutor;
import fr.nekotine.core.map.MapElementCommandGenerator;
import fr.nekotine.core.map.element.MapPositionElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PositionCommandGenerator extends MapElementCommandGenerator{

	private static final String nodeName = "MapPositionElementNode";

	protected MapCommandBranch[] generateFor(Class<?> elementType) {
		var arguments = new Argument<?>[] {new LocationArgument(nodeName, LocationType.PRECISE_POSITION)};
		MapCommandExecutor executor = (element, sender, args) ->{
			var pos = (Location)args.get(nodeName);
			var e = (MapPositionElement)element;
			e.fromLocation(pos);
			sender.sendMessage(Component.text("La position à bien été définie.", NamedTextColor.GREEN));
		};
		//TODO normaliser les messages de commande
		NekotineCore.LOGGER.info("PositionCommandGenerator.generateFor utilise des messages de commande non-normalise");
		return new MapCommandBranch[] {new MapCommandBranch(arguments, executor)};
	}

}
