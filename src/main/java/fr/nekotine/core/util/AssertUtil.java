package fr.nekotine.core.util;

import java.util.Optional;

public class AssertUtil {

	public static void nonNull(Object object) {
		nonNull(object, "L'objet donné ("+object.getClass()+") est null.");
	}
	
	public static void nonNull(Object object, String message) {
		if (object == null) {
			throw new AssertionError(message);
		}
	}
	
	public static <T> void nonNull(Optional<T> optional) {
		nonNull(optional, "L'objet donné ("+optional.getClass()+") est null.");
	}
	
	public static <T> void nonNull(Optional<T> optional, String message) {
		if (optional.isEmpty()) {
			throw new AssertionError(message);
		}
	}
	
}
