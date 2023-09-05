package fr.nekotine.core.state;

import java.util.function.Consumer;

public record LambdaItemState<TItem>(Consumer<TItem> setupLambda, Consumer<TItem> teardownLambda) implements ItemState<TItem>{

	
	
	@Override
	public void setup(TItem item) {
		setupLambda.accept(item);
	}

	@Override
	public void teardown(TItem item) {
		teardownLambda.accept(item);
	}
	
}
