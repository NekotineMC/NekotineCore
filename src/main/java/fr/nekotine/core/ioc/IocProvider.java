package fr.nekotine.core.ioc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class IocProvider implements IIocProvider{

	private Map<Object, Object> singletonMap = new HashMap<>();
	
	private Map<Object, Supplier<Object>> factoryMap = new HashMap<>();
	
	@Override
	public <T> IIocProvider registerSingleton(T singleton) {
		singletonMap.put(singleton.getClass(), singleton);
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
			return type.cast(singletonMap.get(type));
		}
		if (factoryMap.containsKey(type)) {
			return type.cast(factoryMap.get(type).get());
		}
		throw new IllegalArgumentException("Aucune resolution pour le type "+type.getName());
	}

}
