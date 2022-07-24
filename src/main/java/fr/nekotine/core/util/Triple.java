package fr.nekotine.core.util;

public class Triple <A,B,C> {
	
	private final A _a;
	private final B _b;
	private final C _c;
	
	public Triple(A a, B b, C c) {
		_a = a;
		_b = b;
		_c = c;
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public boolean equals(Object o)
    {
        /* Checks specified object is "equal to" the current object or not */
 
        if (this == o) {
            return true;
        }
 
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
 
        Triple<A,B,C> triple = (Triple<A,B,C>) o;
 
        // call `equals()` method of the underlying objects
        if (!_a.equals(triple._a) ||
                !_b.equals(triple._b) ||
                !_c.equals(triple._c)) {
            return false;
        }
 
        return true;
    }
 
    @Override
    public int hashCode()
    {
        /* Computes hash code for an object by using hash codes of
        the underlying objects */
 
        int result = _a.hashCode();
        result = 31 * result + _b.hashCode();
        result = 31 * result + _c.hashCode();
        return result;
    }
 
    @Override
    public String toString() {
        return "(" + _a + ", " + _b + ", " + _c + ")";
    }
	
    public A getA() {
    	return _a;
    }
    
    public B getB() {
    	return _b;
    }
    
    public C getC() {
    	return _c;
    }
}
