package fr.nekotine.core.map.command;

public interface IMapElementCommandGeneratorResolver {

	public MapElementCommandGenerator resolveFor(Class<?> elementType);
	
	public <T extends MapElementCommandGenerator> T resolveSpecific(Class<T> generatorType);
	
}
