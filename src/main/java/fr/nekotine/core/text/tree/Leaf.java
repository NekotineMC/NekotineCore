package fr.nekotine.core.text.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.nekotine.core.text.MessageModule;
import fr.nekotine.core.text.style.MessageStyle;
import net.kyori.adventure.text.Component;

public class Leaf extends TreeElement{
	private LinkedList<String> lines;
	
	//

	public Leaf() {
		lines = new LinkedList<String>();
	}
	public static Leaf builder() {
		return new Leaf();
	}
	
	//
	
	public Leaf addLine(String... lines) {
		for(int i=0 ; i<lines.length ; i++)
			this.lines.add(lines[i]);
		return this;
	}
	public Leaf addStyle(Enum<?>... styleNames) {
		super.addStyle(styleNames);
		return this;
	}
	
	//
	
	@Override
	public List<Component> build(MessageModule module){
		MessageStyle style = module.asMerged(super.styles);
		List<Component> components = new ArrayList<Component>();
		Iterator<String> lines_iter = lines.iterator();
		while(lines_iter.hasNext()) {
			components.add(style.deserialize(lines_iter.next()));
		}
		return components;
	}
}
