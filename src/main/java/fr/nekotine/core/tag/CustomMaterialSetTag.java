package fr.nekotine.core.tag;

import java.util.LinkedList;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.bukkit.Material;

import com.destroystokyo.paper.MaterialSetTag;

import fr.nekotine.core.tuple.Pair;

public class CustomMaterialSetTag<T>{
	private LinkedList<Pair<Material, Predicate<T>>> values = new LinkedList<Pair<Material,Predicate<T>>>();
	public CustomMaterialSetTag<T> add(@Nullable Predicate<T> filter, Material... mat) {
		for(int i=0 ; i < mat.length ; i++) {
			values.add(Pair.from(mat[i], filter));
		}
		return this;
	}
	public CustomMaterialSetTag<T> add(@Nullable Predicate<T> filter, MaterialSetTag tag) {
		return add(filter, tag.getValues().toArray(new Material[0]));
	}
	public CustomMaterialSetTag<T> add(Material... mat) {
		return add(null, mat);
	}
	public CustomMaterialSetTag<T> add(MaterialSetTag tag) {
		return add(null, tag);
	}
	public CustomMaterialSetTag<T> add(CustomMaterialSetTag<T> tag) {
		values.addAll(tag.getValues());
		return this;
	}
	public boolean isTagged(Material mat, T data) {
		var it = values.iterator();
		while(it.hasNext()) {
			var entry = it.next();
			if(entry.a() != mat)
				continue;
			if(entry.b() == null || entry.b().test(data))
				return true;
		}
		return false;
	}
	public LinkedList<Pair<Material, Predicate<T>>> getValues(){
		return values;
	}
}
