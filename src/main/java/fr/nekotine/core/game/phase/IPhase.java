package fr.nekotine.core.game.phase;

public interface IPhase<TParent> {

	public void setup(Object inputData);
	
	public void tearDown();
	
	public void complete();
	
	public Class<TParent> getParentType();
	
	public TParent getParent();
	
	public void setParent(TParent parent);
	
	public IPhaseMachine getMachine();
	
}
