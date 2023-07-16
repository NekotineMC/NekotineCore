package fr.nekotine.core.util;

public class TimeUtil {
	
	/**
	 * 
	 * @param started
	 * @return Le temps passé en ms depuis started
	 */
	public static long elapsedFromMillis(long started) {
		return System.currentTimeMillis() - started;
	}
}
