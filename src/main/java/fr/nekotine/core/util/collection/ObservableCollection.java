package fr.nekotine.core.util.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.nekotine.core.logging.NekotineLogger;

public class ObservableCollection<T> implements Collection<T>{

	private final List<Consumer<T>> itemAddCallbacks = new LinkedList<>();
	
	private final List<Consumer<Object>> itemRemoveCallbacks = new LinkedList<>();
	
	private final Logger logger = new NekotineLogger(getClass());
	
	private Collection<T> inner;
	
	public static final <T> ObservableCollection<T> wrap(Collection<T> collection){
		return new ObservableCollection<>(collection);
	}
	
	protected ObservableCollection() {}
	
	protected ObservableCollection(Collection<T> innerCollection) {
		inner = innerCollection;
	}
	
	protected void setInnerCollection(Collection<T> collection) {
		inner = collection;
	}
	
	public boolean addAdditionCallback(Consumer<T> callback) {
		return itemAddCallbacks.add(callback);
	}
	
	public boolean removeAdditionCallback(Consumer<T> callback) {
		return itemAddCallbacks.remove(callback);
	}
	
	public boolean addSuppressionCallback(Consumer<Object> callback) {
		return itemRemoveCallbacks.add(callback);
	}
	
	public boolean removeSuppressionCallback(Consumer<Object> callback) {
		return itemRemoveCallbacks.remove(callback);
	}

	@Override
	public int size() {
		return inner.size();
	}

	@Override
	public boolean isEmpty() {
		return inner.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return inner.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return inner.iterator();
	}

	@Override
	public Object[] toArray() {
		return inner.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return inner.toArray(a);
	}

	@Override
	public boolean add(T e) {
		var result = inner.add(e);
		if (result) {
			for (var cb : itemAddCallbacks) {
				try {
					cb.accept(e);
				}catch(Exception ex) {
					logger.log(Level.SEVERE, "Une erreur c'est produite dans une callback d'ObservableCollection.add", ex);
				}
			}
		}
		return result;
	}

	@Override
	public boolean remove(Object o) {
		var result = inner.remove(o);
		if (result) {
			var ite = itemRemoveCallbacks.listIterator(itemRemoveCallbacks.size());
			while (ite.hasPrevious()) {
				try {
					ite.previous().accept(o);
				}catch(Exception ex) {
					logger.log(Level.SEVERE, "Une erreur c'est produite dans une callback d'ObservableCollection.remove", ex);
				}
			}
		}
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return inner.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		var changed = false;
		for (var e : c) {
			if (add(e)) {
				changed = true;
				for (var cb : itemAddCallbacks) {
					try {
						cb.accept(e);
					}catch(Exception ex) {
						logger.log(Level.SEVERE, "Une erreur c'est produite dans une callback d'ObservableCollection.addAll", ex);
					}
				}
			}
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		var changed = false;
		for (var e : c) {
			if (remove(e)) {
				changed = true;
				var ite = itemRemoveCallbacks.listIterator(itemRemoveCallbacks.size());
				while (ite.hasPrevious()) {
					try {
						ite.previous().accept(e);
					}catch(Exception ex) {
						logger.log(Level.SEVERE, "Une erreur c'est produite dans une callback d'ObservableCollection.removeAll", ex);
					}
				}
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Pas implémenté");
	}

	@Override
	public void clear() {
		for (var e : inner) {
			var ite = itemRemoveCallbacks.listIterator(itemRemoveCallbacks.size());
			while (ite.hasPrevious()) {
				try {
					ite.previous().accept(e);
				}catch(Exception ex) {
					logger.log(Level.SEVERE, "Une erreur c'est produite dans une callback d'ObservableCollection.clear", ex);
				}
			}
		}
		inner.clear();
	}
	
}
