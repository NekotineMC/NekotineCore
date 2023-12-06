package fr.nekotine.core.text.placeholder;

import java.util.List;

import fr.nekotine.core.tuple.Pair;

public interface TextPlaceholder {	
	public List<Pair<String,String>> resolve();
}
