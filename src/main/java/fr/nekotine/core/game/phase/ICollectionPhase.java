package fr.nekotine.core.game.phase;

public interface ICollectionPhase<T> {

	public void globalSetup();
	
	public void globalTearDown();
	
	public void itemSetup(T item);
	
	public void itemTearDown(T item);
	
}
