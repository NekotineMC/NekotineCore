package fr.nekotine.core.util.lambda;

@FunctionalInterface
public interface TriConsumer<A,B,C>{

	void accept(A a, B b, C c);
	
}
