package fr.nekotine.core.ioc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import fr.nekotine.core.util.map.TypeHashMap;
import fr.nekotine.core.util.map.TypeMap;

public class IocProvider implements IIocProvider{

	private TypeMap singletonMap = new TypeHashMap();
	
	private Map<Object, Supplier<Object>> supplierMap = new HashMap<>();
	
	@Override
	public <T> IIocProvider registerSingleton(T singleton) {
		@SuppressWarnings("unchecked")
		var type = (Class<T>)singleton.getClass();
		return registerSingletonInstanceAs(singleton, type);
	}

	@Override
	public <T, D extends T> IIocProvider registerSingletonInstanceAs(D singleton, Class<T> asType) {
		singletonMap.put(asType, singleton);
		return this;
	}
	
	@Override
	public <T, D extends T> IIocProvider registerSingletonAs(Supplier<D> factory, Class<T> asType) {
		supplierMap.put(asType, () -> {
			var singleton = factory.get();
			singletonMap.put(asType, singleton);
			return singleton;
		});
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, D extends T> IIocProvider registerTransientAs(Supplier<D> factory, Class<T> asType) {
		supplierMap.put(asType, (Supplier<Object>) factory);
		return this;
	}

	@Override
	public <T> T resolve(Class<T> type) {
		if (singletonMap.containsKey(type)) {
			return singletonMap.get(type);
		}
		if (supplierMap.containsKey(type)) {
			return type.cast(supplierMap.get(type).get());
		}
		throw new IllegalArgumentException("Aucune resolution pour le type "+type.getName());
	}
	
	@Override
	public <T> Optional<T> tryResolve(Class<T> type) {
		if (singletonMap.containsKey(type)) {
			return Optional.of(singletonMap.get(type));
		}
		if (supplierMap.containsKey(type)) {
			return Optional.of(type.cast(supplierMap.get(type).get()));
		}
		return Optional.empty();
	}

	@Override
	public <T> IIocProvider unregister(Class<T> type) {
		singletonMap.remove(type);
		supplierMap.remove(type);
		return this;
	}

}
