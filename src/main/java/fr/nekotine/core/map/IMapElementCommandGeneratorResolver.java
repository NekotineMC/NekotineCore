package fr.nekotine.core.map;

public interface IMapElementCommandGeneratorResolver {

	public MapElementCommandGenerator resolve(Class<?> elementType);
	
}
