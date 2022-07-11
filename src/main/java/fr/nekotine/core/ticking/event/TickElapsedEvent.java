package fr.nekotine.core.ticking.event;

import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.nekotine.core.ticking.TickTimeStamp;

public class TickElapsedEvent extends Event{

	private static final HandlerList handlers = new HandlerList();
	
	Map<TickTimeStamp, Boolean> _timeStamps;
	
	public TickElapsedEvent(Map<TickTimeStamp, Boolean> timeStamps) {
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
    
    public boolean IsTimeStampReached(TickTimeStamp stamp) {
    	return _timeStamps.get(stamp);
    }

}
