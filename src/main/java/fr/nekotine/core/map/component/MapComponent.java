package fr.nekotine.core.map.component;

import java.io.Serializable;

import fr.nekotine.core.map.Map;
import fr.nekotine.core.map.MapModule;

public abstract class MapComponent implements Serializable{

	private String _name;
	
	private transient MapComponent _owner;
	
	private transient MapModule _module;
	
	/**
	 * Un constructeur utilisé lors de la construction de l'arbre de commands, NE PAS SURCHARGER DANS LES CLASSES ENFANTS
	 * @param owner
	 * @param name
	 */
	public MapComponent(MapModule module, MapComponent owner, String name) {
		_name = name;
		_owner = owner;
	}
	
	public MapModule getModule() {
		return _module;
	}
	
	public void setModule(MapModule module) {
		_module = module;
	}
	
	public String getName() {
		return _name;
	}
	
	public Map getMap() {
		if (_owner instanceof Map map) {
			return map;
		}
		return _owner.getMap();
	}
	/*
	protected List<MapCommand> generateCommands() {
		List<MapCommand> list = new LinkedList<>();
		Class<? extends MapComponent> clazz = getClass();
		for (Field field : clazz.getDeclaredFields()) {
			Class<?> fieldType = field.getType();
			ComposingMap annotation = field.getAnnotation(ComposingMap.class);
			if (annotation != null) {
				try {
					if (MapComponent.class.isAssignableFrom(fieldType)) {
						var ctor = fieldType.getConstructor(MapModule.class, MapComponent.class, String.class);
						MapComponent childComponent = (MapComponent) ctor.newInstance(_module, this, annotation.Name());
						for (var childCommand : childComponent.generateCommands()) {
							var command_args = new LinkedList<Argument<?>>();
							command_args.add(new LiteralArgument(annotation.Name()));
							command_args.addAll(childCommand.arguments());
							var executor = childCommand.action();
							list.add(new MapCommand(command_args, (sender, args)->{
								var self = (MapComponent) args[0];
								try {
									if (field.get(self) instanceof MapComponent futureSelf && futureSelf != null) {
										args[0] = futureSelf;
										return executor.executeWith(sender, args);
									}
								} catch (IllegalArgumentException | IllegalAccessException e) {
									_module.logException(Level.SEVERE, "Une erreur est survenue lors de l'execution d'une commande", e);
								}
								CommandAPI.fail("Le nom donné est invalide");
								return 0;
							}));
						}
					}
				}catch(Exception e) {
					Bukkit.getLogger().log(Level.WARNING,
							"Une erreur est survenue lors de la génération de commandes pour le composant de carte " + clazz.getName() +
							" de nom " + annotation.Name() +
							" et de type " + fieldType.getName(),
							e);
				}
			}
		}
		return list;
	}*/
}
