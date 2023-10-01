package fr.nekotine.core.text.tree;

import java.util.LinkedList;
import java.util.List;

import fr.nekotine.core.text.MessageModule;
import net.kyori.adventure.text.Component;

public abstract class TreeElement{
	protected LinkedList<Enum<?>> styles;
	
	//
	
	public TreeElement addStyle(Enum<?>... styleNames) {
		for(int i=0 ; i < styleNames.length ; i++) {
			styles.addFirst(styleNames[i]);
		}
		return this;
	}
	
	//
	
	public abstract List<Component> build(MessageModule module);
}
