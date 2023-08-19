package fr.nekotine.core.game.phase;

import java.util.function.Consumer;

import fr.nekotine.core.game.phase.eventargs.PhaseFailureEventArgs;
import fr.nekotine.core.game.phase.reason.PhaseFailureType;

public abstract class Phase implements IPhase{

	private Runnable onSuccess = null;
	
	private Consumer<PhaseFailureEventArgs> onFailure = null;
	
	public Phase() {
	}
	
	public Phase(Runnable onSuccess) {
		this.onSuccess = onSuccess;
	}
	
	public Phase(Consumer<PhaseFailureEventArgs> onFailure) {
		this.onFailure = onFailure;
	}
	
	public Phase(Runnable onSuccess, Consumer<PhaseFailureEventArgs> onFailure) {
		this.onSuccess = onSuccess;
		this.onFailure = onFailure;
	}
	
	public void setOnSuccessCallback(Runnable callback) {
		onSuccess = callback;
	}
	
	public void setOnFailureCallback(Consumer<PhaseFailureEventArgs> callback) {
		onFailure = callback;
	}
	
	@Override
	public final void complete() {
		tearDown();
		postSuccess();
		if (onSuccess != null) {
			onSuccess.run();
		}
	}
	
	@Override
	public final void abort(String info, Exception e) {
		tearDown();
		if (onFailure != null) {
			onFailure.accept(new PhaseFailureEventArgs(PhaseFailureType.ABORTED, info, e));
		}
	}
	
	@Override
	public void cancel(String info, Exception e) {
		handleCancelation();
		tearDown();
		if (onFailure != null) {
			onFailure.accept(new PhaseFailureEventArgs(PhaseFailureType.CANCELED, info, e));
		}
	}
	
	@Override
	public final void abort(PhaseFailureEventArgs args) {
		tearDown();
		if (onFailure != null) {
			onFailure.accept(new PhaseFailureEventArgs(PhaseFailureType.ABORTED, args.info(), args.exception()));
		}
	}
	
	@Override
	public void cancel(PhaseFailureEventArgs args) {
		handleCancelation();
		tearDown();
		if (onFailure != null) {
			onFailure.accept(new PhaseFailureEventArgs(PhaseFailureType.CANCELED, args.info(), args.exception()));
		}
	}
	
	protected void handleCancelation() {};
	
	protected void postSuccess() {};
	
}
