package fr.nekotine.core.ticking.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TickElapsedEvent extends Event{

	private static final HandlerList handlers = new HandlerList();
 
    public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList()
    {
        return handlers;
    }

}
