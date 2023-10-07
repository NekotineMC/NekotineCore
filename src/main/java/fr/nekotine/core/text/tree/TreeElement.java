package fr.nekotine.core.text.tree;

import java.util.LinkedList;
import java.util.List;

import fr.nekotine.core.text.TextModule;
import fr.nekotine.core.text.placeholder.TextPlaceholder;
import net.kyori.adventure.text.Component;

public abstract class TreeElement{
	private LinkedList<Enum<?>> styles;
	private LinkedList<TextPlaceholder> placeholders;
	
	//
	
	public TreeElement() {
		styles = new LinkedList<Enum<?>>();
		placeholders = new LinkedList<TextPlaceholder>();
	}
	
	//
	
	public LinkedList<Enum<?>> getStyles(){
		return styles;
	}
	public TreeElement addStyle(Enum<?>... styleNames) {
		for(Enum<?> style : styles) 
			styles.addFirst(style);
		return this;
	}
	public LinkedList<TextPlaceholder> getPlaceholders(){
		return placeholders;
	}
	public TreeElement addPlaceholder(TextPlaceholder... holders) {
		for(TextPlaceholder holder : holders)
			placeholders.addFirst(holder);
		return this;
	}
	
	//
	
	public abstract List<Component> build(TextModule module);
}
