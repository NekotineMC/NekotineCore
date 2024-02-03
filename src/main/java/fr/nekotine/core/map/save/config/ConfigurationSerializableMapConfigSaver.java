package fr.nekotine.core.map.save.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import fr.nekotine.core.map.MapHandle;

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
	public <T extends ConfigurationSerializable> boolean delete(MapHandle<T> handle) {
		var file = new File(folder, handle.getName() + fileExtention);
		return file.delete();
	}
	
	@Override
	public <T extends ConfigurationSerializable> void save(MapHandle<T> handle, ConfigurationSerializable map) {
		var mapFile = new File(folder, handle.getName() + fileExtention);
		if (!mapFile.exists()) {
			try {
				mapFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		var config = YamlConfiguration.loadConfiguration(mapFile);
		config.set("", map);
		try {
			config.save(mapFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T extends ConfigurationSerializable> T load(MapHandle<T> handle) {
		var mapFile = new File(folder, handle.getName() + fileExtention);
		if (!mapFile.exists()) {
			throw new RuntimeException(new FileNotFoundException(
					String.format("Le fichier pour la carte %s n'existe pas. (Chemin: %s)", handle.getName(), mapFile.getAbsolutePath())
					));
		}
		var config = YamlConfiguration.loadConfiguration(mapFile);
		var map = config.get("");
		if (map == null) {
			try {
				map = handle.getConfigType().getConstructor().newInstance();
			}catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		return handle.getConfigType().cast(map);
	}
}
