package fr.nekotine.core.map;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.executors.CommandArguments;

@FunctionalInterface
public interface MapCommandExecutor {
	
	public void accept(Object element, CommandSender sender, CommandArguments args);
	
}
