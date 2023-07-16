package fr.nekotine.core.map.command.generator;

import java.util.LinkedList;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.annotation.MapElementTyped;
import fr.nekotine.core.map.command.IMapElementCommandGeneratorResolver;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.util.CollectionUtil;

public class DefaultMapElementCommandGenerator extends MapElementCommandGenerator{

	public DefaultMapElementCommandGenerator() {
		super(false);
	}

	protected MapCommandBranch[] generateFor(Class<?> elementType) {
		var list = new LinkedList<MapCommandBranch>();
		for (var field : elementType.getDeclaredFields()) {
			if (field.isAnnotationPresent(ComposingMap.class)) {
				var fieldType = field.getType();
				field.trySetAccessible();
				var name = field.getAnnotation(ComposingMap.class).value();
				if (name.isBlank()) {
					name = field.getName();
				}
				final var finalName = name;
				var selfArgument = new LiteralArgument(finalName);
				var generator = NekotineCore.IOC.resolve(IMapElementCommandGeneratorResolver.class).resolve(fieldType);
				// special Dictionary case
				if (MapDictionaryElement.class == fieldType && generator instanceof DictionaryCommandGenerator dictGenerator) { // Type précis pour permettre l'héritage par l'utilisateur
					if (field.isAnnotationPresent(MapElementTyped.class)) {
						dictGenerator.setNodeName(finalName);
						dictGenerator.setNestedElementType(field.getAnnotation(MapElementTyped.class).value());
					}else {
						var msg = "[MapCommandGenerator]->Default Le champ %s dans %s est de type dictionnaire mais n'a"
								+ " pas l'annotation MapDictionaryElementType nécessaire pour sa génération.";
						throw new IllegalArgumentException(String.format(msg,finalName,elementType.getName()));
					}
				}
				for (var branch : generator.getGenerated(fieldType)) {
					var arguments = CollectionUtil.linkedList(branch.arguments());
					arguments.add(0, selfArgument);
					MapCommandExecutor executor = (element, sender, args) ->{
						try {
							branch.consumer().accept(field.get(element), sender, args);
						}catch(IllegalAccessException e) {
							var ex = new RuntimeException("Impossible d'acceder au champ "+field.getName()+" de la classe "+elementType.getName(),e);
							NekotineCore.LOGGER.throwing("DefaultMapElementCommandGenerator", "MapCommandBranch[] generateFor(Class<?> elementType)", ex);
							throw ex;
						}
					};
					list.add(new MapCommandBranch(arguments.toArray(Argument<?>[]::new), executor));
				}
			}
		}
		return list.toArray(MapCommandBranch[]::new);
	}

}
