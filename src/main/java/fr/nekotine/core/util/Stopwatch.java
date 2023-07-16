package fr.nekotine.core.util;

import java.util.function.Consumer;

public class Stopwatch implements AutoCloseable{

	private long startNano;
	
	private long startMs; // Je suis trop nul pour arondir des nano en ms
	
	private long stopNano;
	
	private long stopMs;
	
	private Consumer<Stopwatch> stopCallback;

	public Stopwatch() {}
	
	public Stopwatch(Consumer<Stopwatch> stopCallback) {
		this.stopCallback = stopCallback;
	}
	
	public void start() {
		startNano = System.nanoTime();
		startMs = System.currentTimeMillis();
	}
	
	public void stop() {
		stopNano = System.nanoTime();
		stopMs = System.currentTimeMillis();
		if (stopCallback != null) {
			stopCallback.accept(this);
		}
	}
	
	public long elapsedNano() {
		return stopNano - startNano;
	}
	
	public long elapsedMillis() {
		return stopMs - startMs;
	}
	
	@Override
	public void close() throws Exception {
		stop();
	}
	
}
