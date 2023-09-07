package fr.nekotine.core.ioc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import fr.nekotine.core.util.map.TypeHashMap;
import fr.nekotine.core.util.map.TypeMap;

public class IocProvider implements IIocProvider{

	private TypeMap singletonMap = new TypeHashMap();
	
	private Map<Object, Supplier<Object>> factoryMap = new HashMap<>();
	
	@Override
	public <T> IIocProvider registerSingleton(T singleton) {
		singletonMap.put(singleton);
		return this;
	}

	@Override
	public <T, D extends T> IIocProvider registerSingletonAs(D singleton, Class<T> asType) {
		singletonMap.put(asType, singleton);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, D extends T> IIocProvider registerTransientAs(Supplier<D> factory, Class<T> asType) {
		factoryMap.put(asType, (Supplier<Object>) factory);
		return this;
	}

	@Override
	public <T> T resolve(Class<T> type) {
		if (singletonMap.containsKey(type)) {
			return singletonMap.get(type);
		}
		if (factoryMap.containsKey(type)) {
			return type.cast(factoryMap.get(type).get());
		}
		throw new IllegalArgumentException("Aucune resolution pour le type "+type.getName());
	}
	
	@Override
	public <T> Optional<T> tryResolve(Class<T> type) {
		if (singletonMap.containsKey(type)) {
			return Optional.of(singletonMap.get(type));
		}
		if (factoryMap.containsKey(type)) {
			return Optional.of(type.cast(factoryMap.get(type).get()));
		}
		return Optional.empty();
	}

}
