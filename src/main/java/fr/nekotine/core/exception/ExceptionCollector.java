package fr.nekotine.core.exception;

import java.util.LinkedList;
import java.util.List;

public class ExceptionCollector {

	private final List<Exception> exceptions = new LinkedList<>();
	
	public void collect(Runnable runnable) {
		try {
			runnable.run();
		}catch(Exception e) {
			exceptions.add(e);
		}
	}
	
	public void collect(Exception e) {
		exceptions.add(e);
	}
	
	public void throwCollected() throws Exception {
		for (var e : exceptions) {
			throw e;
		}
	}
	
	public void throwAsRuntime() {
		for (var e : exceptions) {
			if (e instanceof RuntimeException r) {
				throw r;
			}else {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void throwAsRuntime(String message) {
		for (var e : exceptions) {
			if (e instanceof RuntimeException r) {
				throw r;
			}else {
				throw new RuntimeException(message, e);
			}
		}
	}
	
}
