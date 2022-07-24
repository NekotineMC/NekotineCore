package fr.nekotine.core.util;

public class UtilTime {
	
	/**
	 * 
	 * @return Le temps en ms
	 */
	public static long GetTime() {
		return System.currentTimeMillis();
	}
	
	/**
	 * 
	 * @param time1
	 * @param time2
	 * @return time1 - time2
	 */
	public static long Difference(long time1, long time2) {
		return time1 - time2;
	}
	
	/**
	 * 
	 * @param started
	 * @return Le temps passé en ms depuis started
	 */
	public static long Passed(long started) {
		return GetTime() - started;
	}
}
