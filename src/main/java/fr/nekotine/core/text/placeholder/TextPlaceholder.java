package fr.nekotine.core.text.placeholder;

import java.util.ArrayList;

import fr.nekotine.core.tuple.Pair;

public interface TextPlaceholder {
	
	public ArrayList<Pair<String,String>> resolve();
	
}
