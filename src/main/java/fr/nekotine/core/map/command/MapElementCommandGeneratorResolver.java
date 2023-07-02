package fr.nekotine.core.map.command;

import java.util.HashMap;
import java.util.Map;

public final class MapElementCommandGeneratorResolver implements IMapElementCommandGeneratorResolver{

	private final Map<Class<?>, MapElementCommandGenerator> generatorPool = new HashMap<>();
	
	private final MapElementCommandGenerator defaultGenerator;
	
	public MapElementCommandGeneratorResolver(MapElementCommandGenerator defaultGenerator) {
		this.defaultGenerator = defaultGenerator;
	}
	
	@Override
	public MapElementCommandGenerator resolve(Class<?> elementType) {
		if (generatorPool.containsKey(elementType)){
			return generatorPool.get(elementType);
		}
		return defaultGenerator;
	}
	
	public final MapElementCommandGeneratorResolver registerGenerator(Class<?> type, MapElementCommandGenerator generator) {
		generatorPool.put(type, generator);
		return this;
	}
	
}
