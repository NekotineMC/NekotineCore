package fr.nekotine.core.util;

import java.time.Duration;

public class TimeUtil {
	
	/**
	 * 
	 * @param started
	 * @return Le temps passé en ms depuis started
	 */
	public static long elapsedFromMillis(long started) {
		return System.currentTimeMillis() - started;
	}
	
	public static Duration fromSeconds(double seconds) {
		return Duration.ofMillis((long)(seconds*1000));
	}
}
