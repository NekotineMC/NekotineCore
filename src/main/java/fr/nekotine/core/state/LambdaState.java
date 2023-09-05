package fr.nekotine.core.state;

public record LambdaState(Runnable setupLambda, Runnable teardownLambda) implements State {

	@Override
	public void setup() {
		setupLambda.run();
	}

	@Override
	public void teardown() {
		teardownLambda.run();
	}
	
}
