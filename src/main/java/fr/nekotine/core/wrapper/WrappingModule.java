package fr.nekotine.core.wrapper;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "WrappingModule")
public class WrappingModule extends PluginModule{

	private Map<Object, Map<Class<?>, WrapperBase<? extends Object>>> store;
	
	@Override
	protected void onEnable() {
		store = new WeakHashMap<>();
	}
	
	@Override
	protected void onDisable() {
		store = null;
	}
	
	public <U, T extends WrapperBase<U>> boolean hasWrapper(Object source, Class<T> wrapperType) {
		var srcMap = store.get(source);
		if (srcMap == null) {
			return false;
		}
		return srcMap.containsKey(wrapperType);
	}
	
	public <U, T extends WrapperBase<U>> T get(Object source, Class<T> wrapperType) {
		try {
			var entityStore = store.get(source);
			return wrapperType.cast(entityStore.get(wrapperType));
		}catch(Exception e) {
			return null;
		}
	}
	
	public <U, T extends WrapperBase<U>> void put(Object source, Class<T> wrapperType, T wrapper) {
		if (store == null) {
			logException(Level.WARNING, "Impossible d'ajouter un wrapper car le module n'est probablement pas chargé.", new IllegalStateException());
			return;
		}
		try {
			var entityStore = store.get(source);
			if (entityStore != null) {
				entityStore.put(wrapperType, wrapper);
			}else {
				entityStore = new WeakHashMap<>();
				entityStore.put(wrapperType, wrapper);
				store.put(source, entityStore);
			}
		}catch(Exception e) {
			logException(Level.WARNING, "Une erreur est survenue lors de l'ajout d'un wrapper à l'entitée.", e);
		}
	}
}