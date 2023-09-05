package fr.nekotine.core.game.phase;

import java.util.Collections;
import java.util.List;

import fr.nekotine.core.exception.ExceptionCollector;
import fr.nekotine.core.state.State;

public abstract class Phase<TParent> implements IPhase<TParent> {

	private final IPhaseMachine machine;
	
	private TParent parent;
	
	private final List<State> composingStates = makeAppliedStates();
	
	public Phase(IPhaseMachine machine) {
		this.machine = machine;
	}
	
	@Override
	public void setup(Object inputData) {
		var collector = new ExceptionCollector();
		for (var s : composingStates) {
			try {
				s.setup();
			}catch(Exception e) {
				collector.collect(e);
			}
		}
		try {
			handleSetup(inputData);
		}catch(Exception e) {
			collector.collect(e);
		}
		collector.throwAsRuntime("Une erreur est survenue lors du setup des etats composants "+getClass());
	}

	@Override
	public void tearDown() {
		var collector = new ExceptionCollector();
		collector.collect(this::handleTearDown);
		for (var s : composingStates) {
			try {
				s.teardown();
			}catch(Exception e) {
				collector.collect(e);
			}
		}
		collector.throwAsRuntime("Une erreur est survenue lors du teardown des etats composants "+getClass());
	}

	@Override
	public void complete() {
		machine.onPhaseComplete(this, handleComplete());
	}
	
	@Override
	public IPhaseMachine getMachine() {
		return machine;
	}
	
	@Override
	public TParent getParent() {
		return parent;
	}
	
	@Override
	public void setParent(TParent parent) {
		this.parent = parent;
	}
	
	protected abstract void handleSetup(Object inputData);
	
	protected abstract void handleTearDown();
	
	protected Object handleComplete() {
		return null;
	}
	
	protected List<State> makeAppliedStates(){
		return Collections.emptyList();
	}
	
}
