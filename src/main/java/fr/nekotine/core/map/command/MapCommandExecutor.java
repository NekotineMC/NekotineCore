package fr.nekotine.core.map.command;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.executors.CommandArguments;

@FunctionalInterface
public interface MapCommandExecutor {
	
	public Object accept(Object element, CommandSender sender, CommandArguments args);
	
}
