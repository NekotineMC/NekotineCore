package fr.nekotine.core.text.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fr.nekotine.core.text.TextModule;
import fr.nekotine.core.text.placeholder.TextPlaceholder;
import fr.nekotine.core.text.style.TextStyle;
import fr.nekotine.core.tuple.Pair;
import fr.nekotine.core.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class Leaf extends TreeElement{
	private LinkedList<String> lines;
	
	//

	public Leaf() {
		lines = new LinkedList<String>();
	}
	public static Leaf builder() {
		return new Leaf();
	}
	
	//
	
	public Leaf addLine(String... lines) {
		for(String line : lines)
			this.lines.add(line);
		return this;
	}
	public Leaf addStyle(Enum<?>... styleNames) {
		super.addStyle(styleNames);
		return this;
	}
	public Leaf addStyle(TagResolver... styles) {
		super.addStyle(styles);
		return this;
	}
	public Leaf addPlaceholder(TextPlaceholder... holders) {
		super.addPlaceholder(holders);
		return this;
	}
	
	//
	
	private <T> HashMap<String, Pair<Integer,List<String>>> getHoldersMap(T test){
		HashMap<String, Pair<Integer,List<String>>> map = new HashMap<String, Pair<Integer,List<String>>>();
		for(TextPlaceholder holder : getPlaceholders()) {
			
			List<Pair<String,String>> tags = holder.resolve(test);
			for(Pair<String,String> tag : tags) {
				
				String asTag = "<".concat(tag.a()).concat(">");
				
				
				if(!map.containsKey(asTag))
					map.put(asTag, Pair.from(0, new ArrayList<String>()));
				
				//On ajoute le String à l'array
				map.get(asTag).b().add(tag.b());
			}
		}
		return map;
	}
	
	//
	
	@Override
	public <T> List<Component> build(TextModule module, T resolveData){
		
		//Hashmap contenant les placeholders et leur nombre dans la feuille
		HashMap<String, Pair<Integer,List<String>>> holdersMap = getHoldersMap(resolveData);
		
		//Récupère le style final
		TextStyle style = module.asMerged(getStyles());
		style.addTagResolver(getAddtionalStyles().toArray(new TagResolver[0]));
		
		List<Component> components = new ArrayList<Component>();
		for(String line : lines) {
			
			// /!\ Les clés sont de la forme "<(tag)>"
			var tagsFound = new ArrayList<Pair<Integer,String>>();
			var tagNumber = 0;
			var offset = 0;
			TextUtil.rabinKarp(line, t -> tagsFound.add(t), holdersMap.keySet().toArray(new String[0]));
			
			for(Pair<Integer, String> tagFound : tagsFound){
				var position = tagFound.a();
				var tag = tagFound.b();
				
				//On ajoute un numéro à la fin du tag (<name> devient <name1>)
				line = TextUtil.insertText(line, Integer.toString(tagNumber),offset + position + tag.length() - 1);
				
				//On retire les "<" & ">" du tag
				String cutTag = tag.subSequence(1, tag.length() - 1).toString();
				
				//On ajoute un numéro à la fin du tag
				String newTag = cutTag + Integer.toString(tagNumber);
				
				//On créée le placeholder
				List<String> tagComponents = holdersMap.get(tag).b();
				
				style.addTagResolver(	
					Placeholder.parsed(newTag, tagComponents.get(tagNumber % tagComponents.size()))
				);
				
				//On décale l'offset
				offset += Integer.toString(tagNumber).length();
				
				//On augmente le compteur
				tagNumber += 1;
			}

			//On désérialise
			components.add(style.deserialize(line));
		}
		return components;
	}
}
