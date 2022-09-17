package fr.nekotine.core.map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
			mapRegistryFile = new File(mapFolder,"maps.yaml");
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
			for (var item : config.getList("maps", new ArrayList<MapIdentifier>(0))) {
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
			
			new CommandAPICommand("map").withSubcommands(
					new CommandAPICommand("add")
					.withArguments(new StringArgument("mapName"))
					.executes((sender,args) -> {
						//TODO addMap
					})
					,new CommandAPICommand("remove")
					.withArguments(new StringArgument("mapName"))
					.executes((sender,args) -> {
						//TODO removeMap
					})
					);
			
			for (var mapType : AVAILABLE_MAP_TYPES) {
				try {
					for (var command : MapCommandGenerator.generateFor(mapType)) {
						command.register();
					}
				}catch(Exception e) {
					logException(Level.WARNING, "Une erreur est survenue lors de la génération des commandes pour le type de map " + mapType.getId(), e);
				}
			}
		}
		
		public void addMap(String mapName, Object[] more) {
			
		}
		
		public void removeMap(String mapName) {
			
		}
}
