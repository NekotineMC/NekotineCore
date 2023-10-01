package fr.nekotine.core.text;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.text.style.MessageStyle;
import fr.nekotine.core.text.style.Styles;
import fr.nekotine.core.text.tree.Leaf;
import fr.nekotine.core.text.tree.Node;
import fr.nekotine.core.text.tree.TreeElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MessageModule extends PluginModule{
	public class Builder {
		private TreeElement root;
		private MessageModule module;
		public Builder(TreeElement root, MessageModule module) {
			this.root = root;
			this.module = module;
		}
		public List<Component> build() {
			return root.build(module);
		}
		public Component buildFirst() {
			List<Component> built = build();
			if(built.isEmpty()) return Component.empty();
			return built.get(0);
		}
	}
	
	//
	
	private HashMap<Enum<?>, MessageStyle> styles;
	
	//
	
	public MessageModule() {
		styles = new HashMap<Enum<?>, MessageStyle>();
		
		registerStyle(Styles.EMPTY, MessageStyle.build());
		registerStyle(Styles.STANDART, MessageStyle.build(TagResolver.standard()));
		registerStyle(Styles.NEKOTINE, MessageStyle.build(TagResolver.standard()));
	}
	
	//
	
	public boolean registerStyle(Enum<?> key, MessageStyle value) {
		if(styles.containsKey(key)) return false;
		styles.put(key, value);
		return true;
	}
	@Nullable
	public MessageStyle getStyle(Enum<?> key) {
		MessageStyle style = styles.get(key);
		if(style==null) return MessageStyle.build();
		return style;
	}
	public Builder message(Node node) {
		return new Builder(node, this);
	}
	public Builder message(Leaf leaf) {
		return new Builder(leaf, this);
	}
	
	//
	
	public MessageStyle asMerged(LinkedList<Enum<?>> styles) {
		MessageStyle merged = MessageStyle.build();
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
