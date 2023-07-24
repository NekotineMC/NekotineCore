package fr.nekotine.core.map.save.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import fr.nekotine.core.map.MapHandle;
import fr.nekotine.core.map.save.configurationserialization.ConfigurationSerializableAdapter;

public class ConfigurationSerializableMapConfigSaver implements IMapConfigSaver {
	
	private static final String fileExtention = ".yml";
	
	private static final String contentKey = "mapContent";
	
	private File folder;
	
	public ConfigurationSerializableMapConfigSaver(File mapFolder) {
		folder = mapFolder;
		if (!folder.exists()) {
			folder.mkdir();
		}
	}
	
	@Override
	public <T> boolean delete(MapHandle<T> handle) {
		var file = new File(folder, handle.getName() + fileExtention);
		return file.delete();
	}
	
	@Override
	public <T> void save(MapHandle<T> handle, Object map) {
		var mapFile = new File(folder, handle.getName() + fileExtention);
		if (!mapFile.exists()) {
			try {
				mapFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		var config = YamlConfiguration.loadConfiguration(mapFile);
		config.set(contentKey, new ConfigurationSerializableAdapter(map));
		try {
			config.save(mapFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T load(MapHandle<T> handle) {
		var mapFile = new File(folder, handle.getName() + fileExtention);
		if (!mapFile.exists()) {
			throw new RuntimeException(new FileNotFoundException(
					String.format("Le fichier pour la carte %s n'existe pas. (Chemin: %s)", handle.getName(), mapFile.getAbsolutePath())
					));
		}
		var config = YamlConfiguration.loadConfiguration(mapFile);
		var map = config.get(contentKey);
		if (map instanceof ConfigurationSerializableAdapter adapter) {
			map = adapter.getContent();
		}
		return handle.getConfigType().cast(map);
	}
}
