package fr.nekotine.core.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.Files;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.logging.FormatingRemoteLogger;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import fr.nekotine.core.text.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

@ModuleNameAnnotation(Name = "MappingModule")
public class MapModule extends PluginModule{

	public Logger LOGGER = new FormatingRemoteLogger(NekotineCore.LOGGER, "[MapModule] %s");
	
	// Static part
	
		private static final List<MapTypeIdentifier> AVAILABLE_MAP_TYPES = new LinkedList<>();
		
		public static void registerMapTypes(MapTypeIdentifier... mapType) {
			for (MapTypeIdentifier mt : mapType) {
				if (!AVAILABLE_MAP_TYPES.contains(mt)) {
					AVAILABLE_MAP_TYPES.add(mt);
				}
			}
		}
		
		public static List<MapTypeIdentifier> getAvailableMapTypes(){
			return AVAILABLE_MAP_TYPES;
		}
		
		public static MapTypeIdentifier getMapTypeById(String id) {
			var type = AVAILABLE_MAP_TYPES.stream().filter(t -> id.contentEquals(t.getId())).findFirst();
			return type.isPresent()?type.get():null;
		}
		
		private static final List<MapIdentifier> AVAILABLE_MAPS = new LinkedList<>();
		
		/**
		 * Methode thread safe pour retourner la liste des maps
		 * @return
		 */
		public static synchronized List<MapIdentifier> getAvailableMaps(){
			return AVAILABLE_MAPS;
		}
		
		public static synchronized List<MapIdentifier> getMapsForType(MapTypeIdentifier type){
			return AVAILABLE_MAPS.stream().filter(m -> type.equals(m.type())).toList();
		}
		
		public static synchronized MapIdentifier getMap(String name) {
			for (var map : AVAILABLE_MAPS) {
				if (map.name().contentEquals(name)) {
					return map;
				}
			}
			return null;
		}
		
		// -----
	
		private File mapFolder;
		private File mapRegistryFile;
		
		@Override
		protected void onEnable() {
			super.onEnable();
			ConfigurationSerialization.registerClass(MapIdentifier.class);
			var pluginFolder = getPlugin().getDataFolder();
			try {
				pluginFolder.mkdir();
				mapFolder = new File(getPlugin().getDataFolder(), "Maps");
				mapFolder.mkdir();
				mapRegistryFile = new File(mapFolder,"maps.yml");
				mapRegistryFile.createNewFile();
			} catch (IOException e) {
				logException(Level.SEVERE, "Impossible de creer le fichier map", e);
				return;
			}
			RunAsync(() -> fetchMapList());
		}
		
		public synchronized void fetchMapList() {
			var config = new YamlConfiguration();
			try {
				config.load(mapRegistryFile);
			} catch (IOException | InvalidConfigurationException e) {
				logException(Level.WARNING, "Un erreur est survenue lors du chargement du fichier maps.yaml, est-il au bon format?", e);
				return;
			}
			for (var mapName : config.getStringList("mapList")) {
				var item = config.get("map."+mapName);
				if (item instanceof MapIdentifier map) {
					AVAILABLE_MAPS.removeIf(m -> map.name().contentEquals(m.name()));
					AVAILABLE_MAPS.add(map);
				}
			};
		}
		
		/**
		 * Methode à appeler dans le {@link org.bukkit.plugin.java.JavaPlugin#onEnable JavaPlugin.onEnable()}
		 */
		public void generateCommands() {
			
			log(Level.INFO, "Generation de commandes pour les types de map enregistre:");
			
			var mapCommand = new CommandAPICommand("map");
			var addCommand = new CommandAPICommand("add")
					.withArguments(new StringArgument("mapName"))
					.withArguments(MapCommandGenerator.registeredMapTypeArgument("mapType"))
					.withArguments(new ItemStackArgument("icon"))
					.executes((sender,args) -> {
						var name = (String)args[0];
						var mapTypeIdentifier = (MapTypeIdentifier)args[1];
						var icon = (ItemStack)args[2];
						var mapId = new MapIdentifier(mapTypeIdentifier, name, name, "", icon.getType());
						if (addMap(mapId)) {
							sender.sendMessage(
									Component.text(String.format("La carte à bien été ajoutée."))
									.color(Colors.COMMAND_FEEDBACK));
						}else {
							sender.sendMessage(
									Component.text(String.format("La carte existait déja."))
									.color(Colors.COMMAND_FEEDBACK));
						}
					});
			var removeCommand = new CommandAPICommand("remove")
					.withArguments(MapCommandGenerator.existingMapArgument("mapName"))
					.executes((sender,args) -> {
						removeMap((MapIdentifier) args[0]);
						sender.sendMessage(
								Component.text(String.format("La carte à bien été supprimée."))
								.color(Colors.COMMAND_FEEDBACK));
					});
			var copyCommand = new CommandAPICommand("copy")
					.withArguments(MapCommandGenerator.existingMapArgument("sourceName"))
					.withArguments(new StringArgument("destinationName"))
					.executes((sender,args) -> {
						var mapId = (MapIdentifier)args[0];
						var newMapName = (String)args[1];
						// New map id
						var newMapId = new MapIdentifier(mapId.type(),newMapName,mapId.displayName(),mapId.description(),mapId.icon());
						var srcFile = new File(mapFolder, mapId.name());
						var destFile = new File(mapFolder, newMapName);
						if (addMap(newMapId)) {
							sender.sendMessage(
									Component.text("La carte à bien été ajoutée.")
									.color(Colors.COMMAND_FEEDBACK));
						}else {
							sender.sendMessage(
									Component.text("La carte existait déja.")
									.color(Colors.COMMAND_FEEDBACK));
						}
						var oldMapName = mapId.name();
						RunAsync(() -> {
							try {
								Files.copy(srcFile, destFile);
							} catch (IOException e) {
								RunSync(() -> 
								logException(Level.WARNING, String.format("Une erreur est survenue lors de la copie de la carte %s", oldMapName), e));
							}
						});
					});
			var reloadCommand = new CommandAPICommand("reload")
					.executes((sender,args) -> {
						RunAsync(this::fetchMapList);
						sender.sendMessage(
								Component.text("Rechargment de la liste des cartes...")
								.color(Colors.COMMAND_FEEDBACK));
					});
			var listCommand = new CommandAPICommand("list")
					.executes((sender,args) -> {
						sender.sendMessage(
								Component.text("Liste des cartes:")
								.color(Colors.COMMAND_FEEDBACK));
						for (var map : getAvailableMaps()) {
							sender.sendMessage(
								Component.text(String.format("Id: %s Type: %s Nom: ", map.name(), map.type().getId()))
								.color(NamedTextColor.AQUA)
								.append(MiniMessage.miniMessage().deserialize(map.displayName())));
						}
					});
			var editCommand = new CommandAPICommand("edit").executes((sender,args)-> {
				sender.sendMessage(Component.text("usage: map edit <mapType> ..."));
			});
			for (var mapType : AVAILABLE_MAP_TYPES) {
				log(Level.INFO, "MAP TYPE = " + mapType.getId());
				try {
					editCommand.withSubcommands(MapCommandGenerator.generateFor(mapType).toArray(CommandAPICommand[]::new));
				}catch(Exception e) {
					logException(Level.WARNING, "Une erreur est survenue lors de la génération des commandes pour le type de map " + mapType.getId(), e);
				}
			}
			mapCommand.withSubcommands(addCommand, removeCommand, copyCommand, editCommand, reloadCommand, listCommand);
			try {
				mapCommand.register();
			}catch(Exception e) {
				logException(Level.WARNING, "Une erreur est survenue lors de l'ajout des commandes de map au registre.", e);
			}
		}
		
		private synchronized void saveMapList() {
			var config = new YamlConfiguration();
			for (var map : getAvailableMaps()) {
				var list = config.getStringList("mapList");
				list.add(map.name());
				config.set("mapList", list);
				config.set("map."+map.name(), map);
			}
			try {
				config.save(mapRegistryFile);
			} catch (IOException e) {
				logException(Level.WARNING, "Un erreur est survenue lors de l'enregistrement du fichier maps.yaml", e);
			}
		}
		
		public boolean addMap(MapIdentifier identifier) {
			if (!getAvailableMaps().contains(identifier)) {
				getAvailableMaps().add(identifier);
				RunAsync(this::saveMapList);
				return true;
			}
			return false;
		}
		
		public void removeMap(MapIdentifier identifier) {
			getAvailableMaps().remove(identifier);
			File f = new File(mapFolder, identifier.name());
			RunAsync(this::saveMapList);
			RunAsync(f::delete);
		}
		
		public void removeMap(String mapName) {
			var map = getAvailableMaps().stream().filter(m -> m.name().contentEquals(mapName)).findFirst();
			if (map.isPresent()) {
				removeMap(map.get());
			}
		}
		
		public synchronized void saveMapInstance(GameMap map) {
			File mapFile = new File(mapFolder, map.getId().name());
			if (!mapFile.exists()) {
				try {
					mapFile.createNewFile();
				} catch (IOException e) {
					logException(Level.SEVERE, "Une erreur est survenue lors de la création du fichier carte  "+map.getId().name(), e);
				}
			}
			try (var fos = new FileOutputStream(mapFile);
					var oos = new ObjectOutputStream(fos)){
				oos.writeObject(map);
				oos.flush();
			}catch (Exception e) {
				logException(Level.SEVERE, "Une erreur est survenue lors de la sauvegarde de la carte "+map.getId().name(), e);
			}
		}
		
		/**
		 * Charge la carte a partir de son fichier, retourne une nouvelle carte en cas échéant.
		 * @param identifier
		 * @return
		 */
		public GameMap loadMap(MapIdentifier identifier) {
			File mapFile = new File(mapFolder, identifier.name());
			if (mapFile.exists()) {
				try (var fis = new FileInputStream(mapFile);
						var ois = new ObjectInputStream(fis)){
					return (GameMap) ois.readObject();
				}catch (Exception e) {
					logException(Level.SEVERE, "Une erreur est survenue lors du chargement de la carte "+identifier.name(), e);
				}
			}
			return identifier.type().generateTypedMap(identifier);
		}
		
		public void saveMapInstanceAsync(GameMap map) {
			RunAsync(() -> saveMapInstance(map));
		}
}
