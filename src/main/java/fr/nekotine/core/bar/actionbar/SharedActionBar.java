package fr.nekotine.core.bar.actionbar;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.nekotine.core.ticking.event.TickElapsedEvent;
import fr.nekotine.core.util.EventUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class SharedActionBar implements Listener{
	private Set<Player> viewers = new HashSet<Player>();
	private List<ActionBarComponent> components = new LinkedList<ActionBarComponent>();
	private boolean rebuild = false;
	private boolean resort = false;
	private boolean isEmpty = true;
	private Component lastBuilt = Component.empty();
	private int refreshFrequencyTick = 1;
	private int tickCount = 0;
	public SharedActionBar(Player... viewers) {
		EventUtil.register(this);
		addViewers(viewers);
	}
	public void addViewers(Player... viewers) {
		for(Player viewer : viewers) {
			this.viewers.add(viewer);
		}
	}
	public void removeViewers(Player... viewers) {
		for(Player viewer : viewers) {
			this.viewers.remove(viewer);
		}
	}
	public void addComponent(ActionBarComponent component) {
		if(!components.add(component)) return;
		component.addActionBar(this);
		scheduleSort();
		scheduleBuild();
	}
	public void addComponents(ActionBarComponent... components) {
		for(ActionBarComponent component : components) {
			addComponent(component);
		}
	}
	public void removeComponent(ActionBarComponent component) {
		if(!components.remove(component)) return;
		component.removeActionBar(this);
		scheduleBuild();
	}
	public Optional<ActionBarComponent> getComponent(int priority) {
		return components.stream().filter(c -> c.getPriority()==priority).findAny();
	}
	public void scheduleSort() {
		resort = true;
	}
	public void scheduleBuild() {
		rebuild = true;
	}
	public void setRefreshFrequency(int refreshFrequencyTick) {
		this.refreshFrequencyTick = refreshFrequencyTick;
	}
	
	//
	
	private void build() {
		lastBuilt = Component.empty();
		components.forEach(c -> lastBuilt = lastBuilt.append(c.getText(this)));
		isEmpty = !(TextComponent.IS_NOT_EMPTY.test(lastBuilt));
	}
	private void sort() {
		Collections.sort(components);
	}
	@EventHandler
	private void onTick(TickElapsedEvent evt) {
		if(++tickCount>=refreshFrequencyTick) {
			tickCount = 0;
			if(resort) {
				resort = false;
				sort();
			}
			if(rebuild) {
				rebuild = false;
				build();
			}
			if(isEmpty) {
				return;
			}
			viewers.forEach(p -> p.sendActionBar(lastBuilt));
		}
	}
}
