package fr.nekotine.core.text.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.nekotine.core.text.TextModule;
import fr.nekotine.core.text.placeholder.TextPlaceholder;
import fr.nekotine.core.text.style.TextStyle;
import net.kyori.adventure.text.Component;

public class Node extends TreeElement{
	private LinkedList<TreeElement> childs;
	private boolean flatten;
	
	//
	
	public Node() {
		childs = new LinkedList<TreeElement>();
		flatten = false;
	}
	public static Node builder() {
		return new Node();
	}
	
	//
	
	private Node addElement(TreeElement child) {
		childs.add(child);
		return this;
	}
	public Node addNode(Node child) {
		return addElement(child);
	}
	public Node addLeaf(Leaf child) {
		return addElement(child);
	}
	public Node addStyle(Enum<?>... styleNames) {
		super.addStyle(styleNames);
		return this;
	}
	public Node addStyle(TextStyle... styles) {
		super.addStyle(styles);
		return this;
	}
	public Node addPlaceholder(TextPlaceholder... holders) {
		super.addPlaceholder(holders);
		return this;
	}
	public Node flatten() {
		flatten = true;
		return this;
	}
	
	//
	
	private void addStylesToChildren() {
		for(TreeElement child : childs) {
			for(Enum<?> style : getStyles()) 
				child.addStyle(style);
			
			for(TextStyle style : getAddtionalStyles())
				child.addStyle(style);
		}	
	}
	private void addPlaceholdersToChildren() {
		for(TreeElement child : childs) 
			for(TextPlaceholder holder : getPlaceholders()) 
			child.addPlaceholder(holder);
	}
	@Override
	public List<Component> build(TextModule module) {
		//Passe ses styles/holders aux enfants
		addStylesToChildren();
		addPlaceholdersToChildren();
		
		//Recupère les textes des enfants déserialisés
		List<Component> child_compon = new ArrayList<Component>();
		for(TreeElement child : childs) {
			List<Component> child_builded = child.build(module);
			if(child_builded.isEmpty())
				continue;
			
			//Flatten éventuel
			if(flatten) {
				
				if(child_compon.isEmpty()) {
					child_compon.add(child_builded.get(0));
				}else {
					child_compon.set(0, 
						child_compon.get(0).append(child_builded.get(0))
					);
				}
				
			}else {
				child_compon.addAll(child_builded);
			}
		}
		
		return child_compon;
	}
}
