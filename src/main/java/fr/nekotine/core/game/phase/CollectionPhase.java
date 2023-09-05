package fr.nekotine.core.game.phase;

import java.util.Collections;
import java.util.List;

import fr.nekotine.core.exception.ExceptionCollector;
import fr.nekotine.core.state.ItemState;
import fr.nekotine.core.util.collection.ObservableCollection;

public abstract class CollectionPhase<P, T> extends Phase<P>{
	
	private final List<ItemState<T>> composingItemStates = makeAppliedItemStates();
	
	private ObservableCollection<T> itemCollection = getItemCollection();
	
	public CollectionPhase(IPhaseMachine machine) {
		super(machine);
	}

	@Override
	public final void handleSetup(Object inputData) {
		var collector = new ExceptionCollector();
		try {
			globalSetup(inputData);
		}catch(Exception e) {
			collector.collect(e);
		}
		for (var item : itemCollection) {
			for (var state : composingItemStates) {
				try {
					state.setup(item);
				}catch(Exception e) {
					collector.collect(new RuntimeException("Une erreur est survenue lors du setup des etats d'item composants "+getClass(),e));
				}
			}
			try {
				itemSetup(item);
			}catch(Exception e) {
				collector.collect(new RuntimeException("Une erreur est survenue lors du setup des item composants "+getClass(),e));
			}
		}
		itemCollection.addAdditionCallback(this::itemSetup);
		itemCollection.addSuppressionCallback(this::objectTearDown);
		collector.throwAsRuntime("Une erreur est survenue lors du setup des etats composants "+getClass());
	}

	@Override
	public final void handleTearDown() {
		var collector = new ExceptionCollector();
		itemCollection.removeAdditionCallback(this::itemSetup);
		itemCollection.removeSuppressionCallback(this::objectTearDown);
		for (var item : itemCollection) {
			try {
				itemTearDown(item);
			}catch(Exception e) {
				collector.collect(new RuntimeException("Une erreur est survenue lors du setup des items composants "+getClass(),e));
			}
			for (var state : composingItemStates) {
				try {
					state.teardown(item);
				}catch(Exception e) {
					collector.collect(new RuntimeException("Une erreur est survenue lors du setup des etats d'item composants "+getClass(),e));
				}
			}
		}
		collector.collect(this::globalTearDown);
		collector.throwAsRuntime("Une erreur est survenue lors du teardown des etats composants "+getClass());
	}
	
	public abstract ObservableCollection<T> getItemCollection();
	
	protected abstract void globalSetup(Object inputData);
	
	protected abstract void globalTearDown();
	
	public abstract void itemSetup(T item);
	
	public abstract void itemTearDown(T item);
	
	@SuppressWarnings("unchecked")
	private void objectTearDown(Object o) {
		itemTearDown((T)o);
	}
	
	protected List<ItemState<T>> makeAppliedItemStates(){
		return Collections.emptyList();
	}
}
