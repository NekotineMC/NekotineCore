package fr.nekotine.core.bar.actionbar;

import java.util.ArrayList;
import java.util.Collection;

import net.kyori.adventure.text.Component;

public class ActionBarComponent implements Comparable<ActionBarComponent>{
	private Collection<SharedActionBar> actionBars = new ArrayList<SharedActionBar>();
	private int priority = 0;
	private Component text = Component.empty();
	public ActionBarComponent(){}
	public ActionBarComponent(Component text, int priority) {
		setPriority(priority);
		setText(text);	
	}
	
	//
	
	public Component getText(SharedActionBar bar) {
		return text;
	}
	public void setText(Component text) {
		this.text = text;
		actionBars.forEach(ab -> ab.scheduleBuild());
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
		actionBars.forEach(ab -> ab.scheduleSort());
	}
	public void clearText() {
		setText(Component.empty());
	}
	
	//
	
	protected void addActionBar(SharedActionBar actionBar) {
		actionBars.add(actionBar);
	}
	protected void removeActionBar(SharedActionBar actionBar) {
		actionBars.remove(actionBar);
	}
	
	//
	
	@Override
	public int compareTo(ActionBarComponent o) {
		return Integer.compare(o.getPriority(), priority);
	}
}
