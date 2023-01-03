package fr.nekotine.core.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "WrappingModule")
public class WrappingModule extends PluginModule{

	private Map<Object, Map<Class<? extends Object>, WrapperBase<? extends Object>>> store;
	
	@Override
	protected void onEnable() {
		store = new WeakHashMap<>();
	}
	
	@Override
	protected void onDisable() {
		store = null;
	}
	
	public <U, T extends WrapperBase<U>> boolean hasWrapper(U source, Class<T> wrapperType) {
		if (store == null) {
			throw new IllegalStateException("Impossible d'ajouter un wrapper car le module n'est probablement pas chargé.");
		}
		try {
			var srcMap = store.get(source);
			if (srcMap == null) {
				return false;
			}
			return srcMap.containsKey(wrapperType);
		}catch(Exception e) {
			logException(Level.WARNING, "Une erreur est survenue lors de l'ajout d'un wrapper à l'entitée.", e);
			return false;
		}
	}
	
	public <U, T extends WrapperBase<U>> T getWrapper(U source, Class<T> wrapperType) {
		if (store == null) {
			throw new IllegalStateException("Impossible d'ajouter un wrapper car le module n'est probablement pas chargé.");
		}
		try {
			var entityStore = store.get(source);
			return wrapperType.cast(entityStore.get(wrapperType));
		}catch(Exception e) {
			logException(Level.WARNING, "Une erreur est survenue lors de la récupération d'un wrapper de l'entitée.", e);
			return null;
		}
	}
	
	public <U, T extends WrapperBase<U>> void putWrapper(U source, T wrapper) {
		if (store == null) {
			throw new IllegalStateException("Impossible d'ajouter un wrapper car le module n'est probablement pas chargé.");
		}
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
			logException(Level.WARNING, "Une erreur est survenue lors de l'ajout d'un wrapper à l'entitée.", e);
		}
	}
	
	public <U, T extends WrapperBase<U>> void removeWrapper(U source, Class<T> wrapperType) {
		if (store == null) {
			throw new IllegalStateException("Impossible d'ajouter un wrapper car le module n'est probablement pas chargé.");
		}
		var entityStore = store.get(source);
		if (entityStore != null) {
			entityStore.remove(wrapperType);
		}
	}
}