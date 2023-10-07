package fr.nekotine.core.text.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import fr.nekotine.core.text.TextModule;
import fr.nekotine.core.text.placeholder.TextPlaceholder;
import fr.nekotine.core.text.style.TextStyle;
import fr.nekotine.core.tuple.Pair;
import fr.nekotine.core.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

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
	public Leaf addPlaceholder(TextPlaceholder... holders) {
		super.addPlaceholder(holders);
		return this;
	}
	
	//
	
	private HashMap<String, Pair<Integer,List<ComponentLike>>> getHoldersMap(){
		HashMap<String, Pair<Integer,List<ComponentLike>>> map = new HashMap<String, Pair<Integer,List<ComponentLike>>>();
		for(TextPlaceholder holder : getPlaceholders()) {
			
			Pair<String,ComponentLike>[] tags = holder.resolve();
			for(Pair<String,ComponentLike> tag : tags) {
				
				String asTag = "<".concat(tag.a()).concat(">");
				
				
				if(!map.containsKey(asTag))
					map.put(asTag, Pair.from(0, new ArrayList<ComponentLike>()));
				
				//On ajoute le ComponentLike à l'array
				map.get(asTag).b().add(tag.b());
			}
		}
		return map;
	}
	
	//
	
	@Override
	public List<Component> build(TextModule module){
		
		//Hashmap contenant les placeholders et leur nombre dans la feuille
		HashMap<String, Pair<Integer,List<ComponentLike>>> holdersMap = getHoldersMap();
		
		//Récupère le style final
		TextStyle style = module.asMerged(getStyles());
		
		List<Component> components = new ArrayList<Component>();
		for(String line : lines) {
			
			// /!\ Les clés sont de la forme "<(tag)>"
			HashMap<String,List<Integer>> tagsFound = TextUtil.rabinKarp(line, holdersMap.keySet().toArray(new String[0]));
			
			for(Entry<String, List<Integer>> tagFound : tagsFound.entrySet()) {
				String tag = tagFound.getKey();
				
				for(Integer position : tagFound.getValue()) {
					
					Integer tagNumber = holdersMap.get(tag).a();
					
					//On ajoute un numéro à la fin du tag (<name> devient <name1>)
					line = TextUtil.insertText(line, Integer.toString(tagNumber), position + tag.length() - 1);
					
					//On retire les "<" & ">" du tag
					String cutTag = tag.subSequence(1, tag.length() - 1).toString();
					
					//On ajoute un numéro à la fin du tag
					String newTag = cutTag + Integer.toString(tagNumber);
					
					//On créée le placeholder
					List<ComponentLike> tagComponents = holdersMap.get(tag).b();
					style.addTagResolver(
						Placeholder.component(newTag, tagComponents.get(tagNumber % tagComponents.size()))
					);
					
					//On augmente le compteur
					tagNumber += 1;
				}
			}
			
			//On désérialise
			components.add(style.deserialize(line));
		}
		return components;
	}
}
