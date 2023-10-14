package fr.nekotine.core.text.placeholder;

import java.util.ArrayList;

import fr.nekotine.core.tuple.Pair;
import net.kyori.adventure.text.ComponentLike;

public interface TextPlaceholder {
	
	public ArrayList<Pair<String,ComponentLike>> resolve();
	
}
