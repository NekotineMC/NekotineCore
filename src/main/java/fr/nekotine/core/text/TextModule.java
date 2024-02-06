package fr.nekotine.core.text;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.text.style.NekotineStyles;
import fr.nekotine.core.text.style.TextStyle;
import fr.nekotine.core.text.tree.Leaf;
import fr.nekotine.core.text.tree.Node;
import fr.nekotine.core.text.tree.TreeElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TextModule implements IPluginModule{
	public class Builder {
		private TreeElement root;
		private TextModule module;
		public Builder(TreeElement root, TextModule module) {
			this.root = root;
			this.module = module;
		}
		public <T> List<Component> build() {
			return root.build(module, null);
		}
		public <T> List<Component> build(@Nullable T resolveData) {
			return root.build(module, resolveData);
		}
		public <T> Component buildFirst() {
			List<Component> built = build(null);
			if(built.isEmpty()) return Component.empty();
			return built.get(0);
		}
		public <T> Component buildFirst(@Nullable T resolveData) {
			List<Component> built = build(resolveData);
			if(built.isEmpty()) return Component.empty();
			return built.get(0);
		}
	}
	
	//
	
	private HashMap<Enum<?>, TextStyle> styles;
	
	//
	
	public TextModule() {
		styles = new HashMap<Enum<?>, TextStyle>();
		registerStyle(NekotineStyles.EMPTY, TextStyle.build());
		registerStyle(NekotineStyles.STANDART, TextStyle.build(TagResolver.standard()));
		registerStyle(NekotineStyles.NEKOTINE, TextStyle.build(TagResolver.standard()));
	}
	
	//
	
	@Override
	public void unload() {
	}
	
	public boolean registerStyle(Enum<?> key, TextStyle value) {
		if(styles.containsKey(key)) return false;
		styles.put(key, value);
		return true;
	}
	@Nullable
	public TextStyle getStyle(Enum<?> key) {
		TextStyle style = styles.get(key);
		if(style==null) return TextStyle.build();
		return style;
	}
	public Builder message(Node node) {
		return new Builder(node, this);
	}
	public Builder message(Leaf leaf) {
		return new Builder(leaf, this);
	}
	
	//
	
	public TextStyle asMerged(LinkedList<Enum<?>> styles) {
		TextStyle merged = TextStyle.build();
		TagResolver[] resolvers = styles.stream().map(new Function<Enum<?>, TagResolver>() {
			@Override
			public TagResolver apply(Enum<?> style) {
				return getStyle(style).getResolver();
			}
		}).toList().toArray(new TagResolver[0]);
		merged.addTagResolver(resolvers);
		return merged;
	}
}
