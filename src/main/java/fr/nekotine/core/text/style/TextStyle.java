package fr.nekotine.core.text.style;

import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TextStyle {
	private MiniMessage mm;
	private TagResolver resolver;
	private boolean changed;
	
	//
	public static TextStyle build(@Nullable TagResolver... resolvers) {
		return new TextStyle(resolvers);
	}
	public TextStyle(@Nullable TagResolver... resolvers) {
		this.resolver = TagResolver.empty();
		this.changed = true;
		if(resolvers != null) 
			addTagResolver(resolvers);
	}
	
	//
	
	public void setResolver(TagResolver resolver) {
		this.changed = true;
		this.resolver = resolver;
	}
	public void addTagResolver(TagResolver... resolvers) {
		this.changed = true;
		this.resolver = TagResolver.builder()
			.resolver(this.resolver)
			.resolvers(resolvers)
		.build();
		
	}
	public void addTagResolver(String name, Tag tag) {
		addTagResolver(TagResolver.resolver(name, tag));
	}
	
	//
	
	public Component deserialize(String text) {
		if(changed)
			mm = MiniMessage.builder().tags(resolver).build();
		changed = false;
		return mm.deserialize(text);
	}
	
	//
	
	public TagResolver getResolver() {
		return resolver;
	}
}
