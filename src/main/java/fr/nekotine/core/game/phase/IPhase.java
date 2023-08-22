package fr.nekotine.core.game.phase;

import fr.nekotine.core.game.phase.eventargs.PhaseFailureEventArgs;

public interface IPhase {

	public void setup();
	
	public void tearDown();
	
	public void complete();
	
	public void abort(String info, Exception e);
	
	public void cancel(String info, Exception e);
	
	public void abort(PhaseFailureEventArgs args);
	
	public void cancel(PhaseFailureEventArgs args);
	
	public boolean isRunning();
	
}
