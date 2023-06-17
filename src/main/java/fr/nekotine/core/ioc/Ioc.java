package fr.nekotine.core.ioc;

public class Ioc {

	private static IIocProvider provider;
	
	public static void setProvider(IIocProvider provider) {
		Ioc.provider = provider;
	}
	
	public static IIocProvider getProvider() {
		return Ioc.provider;
	}
	
	public static <T> T resolve(Class<T> type) {
		return provider.resolve(type);
	}
	
}
