package fr.nekotine.core.defaut;

import java.util.function.Supplier;

public interface IDefaultProvider {

	public <T> T get(Class<T> clazz);
	
	public <T> Supplier<T> getSupplier(Class<T> clazz);
	
	public <T> void register(Supplier<T> supplier);
	
	public <T,D extends T> void registerAs(Supplier<D> supplier, Class<T> as);
	
}
