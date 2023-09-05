package fr.nekotine.core.state;

public interface ItemState<TItem> {

	public void setup(TItem item);
	
	public void teardown(TItem item);
	
}
