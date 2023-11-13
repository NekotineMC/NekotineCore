package fr.nekotine.core.ioc;

public class Ioc {

	private static IIocProvider provider = new IocProvider();
	
	/**
	 * Replace default IocProvider
	 * @param provider
	 */
	public static void setProvider(IIocProvider provider) {
		Ioc.provider = provider;
	}
	
	/**
	 * Get configured IocProvider
	 * @return
	 */
	public static IIocProvider getProvider() {
		return Ioc.provider;
	}
	
	/**
	 * Resolve dependency using configured IocProvider.
	 * If none are set, a default one will be used
	 * @param <T>
	 * @param type
	 * @return
	 */
	public static <T> T resolve(Class<T> type) {
		return provider.resolve(type);
	}
	
}
