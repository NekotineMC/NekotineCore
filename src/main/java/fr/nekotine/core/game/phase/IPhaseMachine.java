package fr.nekotine.core.game.phase;

import java.util.function.Function;

public interface IPhaseMachine {
	
	@SuppressWarnings("rawtypes")
	public <P extends Phase,T extends IPhase<P>> void registerPhase(Class<T> type, Function<IPhaseMachine,T> phaseSupplier);
	
	@SuppressWarnings("rawtypes")
	public <P extends Phase, T extends IPhase<P>> void goTo(Class<T> phase, Object inputData);
	
	public void end();
	
	public <T> T getPhase(Class<T> phaseType);
	
	public boolean isRunning();
	
	public <P> void onPhaseComplete(IPhase<P> phase, Object outData);
	
}
