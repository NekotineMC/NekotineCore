package fr.nekotine.core.text.style;

import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MessageStyle {
	private MiniMessage mm;
	private TagResolver resolver;
	
	//
	public static MessageStyle build(@Nullable TagResolver... resolvers) {
		return new MessageStyle(resolvers);
	}
	public MessageStyle(@Nullable TagResolver... resolvers) {
		if(resolver == null) resolver = TagResolver.empty();
		setResolver(resolver);
	}
	
	//
	
	public void setResolver(TagResolver resolver) {
		this.resolver = resolver;
		buildMM();
	}
	public void addTagResolver(TagResolver... resolvers) {
		this.resolver = TagResolver.builder()
			.resolver(this.resolver)
			.resolvers(resolvers)
		.build();
		buildMM();
	}
	public void addTagResolver(String name, Tag tag) {
		addTagResolver(TagResolver.resolver(name, tag));
	}
	
	//
	
	public Component deserialize(String text) {
		return mm.deserialize(text);
	}
	
	//
	
	public TagResolver getResolver() {
		return resolver;
	}
	
	//
	
	private void buildMM() {
		mm = MiniMessage.builder().tags(resolver).build();
	}
}
