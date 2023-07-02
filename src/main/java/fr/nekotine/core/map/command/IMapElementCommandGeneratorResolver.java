package fr.nekotine.core.map.command;

public interface IMapElementCommandGeneratorResolver {

	public MapElementCommandGenerator resolve(Class<?> elementType);
	
}
