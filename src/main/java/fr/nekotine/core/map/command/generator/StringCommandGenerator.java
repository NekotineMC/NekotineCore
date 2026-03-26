package fr.nekotine.core.map.command.generator;

import java.util.function.Function;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.text.Colors;
import net.kyori.adventure.text.Component;

public class StringCommandGenerator implements MapElementCommandGenerator{

	private static final String nodeName = "StringElementNode";
	
	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var arguments = new Argument<?>[] {new TextArgument(nodeName)};
		MapCommandExecutor executor = (element, sender, args) ->{
			var string = args.get(nodeName);
			sender.sendMessage(Component.text("Le text à bien été défini.", Colors.Command.SUCCESS));
			return string;// On override l'existant
		};
		return new MapCommandBranch[] {new MapCommandBranch(arguments, executor)};
	}

}