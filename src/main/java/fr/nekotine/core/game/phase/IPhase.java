package fr.nekotine.core.game.phase;

public interface IPhase {

	public void setup();
	
	public void tearDown();
	
	public void complete();
	
	public void abort(String info, Exception e);
	
	public void cancel(String info, Exception e);
	
}
