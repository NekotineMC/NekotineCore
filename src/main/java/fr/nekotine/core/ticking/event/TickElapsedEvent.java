package fr.nekotine.core.ticking.event;

import java.util.Set;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.nekotine.core.ticking.TickTimeStamp;

public class TickElapsedEvent extends Event{

	private static final HandlerList handlers = new HandlerList();
	
	Set<TickTimeStamp> _timeStamps;
	
	public TickElapsedEvent(Set<TickTimeStamp> timeStamps) {
		_timeStamps = timeStamps;
	}
	
    public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    public boolean timeStampReached(TickTimeStamp stamp) {
    	return _timeStamps.contains(stamp);
    }

}
