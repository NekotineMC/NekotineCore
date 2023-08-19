package fr.nekotine.core.game.team;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;

public class Team implements ForwardingAudience, Collection<Player>{
	
	private final List<Player> players = new LinkedList<>();
	
	private Consumer<Player> playerAddCallback;
	
	private Consumer<Player> playerRemoveCallback;
	
	public void setPlayerAddCallback(Consumer<Player> callback) {
		playerAddCallback = callback;
	}
	
	public void setPlayerRemoveCallback(Consumer<Player> callback) {
		playerRemoveCallback = callback;
	}
	
	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return players;
	}

	@Override
	public int size() {
		return players.size();
	}

	@Override
	public boolean isEmpty() {
		return players.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return players.contains(o);
	}

	@Override
	public Iterator<Player> iterator() {
		return players.iterator();
	}

	@Override
	public Object[] toArray() {
		return players.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return players.toArray(a);
	}

	@Override
	public boolean add(Player e) {
		if (playerAddCallback != null) {
			playerAddCallback.accept(e);
		}
		return players.add(e);
	}

	@Override
	public boolean remove(Object o) {
		if (players.remove(o) && playerRemoveCallback != null && o instanceof Player p) {
			playerRemoveCallback.accept(p);
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return players.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Player> c) {
		if (playerAddCallback != null) {
			var changed = false;
			for (var item : c) {
				playerAddCallback.accept(item);
				changed |= players.add(item);
			}
			return changed;
		}else {
			return players.addAll(c);
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (playerRemoveCallback != null) {
			var changed = false;
			for (var item : c) {
				if (item instanceof Player p && players.remove(item)) {
					playerRemoveCallback.accept(p);
					changed = true;
				}
			}
			return changed;
		}else {
			return players.removeAll(c);
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (playerRemoveCallback != null) {
			for (var item : c) {
				if (item instanceof Player p) {
					playerRemoveCallback.accept(p);
				}
			}
		}
		return players.retainAll(c);
	}

	@Override
	public void clear() {
		if (playerRemoveCallback != null) {
			players.forEach(playerRemoveCallback::accept);
		}
		players.clear();
	}
}
