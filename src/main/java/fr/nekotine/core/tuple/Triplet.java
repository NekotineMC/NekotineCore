package fr.nekotine.core.tuple;

public record Triplet<A,B,C>(A a, B b, C c){
	
	public static final <A,B,C> Triplet<A,B,C> from(A a, B b, C c){
		return new Triplet<A,B,C>(a,b,c);
	}
	
}
