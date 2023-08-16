package fr.nekotine.core.game.phase;

import java.util.function.Consumer;

import fr.nekotine.core.game.phase.eventargs.PhaseFailureEventArgs;
import fr.nekotine.core.game.phase.reason.PhaseFailureType;

public abstract class Phase implements IPhase{

	private final Runnable onSuccess;
	
	private final Consumer<PhaseFailureEventArgs> onFailure;
	
	public Phase(Runnable onSuccess, Consumer<PhaseFailureEventArgs> onFailure) {
		this.onSuccess = onSuccess;
		this.onFailure = onFailure;
	}
	
	@Override
	public final void complete() {
		tearDown();
		onSuccess.run();
	}
	
	@Override
	public final void abort(String info, Exception e) {
		tearDown();
		onFailure.accept(new PhaseFailureEventArgs(PhaseFailureType.ABORTED, info, e));
	}
	
	@Override
	public void cancel(String info, Exception e) {
		handleCancelation();
		tearDown();
		onFailure.accept(new PhaseFailureEventArgs(PhaseFailureType.CANCELED, info, e));
	}
	
	protected void handleCancelation() {
	}
	
}
