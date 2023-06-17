package fr.nekotine.core.ioc;

import java.util.function.Supplier;

public interface IIocProvider {

	public <T> IIocProvider registerSingleton(T singleton);
	
	public <T,D extends T> IIocProvider registerSingletonAs(D singleton, Class<T> asType);
	
	public <T,D extends T> IIocProvider registerTransientAs(Supplier<D> factory, Class<T> asType);
	
	public <T> T resolve(Class<T> type);
	
}
