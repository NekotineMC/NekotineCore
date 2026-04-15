package fr.nekotine.core.text.placeholder;

import fr.nekotine.core.tuple.Pair;
import java.util.List;

public interface TextPlaceholder {
	public <T> List<Pair<String, String>> resolve(T resolveData);
}
