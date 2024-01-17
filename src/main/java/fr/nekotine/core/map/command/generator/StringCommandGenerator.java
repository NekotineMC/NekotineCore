package fr.nekotine.core.map.command.generator;

import java.util.function.Function;
import java.util.logging.Logger;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StringCommandGenerator implements MapElementCommandGenerator{

	private static final String nodeName = "StringElementNode";

	private Logger logger = new NekotineLogger(getClass());
	
	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var arguments = new Argument<?>[] {new TextArgument(nodeName)};
		MapCommandExecutor executor = (element, sender, args) ->{
			var string = args.get(nodeName);
			sender.sendMessage(Component.text("Le text à bien été défini.", NamedTextColor.GREEN));
			return string;// On override l'existant
		};
		//TODO normaliser les messages de commande
		logger.info("StringCommandGenerator.generateFor utilise des messages de commande non-normalise");
		return new MapCommandBranch[] {new MapCommandBranch(arguments, executor)};
	}

}