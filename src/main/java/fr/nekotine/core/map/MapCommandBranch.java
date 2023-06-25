package fr.nekotine.core.map;

import dev.jorel.commandapi.arguments.Argument;

public record MapCommandBranch(Argument<?>[] arguments, MapCommandExecutor consumer){
}
