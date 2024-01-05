package fr.nekotine.core.text.tree;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import fr.nekotine.core.text.TextModule;
import fr.nekotine.core.text.placeholder.TextPlaceholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public abstract class TreeElement{
	private LinkedList<Enum<?>> styles;
	private LinkedList<TagResolver> additionalStyles;
	private LinkedList<TextPlaceholder> placeholders;
	
	//
	
	public TreeElement() {
		styles = new LinkedList<Enum<?>>();
		additionalStyles = new LinkedList<TagResolver>();
		placeholders = new LinkedList<TextPlaceholder>();
	}
	
	//
	
	public LinkedList<Enum<?>> getStyles(){
		return styles;
	}
	public LinkedList<TagResolver> getAddtionalStyles(){
		return additionalStyles;
	}
	public TreeElement addStyle(Enum<?>... styleNames) {
		for(Enum<?> style : styleNames) 
			styles.addFirst(style);
		return this;
	}
	public TreeElement addStyle(TagResolver... styles) {
		for(TagResolver style : styles) 
			additionalStyles.addFirst(style);
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
	
	public abstract <T> List<Component> build(TextModule module, @Nullable T resolveData);
}
