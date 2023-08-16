package fr.nekotine.core.game.phase;

import java.util.function.Consumer;
import java.util.function.Supplier;

import fr.nekotine.core.game.phase.eventargs.PhaseFailureEventArgs;

public abstract class CollectionPhase<T> extends Phase implements IPhase, ICollectionPhase<T>{
	
	private final Supplier<Iterable<T>> source;
	
	@Override
	public final void setup() {
		globalSetup();
		for (var item : source.get()) {
			itemSetup(item);
		}
	}

	@Override
	public final void tearDown() {
		for (var item : source.get()) {
			itemTearDown(item);
		}
		globalTearDown();
	}
	
	public CollectionPhase(Runnable onSuccess, Consumer<PhaseFailureEventArgs> onFailure, Supplier<Iterable<T>> source) {
		super(onSuccess, onFailure);
		this.source = source;
	}
}
