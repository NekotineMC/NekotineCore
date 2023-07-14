package fr.nekotine.core.tuple;

public record Pair<A,B>(A a, B b){
	
	public static final <A,B> Pair<A,B> from(A a, B b){
		return new Pair<A,B>(a,b);
	}
	
}
