package fr.nekotine.core.text;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public interface MessagePlaceholder {
	
	public ComponentLike resolve(String tag);
	
}
