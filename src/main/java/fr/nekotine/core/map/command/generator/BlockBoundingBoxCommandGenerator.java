package fr.nekotine.core.map.command.generator;

import org.bukkit.Location;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.map.element.MapBlockBoundingBoxElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BlockBoundingBoxCommandGenerator extends MapElementCommandGenerator{

	private static final String nodeName = "MapBlockBoundingBoxElementNode";

	protected MapCommandBranch[] generateFor(Class<?> elementType) {
		var arguments = new Argument<?>[] {
			new LocationArgument(nodeName+"1", LocationType.BLOCK_POSITION),
			new LocationArgument(nodeName+"2", LocationType.BLOCK_POSITION),
			};
		MapCommandExecutor executor = (element, sender, args) ->{
			var pos1 = (Location)args.get(nodeName+1);
			var pos2 = (Location)args.get(nodeName+2);
			var e = (MapBlockBoundingBoxElement)element;
			e.get().resize(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(), pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
			sender.sendMessage(Component.text("La location à bien été définie.", NamedTextColor.GREEN));
		};
		//TODO normaliser les messages de commande
		NekotineCore.LOGGER.info("BlockBoundingBoxCommandGenerator.generateFor utilise des messages de commande non-normalise");
		return new MapCommandBranch[] {new MapCommandBranch(arguments, executor)};
	}

}
