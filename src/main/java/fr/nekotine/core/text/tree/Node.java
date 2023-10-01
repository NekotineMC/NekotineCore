package fr.nekotine.core.text.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.nekotine.core.text.MessageModule;
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
	public Node flatten() {
		flatten = true;
		return this;
	}
	
	//
	
	private void addStylesToChildren() {
		Iterator<TreeElement> childs_iter = childs.iterator();
		while(childs_iter.hasNext()) {
			TreeElement child = childs_iter.next();
			Iterator<Enum<?>> styles_iter = super.styles.iterator();
			while(styles_iter.hasNext()) {
				child.addStyle(styles_iter.next());
			}
		}
	}
	@Override
	public List<Component> build(MessageModule module) {
		addStylesToChildren();
		
		//Recupère les textes des enfants déserialisés
		List<Component> child_compon = new ArrayList<Component>();
		Iterator<TreeElement> childs_iter = childs.iterator();
		while(childs_iter.hasNext()) {
			TreeElement child = childs_iter.next();
			List<Component> child_builded = child.build(module);
			if(child_builded.isEmpty())
				continue;
			
			//Flatten éventuel
			if(flatten) {
				
				if(child_compon.isEmpty()) {
					child_compon.add(child_builded.get(0));
				}else {
					child_builded.get(0).append(child_builded.get(0));
				}
				
			}else {
				child_compon.addAll(child_builded);
			}
		}
		
		return child_compon;
	}
}
