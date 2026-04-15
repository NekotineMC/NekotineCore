package fr.nekotine.core.map.command;

import dev.jorel.commandapi.executors.CommandArguments;
import java.util.function.Function;

public interface MapElementCommandGenerator {

	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType);
}
