package fr.nekotine.core.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.logging.Level;

import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.module.PluginModule;

public class WrappingModule extends PluginModule{

	private Map<Object, Map<Class<? extends Object>, WrapperBase<? extends Object>>> store = new WeakHashMap<>();
	
	public <U, T extends WrapperBase<U>> boolean hasWrapper(U source, Class<T> wrapperType) {
		try {
			var srcMap = store.get(source);
			if (srcMap == null) {
				return false;
			}
			return srcMap.containsKey(wrapperType);
		}catch(Exception e) {
			logger.log(Level.WARNING, "Une erreur est survenue lors de l'ajout d'un wrapper à l'entitée.", e);
			return false;
		}
	}
	
	public <U, T extends WrapperBase<U>> T getWrapper(U source, Class<T> wrapperType) {
		try {
			var entityStore = store.get(source);
			return wrapperType.cast(entityStore.get(wrapperType));
		}catch(Exception e) {
			logger.log(Level.WARNING, "Une erreur est survenue lors de la récupération d'un wrapper de l'entitée.", e);
			return null;
		}
	}
	
	public <U, T extends WrapperBase<U>> @Nullable T getWrapperNullable(U source, Class<T> wrapperType) {
		var entityStore = store.get(source);
		if (entityStore == null) {
			return null;
		}
		return wrapperType.cast(entityStore.get(wrapperType));
	}
	
	public <U, T extends WrapperBase<U>> Optional<T> getWrapperOptional(U source, Class<T> wrapperType) {
		var entityStore = store.get(source);		
		if (entityStore == null) {
			return Optional.empty();
		}
		return Optional.of(wrapperType.cast(entityStore.get(wrapperType)));
	}
	
	public <U, T extends WrapperBase<U>> void putWrapper(U source, T wrapper) {
		try {
			var entityStore = store.get(source);
			if (entityStore != null) {
				entityStore.put(wrapper.getClass(), wrapper);
			}else {
				entityStore = new HashMap<>();
				entityStore.put(wrapper.getClass(), wrapper);
				store.put(source, entityStore);
			}
		}catch(Exception e) {
			logger.log(Level.WARNING, "Une erreur est survenue lors de l'ajout d'un wrapper à l'entitée.", e);
		}
	}
	
	public <U, T extends WrapperBase<U>> void makeWrapper(U source, Function<U,T> wrapperProvider) {
		putWrapper(source, wrapperProvider.apply(source));
	}
	
	public <U, T extends WrapperBase<U>> void removeWrapper(U source, Class<T> wrapperType) {
		var entityStore = store.get(source);
		if (entityStore != null) {
			entityStore.remove(wrapperType);
		}
	}
}