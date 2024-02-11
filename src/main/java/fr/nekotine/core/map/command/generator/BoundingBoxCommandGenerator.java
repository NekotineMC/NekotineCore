package fr.nekotine.core.map.command.generator;

import java.util.function.Function;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

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
			var e = (BoundingBox)element;
			e.resize(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
			sender.sendMessage(Component.text("La location à bien été définie.", NamedTextColor.GREEN));
			return element;
		};
		MapCommandExecutor worldEditExecutor = (element, sender, args) ->{
			if (!(sender instanceof Player player)) {
				sender.sendMessage(Component.text("Vous devez être un joueur pour executer cette commande"));
				return element;
			}
			var session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
			try {
				var sel = session.getSelection();
				var bb = sel.getBoundingBox();
				var e = (BoundingBox)element;
				e.resize(bb.getMinimumX(), bb.getMinimumY(), bb.getMinimumZ(),
						bb.getMaximumX(), bb.getMaximumY(), bb.getMaximumZ());
				sender.sendMessage(Component.text("La location à bien été définie.", NamedTextColor.GREEN));
			}catch(IncompleteRegionException ex) {
				sender.sendMessage(Component.text("Vous devez sélectionner une zone avec world edit"));
			}
			return element;
		};
		//TODO normaliser les messages de commande
		logger.info("BoundingBoxCommandGenerator.generateFor utilise des messages de commande non-normalise");
		return new MapCommandBranch[] {
				new MapCommandBranch(arguments, executor),
				new MapCommandBranch(new Argument<?>[]{},worldEditExecutor)
				};
	}

}
