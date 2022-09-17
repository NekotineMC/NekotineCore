package fr.nekotine.core.map.component;

import java.util.List;

import fr.nekotine.core.map.MapModule;

public final class MapComponentList<ComponentType extends MapComponent> extends MapComponent {
	
	private Class<ComponentType> _contentType;
	
	public MapComponentList(MapModule module,MapComponent owner, String name, Class<ComponentType> contentType) {
		super(module, owner, name);
		_contentType = contentType;
	}

	private List<ComponentType> _content;
/*
	@Override
	protected List<MapCommand> generateCommands() {
		// COMMANDS
		var commands = new LinkedList<MapCommand>();//add edit remove
		// ADD
		var add_args = new LinkedList<Argument<?>>();
		add_args.add(new LiteralArgument("add"));
		add_args.add(new StringArgument("name"));
		commands.add(new MapCommand(add_args, (sender, args)->{
			@SuppressWarnings("unchecked")
			var self = (MapComponentList<ComponentType>) args[0];
			Constructor<ComponentType> constructor;
			try {
				constructor = _contentType.getConstructor(MapModule.class, MapComponent.class, String.class);
			} catch (NoSuchMethodException | SecurityException e) {
				CommandAPI.fail("Impossible de trouver le constructeur pour le type " + _contentType.getName());
				return 0;
			}
			if (args[1] instanceof String name) {
				try {
					ComponentType newComponent = constructor.newInstance(getModule(), this, name);
					self._content.add(newComponent);
					sender.sendMessage(Component.text(name + " a bien été ajouté à " + getName()).color(Colors.COMMAND_FEEDBACK));
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					CommandAPI.fail("Impossible d'appeler le constructeur pour le type " + _contentType.getName());
					return 0;
				}
			}else {
				CommandAPI.fail("Le nom donné est invalide");
				return 0;
			}
			return 1;
		}));
		// REMOVE
		var remove_args = new LinkedList<Argument<?>>();
		remove_args.add(new LiteralArgument("remove"));
		remove_args.add(new StringArgument("name"));
		commands.add(new MapCommand(remove_args, (sender, args)->{
			@SuppressWarnings("unchecked")
			var self = (MapComponentList<ComponentType>) args[0];
			if (args[1] instanceof String name) {
				if (self._content.removeIf(c -> name.contentEquals(c.getName()))) {
					sender.sendMessage(Component.text("Une ou plusieurs suppression on été effectués").color(Colors.COMMAND_FEEDBACK));
				}else {
					sender.sendMessage(Component.text("Aucune suppression n'on été effectués").color(Colors.COMMAND_FEEDBACK));
				}
			}else {
				CommandAPI.fail("Le nom donné est invalide");
				return 0;
			}
			return 1;
		}));
		try {
			// EDIT
			var ctor = _contentType.getConstructor();
			MapComponent child = ctor.newInstance();
			for (MapCommand childCommand : child.generateCommands()) {
				var edit_args = new LinkedList<Argument<?>>();
				edit_args.add(new LiteralArgument("edit"));
				edit_args.add(new StringArgument("name"));
				edit_args.addAll(childCommand.arguments());
				var executor = childCommand.action();
				commands.add(new MapCommand(edit_args, (sender, args)->{
					@SuppressWarnings("unchecked")
					var self = (MapComponentList<ComponentType>) args[0];
					if (args[1] instanceof String name) {
						for (var item : self._content) {
							if (name.contentEquals(item.getName())) {
								var array_list = new ArrayList<Object>();
								array_list.add(item);
								for (int i = 2; i < args.length; i++) {
									array_list.add(args[i]);
								}
								return executor.executeWith(sender, array_list.toArray());
							}
						}
					}
					CommandAPI.fail("Le nom donné est invalide");
					return 0;
				}));
			}
			return commands;
		}catch(Exception e) {
			getModule().logException(Level.WARNING, "Erreur lors de la generation des commandes pour le type array " + getClass().getName(), e);
			return new LinkedList<MapCommand>();
		}
	}
	
	public List<ComponentType> getContent(){
		return _content;
	}
	*/
}
