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

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;
import net.kyori.adventure.text.Component;

@ModuleNameAnnotation(Name = "MappingModule")
public class MapModule extends PluginModule{

	// Static part
	
		public static final List<MapTypeIdentifier> AVAILABLE_MAP_TYPES = new LinkedList<>();
		
		public static void registerMapTypes(MapTypeIdentifier... mapType) {
			for (MapTypeIdentifier mt : mapType) {
				if (!AVAILABLE_MAP_TYPES.contains(mt)) {
					AVAILABLE_MAP_TYPES.add(mt);
				}
			}
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
			mapFolder = new File(getPlugin().getDataFolder(), "Maps");
			mapRegistryFile = new File(mapFolder,"maps.yml");
			try {
				mapFolder.createNewFile();
				mapRegistryFile.createNewFile();
			} catch (IOException e) {
				logException(Level.SEVERE, "Impossible de creer le fichier map", e);
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
			
			var mapCommand = new CommandAPICommand("map");
			var addCommand = new CommandAPICommand("add")
					.withArguments(new StringArgument("mapName"))
					.executes((sender,args) -> {
						//TODO addMap
					});
			var removeCommand = new CommandAPICommand("remove")
					.withArguments(new StringArgument("mapName"))
					.executes((sender,args) -> {
						//TODO removeMap
					});
			var editCommand = new CommandAPICommand("edit").executes((sender,args)-> {
				sender.sendMessage(Component.text("Oui"));
			});
			for (var mapType : AVAILABLE_MAP_TYPES) {
				log(Level.INFO, "MAP TYPE = " + mapType.getId());
				try {
					editCommand.withSubcommands(MapCommandGenerator.generateFor(mapType).toArray(CommandAPICommand[]::new));
				}catch(Exception e) {
					logException(Level.WARNING, "Une erreur est survenue lors de la génération des commandes pour le type de map " + mapType.getId(), e);
				}
			}
			mapCommand.withSubcommands(addCommand, removeCommand, editCommand);
			try {
				mapCommand.register();
			}catch(Exception e) {
				logException(Level.WARNING, "Une erreur est survenue lors de l'ajout des commandes de map au registre.", e);
			}
		}
		
		private void saveMapList() {
			var config = new YamlConfiguration();
			for (var map : getAvailableMaps()) {
				config.getStringList("mapList").add(map.name());
				config.addDefault("map."+map.name(), map);
			}
			try {
				config.save(mapRegistryFile);
			} catch (IOException e) {
				logException(Level.WARNING, "Un erreur est survenue lors de l'enregistrement du fichier maps.yaml", e);
			}
		}
		
		public void addMap(MapIdentifier identifier) {
			getAvailableMaps().add(identifier);
			RunAsync(() -> saveMapList());
		}
		
		public void removeMap(MapIdentifier identifier) {
			getAvailableMaps().remove(identifier);
			RunAsync(() -> saveMapList());
		}
		
		public void removeMap(String mapName) {
			var map = getAvailableMaps().stream().filter(m -> m.name().contentEquals(mapName)).findFirst();
			if (map.isPresent()) {
				removeMap(map.get());
			}
		}
		
		public synchronized void saveMapInstance(GameMap map) {
			File mapFile = new File(mapFolder, map.getId().name());
			try (var fos = new FileOutputStream(mapFile);
					var oos = new ObjectOutputStream(fos)){
				oos.writeObject(map);
				oos.flush();
			}catch (Exception e) {
				logException(Level.SEVERE, "Une erreur est survenue lors de la sauvegarde de la carte "+map.getId().name(), e);
			}
		}
		
		/**
		 * Charge la carte a partir de s,on fichier, retourne une nouvelle carte en cas échéant.
		 * @param identifier
		 * @return
		 */
		public GameMap loadMap(MapIdentifier identifier) {
			File mapFile = new File(mapFolder, identifier.name());
			try (var fis = new FileInputStream(mapFile);
					var ois = new ObjectInputStream(fis)){
				return (GameMap) ois.readObject();
			}catch (Exception e) {
				logException(Level.SEVERE, "Une erreur est survenue lors du chargement de la carte "+identifier.name(), e);
			}
			return identifier.type().generateTypedMap(identifier);
		}
		
		public void saveMapInstanceAsync(GameMap map) {
			RunAsync(() -> saveMapInstance(map));
		}
}
