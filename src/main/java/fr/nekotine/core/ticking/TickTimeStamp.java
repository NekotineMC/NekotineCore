package fr.nekotine.core.ticking;

public enum TickTimeStamp {

	QuartSecond(5),
	HalfSecond(10),
	Second(20); // Une seconde = 20 tick server (en théorie)
	
	private int _numberOfTick;
	
	private TickTimeStamp(int numberOfTick) {
		_numberOfTick = numberOfTick;
	}
	
	public int getNumberOfTick() {
		return _numberOfTick;
	}
	
}
