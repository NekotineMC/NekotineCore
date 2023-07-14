package fr.nekotine.core.map.save.saver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import fr.nekotine.core.map.MapIdentifier;
import fr.nekotine.core.map.save.IMapSaver;
import fr.nekotine.core.map.save.saver.configurationserializable.ConfigurationSerializableAdapter;
import fr.nekotine.core.tuple.Pair;

public class ConfigurationSerializableSaver implements IMapSaver {
	
	public static final String fileExtention = ".yml";
	
	private static final String contentKey = "mapContent";
	
	private static final String identityKey = "mapIdentity";
	
	private File folder;
	
	public ConfigurationSerializableSaver(File mapFolder) {
		folder = mapFolder;
		if (!folder.exists()) {
			folder.mkdir();
		}
	}
	
	@Override
	public boolean delete(MapIdentifier identifier) {
		var file = new File(folder, identifier.getName() + fileExtention);
		return file.delete();
	}
	
	@Override
	public void save(MapIdentifier identifier, Object map) {
		var mapFile = new File(folder, identifier.getName() + fileExtention);
		if (!mapFile.exists()) {
			try {
				mapFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		var config = YamlConfiguration.loadConfiguration(mapFile);
		config.set(contentKey, new ConfigurationSerializableAdapter(map));
		config.set(identityKey, identifier);
		try {
			config.save(mapFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object load(MapIdentifier identifier) {
		var mapFile = new File(folder, identifier.getName() + fileExtention);
		if (!mapFile.exists()) {
			throw new RuntimeException(new FileNotFoundException(
					String.format("Le fichier pour la carte %s n'existe pas. (Chemin: %s)", identifier.getName(), mapFile.getAbsolutePath())
					));
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(mapFile);
		var map = config.get(contentKey);
		if (map instanceof ConfigurationSerializableAdapter adapter) {
			return adapter.getContent();
		}
		return map;
	}
	
	public Pair<MapIdentifier,Object> loadFile(File file){
		var config = YamlConfiguration.loadConfiguration(file);
		return new Pair<>((MapIdentifier)config.get(identityKey), config.get(contentKey));
	}
}
