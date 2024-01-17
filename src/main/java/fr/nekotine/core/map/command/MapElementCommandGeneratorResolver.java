package fr.nekotine.core.map.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public final class MapElementCommandGeneratorResolver implements IMapElementCommandGeneratorResolver{

	private final Map<Class<?>, MapElementCommandGenerator> generatorPool = new HashMap<>();
	
	private final Collection<MapElementCommandGenerator> allGenerators = new LinkedList<>();
	
	private final MapElementCommandGenerator defaultGenerator;
	
	public MapElementCommandGeneratorResolver(MapElementCommandGenerator defaultGenerator) {
		this.defaultGenerator = defaultGenerator;
		allGenerators.add(defaultGenerator);
	}
	
	@Override
	public MapElementCommandGenerator resolveFor(Class<?> elementType) {
		if (generatorPool.containsKey(elementType)){
			return generatorPool.get(elementType);
		}
		return defaultGenerator;
	}
	
	@Override
	public <T extends MapElementCommandGenerator> T resolveSpecific(Class<T> elementType) {
		var gen = allGenerators.stream().filter(g -> elementType.isAssignableFrom(g.getClass())).findAny();
		if (gen.isEmpty()) {
			return null;
		}
		return elementType.cast(gen.get());
	}
	
	public final MapElementCommandGeneratorResolver registerGenerator(Class<?> type, MapElementCommandGenerator generator) {
		if (!generatorPool.containsKey(type)) {
			generatorPool.put(type, generator);
		}
		allGenerators.add(generator);
		return this;
	}
	
}
