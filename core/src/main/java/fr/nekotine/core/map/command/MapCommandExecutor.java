package fr.nekotine.core.map.command;

import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface MapCommandExecutor {

	public Object accept(Object element, CommandSender sender, CommandArguments args);
}
