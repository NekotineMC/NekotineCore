package fr.nekotine.core.map.command.generator;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.map.annotation.CommandGeneratorOverride;
import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.annotation.MapDictKey;
import fr.nekotine.core.map.annotation.MapElementTyped;
import fr.nekotine.core.map.command.IMapElementCommandGeneratorResolver;
import fr.nekotine.core.map.command.MapCommandBranch;
import fr.nekotine.core.map.command.MapCommandExecutor;
import fr.nekotine.core.map.command.MapElementCommandGenerator;
import fr.nekotine.core.util.CollectionUtil;

public class DefaultMapElementCommandGenerator implements MapElementCommandGenerator{
	
	private Logger logger = new NekotineLogger(getClass());

	@Override
	public MapCommandBranch[] generateFor(Function<CommandArguments, Object> pipeline, Class<?> elementType) {
		var list = new LinkedList<MapCommandBranch>();
		for (var field : elementType.getDeclaredFields()) {
			if (field.isAnnotationPresent(ComposingMap.class)) {
				if (field.isAnnotationPresent(MapDictKey.class)) {
					continue; // On skip les champs qui servent de clef, car la commande ne sert a rien
				}
				var fieldType = field.getType();
				field.trySetAccessible();
				var name = field.getAnnotation(ComposingMap.class).value();
				if (name.isBlank()) {
					name = field.getName();
				}
				final var finalName = name;
				var selfArgument = new LiteralArgument(finalName);
				MapElementCommandGenerator generator;
				var resolver = Ioc.resolve(IMapElementCommandGeneratorResolver.class);
				if (field.isAnnotationPresent(CommandGeneratorOverride.class)) {
					var generatorType = field.getAnnotation(CommandGeneratorOverride.class).value();
					generator = resolver.resolveSpecific(generatorType);
				}else {
					generator = resolver.resolveFor(fieldType);
				}
				// special Dictionary case
				if (Map.class.isAssignableFrom(fieldType) && generator instanceof DictionaryCommandGenerator dictGenerator) { // Type précis pour permettre l'héritage par l'utilisateur
					if (field.isAnnotationPresent(MapElementTyped.class)) {
						if (field.isAnnotationPresent(CommandGeneratorOverride.class)) {
							dictGenerator.setElementGeneratorTypeOverride(field.getAnnotation(CommandGeneratorOverride.class).value());
						}
						dictGenerator.setNodeName(finalName);
						dictGenerator.setNestedElementType(field.getAnnotation(MapElementTyped.class).value());
					}else {
						var msg = "[MapCommandGenerator]->Default Le champ %s dans %s est de type dictionnaire mais n'a"
								+ " pas l'annotation MapElementTyped nécessaire pour sa génération.";
						throw new IllegalArgumentException(String.format(msg,finalName,elementType.getName()));
					}
				}
				Function<CommandArguments, Object> pip = a -> {
					try {
						return field.get(pipeline.apply(a));
					} catch (Exception e) {
						throw new RuntimeException("Impossible d'acceder au champ "+field.getName()+" de la classe "+elementType.getName(),e);
					}
				};
				for (var branch : generator.generateFor(pip, fieldType)) {
					var arguments = CollectionUtil.linkedList(branch.arguments());
					arguments.add(0, selfArgument);
					MapCommandExecutor executor = (element, sender, args) ->{
						try {
							field.set(element, branch.consumer().accept(field.get(element), sender, args));
							return element;
						}catch(IllegalAccessException e) {
							var ex = new RuntimeException("Impossible d'acceder au champ "+field.getName()+" de la classe "+elementType.getName(),e);
							logger.throwing("DefaultMapElementCommandGenerator", "MapCommandBranch[] generateFor(Class<?> elementType)", ex);
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
