package fr.nekotine.core.util;

public class ReflexionUtil {

	private ReflexionUtil() {
	}

	public static String getCallingClassName() {
		return StackWalker.getInstance().walk(stream -> stream.map(StackWalker.StackFrame::getClassName).skip(2) // Skip
																													// this
																													// method,
																													// plus
																													// the
																													// calling
																													// method.
				.findFirst())
				.orElseThrow(() -> new IllegalArgumentException("Not enough stack elements to skip 2 elements"));
	}

	public static Class<?> getCallingClass() {
		try {
			return Class.forName(StackWalker.getInstance()
					.walk(stream -> stream.map(StackWalker.StackFrame::getClassName).skip(2) // Skip this method, plus
																								// the calling method.
							.findFirst())
					.orElseThrow(() -> new IllegalArgumentException("Not enough stack elements to skip 2 elements")));
		} catch (ClassNotFoundException e) {
			// Should never happen
			return null;
		}
	}
}
