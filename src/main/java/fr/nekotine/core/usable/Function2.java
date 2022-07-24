package fr.nekotine.core.usable;

@FunctionalInterface
public interface Function2<A, B> {
	void run(A a, B b);
}
