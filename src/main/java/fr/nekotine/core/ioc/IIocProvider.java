package fr.nekotine.core.ioc;

import java.util.function.Supplier;

public interface IIocProvider {

	public <T> void registerSingleton(T singleton);
	
	public <T,D extends T> void registerSingletonAs(D singleton, Class<T> asType);
	
	public <T> void registerTransient(Supplier<T> factory);
	
	public <T,D extends T> void registerTransientAs(Supplier<D> factory, Class<T> asType);
	
	public <T> T resolve(Class<T> type);
	
}
