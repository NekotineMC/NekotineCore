package fr.nekotine.core.map.command;

public abstract class MapElementCommandGenerator {

	private boolean cacheResult;
	
	private MapCommandBranch[] cache;
	
	public MapElementCommandGenerator() {
		this(true);
	}
	
	/**
	 * Constructeur
	 * @param cacheResult si oui ou non le résultat peut être caché pour éviter de multiples générations.
	 */
	public MapElementCommandGenerator(boolean cacheResult) {
		this.cacheResult = cacheResult;
	}
	
	protected abstract MapCommandBranch[] generateFor(Class<?> elementType);
	
	public final MapCommandBranch[] getGenerated(Class<?> elementType){
		if (cacheResult) {
			if (cache == null) {
				cache = generateFor(elementType);
			}
			return cache;
		}
		return generateFor(elementType);
	}
	
}
