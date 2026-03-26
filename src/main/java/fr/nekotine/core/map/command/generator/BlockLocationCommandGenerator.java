package fr.nekotine.core.map.command.generator;

import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.text.Colors;
import net.kyori.adventure.text.Component;

/**
 * Générateur de commande pour un champ de type {@link org.bukkit.Location Location}
 * Il permet de définir les coordonées en arrondissant au block
 * 
 * @author XxGoldenbluexX
 *
 */
public class BlockLocationCommandGenerator implements MapElementCommandGenerator{

	private static final String nodeName = "MapBlockLocationElementNode";
	
	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var arguments = new Argument<?>[] {new LocationArgument(nodeName, LocationType.BLOCK_POSITION)};
		MapCommandExecutor executor = (element, sender, args) ->{
			var pos = (Location)args.get(nodeName);
			var e = (BlockVector)element;
			e.setX((double)pos.getBlockX());
			e.setY((double)pos.getBlockY());
			e.setZ((double)pos.getBlockZ());
			sender.sendMessage(Component.text("La position du block à bien été définie.", Colors.Command.SUCCESS));
			return e;
		};
		MapCommandExecutor worldEditExecutor = (element, sender, args) ->{
			if (!(sender instanceof Player player)) {
				sender.sendMessage(Component.text("Vous devez être un joueur pour executer cette commande.", Colors.Command.WARNING));
				return element;
			}
			var session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
			try {
				var sel = session.getSelection();
				var bb = sel.getBoundingBox();
				var e = (BlockVector)element;
				if (bb.getMinimumX() != bb.getMaximumX() ||
						bb.getMinimumY() != bb.getMaximumY() ||
						bb.getMinimumZ() != bb.getMaximumZ()) {
					sender.sendMessage(Component.text("Vous devez sélectionner seulement UN block avec world edit.", Colors.Command.WARNING));
					return element;
				}
				e.setX((double)bb.getMinimumX());
				e.setY((double)bb.getMinimumY());
				e.setZ((double)bb.getMinimumZ());
				sender.sendMessage(Component.text("La location à bien été définie.", Colors.Command.SUCCESS));
			}catch(IncompleteRegionException ex) {
				sender.sendMessage(Component.text("Vous devez sélectionner un block avec world edit.", Colors.Command.WARNING));
			}
			return element;
		};
		return new MapCommandBranch[] {
				new MapCommandBranch(arguments, executor),
				new MapCommandBranch(new Argument<?>[]{},worldEditExecutor)
				};
	}

}
