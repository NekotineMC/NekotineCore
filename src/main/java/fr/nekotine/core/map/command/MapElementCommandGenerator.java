package fr.nekotine.core.map.command;

import java.util.function.Function;

import dev.jorel.commandapi.executors.CommandArguments;

public interface MapElementCommandGenerator {
	
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType);
	
}
