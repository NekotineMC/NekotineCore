package fr.nekotine.core.defaut;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class DefaultProvider implements IDefaultProvider {

	private Map<Class<?>,Supplier<?>> suppliers = new HashMap<>();
	
	@Override
	public <T> @Nullable T get(Class<T> clazz) {
		var sup = getSupplier(clazz);
		return sup != null ? sup.get() : null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable Supplier<T> getSupplier(Class<T> clazz) {
		if (!suppliers.containsKey(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
			try {
				var ctor = clazz.getConstructor();
				suppliers.put(clazz, ()->{
					try {
						return ctor.newInstance();
					} catch (Exception e) {
						return null;
					}
				});
			} catch (Exception e) {
			}
		}
		return (Supplier<T>)suppliers.get(clazz);
	}

	@Override
	public <T> void register(Supplier<T> supplier) {
		suppliers.put(supplier.get().getClass(), supplier);
	}

	@Override
	public <T, D extends T> void registerAs(Supplier<D> supplier, Class<T> as) {
		suppliers.put(as, supplier);
		
	}
	
	// REGISTER SOME DEFAULTS
	public DefaultProvider() {
		registerAs(() -> Bukkit.getWorlds().get(0),World.class);
		registerAs(() -> new Location(get(World.class),0,0,0),Location.class);
	}

}
