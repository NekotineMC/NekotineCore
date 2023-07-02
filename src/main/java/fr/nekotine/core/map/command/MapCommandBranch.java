package fr.nekotine.core.map.command;

import dev.jorel.commandapi.arguments.Argument;

public record MapCommandBranch(Argument<?>[] arguments, MapCommandExecutor consumer){
}
