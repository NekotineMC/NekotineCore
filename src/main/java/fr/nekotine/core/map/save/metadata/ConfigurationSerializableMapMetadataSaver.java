package fr.nekotine.core.map.save.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import fr.nekotine.core.map.MapHandle;
import fr.nekotine.core.map.MapMetadata;
import fr.nekotine.core.map.save.configurationserialization.ConfigurationSerializableAdapter;

public class ConfigurationSerializableMapMetadataSaver implements IMapMetadataSaver {
	
	public static final String fileExtension = ".yml";
	
	private static final String mapMetadataKey = "mapMetadata";
	
	private File folder;
	
	public ConfigurationSerializableMapMetadataSaver(File mapFolder) {
		folder = mapFolder;
		if (!folder.exists()) {
			folder.mkdir();
		}
	}
	
	@Override
	public <T> boolean delete(MapHandle<T> handle) {
		var file = new File(folder, handle.getName() + fileExtension);
		return file.delete();
	}
	
	@Override
	public <T> void saveMetadata(MapHandle<T> handle, MapMetadata map) {
		var mapFile = new File(folder, handle.getName() + fileExtension);
		if (!mapFile.exists()) {
			try {
				mapFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		var config = YamlConfiguration.loadConfiguration(mapFile);
		config.set(mapMetadataKey, new ConfigurationSerializableAdapter(map));
		try {
			config.save(mapFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> MapMetadata loadMetadata(MapHandle<T> handle) {
		var mapFile = new File(folder, handle.getName() + fileExtension);
		if (!mapFile.exists()) {
			throw new RuntimeException(new FileNotFoundException(
					String.format("Le fichier pour la carte %s n'existe pas. (Chemin: %s)", handle.getName(), mapFile.getAbsolutePath())
					));
		}
		var config = YamlConfiguration.loadConfiguration(mapFile);
		return (MapMetadata)config.get(mapMetadataKey);
	}
}
