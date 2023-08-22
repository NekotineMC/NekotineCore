package fr.nekotine.core.game.phase;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import fr.nekotine.core.game.phase.eventargs.PhaseFailureEventArgs;

public abstract class CollectionPhase<T> extends Phase implements IPhase{
	
	private Supplier<Stream<T>> source;
	
	@Override
	public final void handleSetup() {
		globalSetup();
		source.get().forEach(this::itemSetup);
	}

	@Override
	public final void handleTearDown() {
		source.get().forEach(this::itemTearDown);
		globalTearDown();
	}
	
	public CollectionPhase() {};
	
	public CollectionPhase(Runnable onSuccess, Consumer<PhaseFailureEventArgs> onFailure, Supplier<Stream<T>> source) {
		super(onSuccess, onFailure);
		this.source = source;
	}
	
	public CollectionPhase(Runnable onSuccess, Consumer<PhaseFailureEventArgs> onFailure) {
		super(onSuccess, onFailure);
	}
	
	public CollectionPhase(Runnable onSuccess) {
		super(onSuccess);
	}
	
	public CollectionPhase(Consumer<PhaseFailureEventArgs> onFailure) {
		super(onFailure);
	}
	
	public void setItemStreamSupplier(Supplier<Stream<T>> supplier) {
		source = supplier;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	protected abstract void globalSetup();
	
	protected abstract void globalTearDown();
	
	public abstract void itemSetup(T item);
	
	public abstract void itemTearDown(T item);
}
