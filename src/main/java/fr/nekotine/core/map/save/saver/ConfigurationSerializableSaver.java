package fr.nekotine.core.map.save.saver;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import fr.nekotine.core.map.MapIdentifier;
import fr.nekotine.core.map.save.IMapSaver;
import fr.nekotine.core.map.save.saver.configurationserializable.ConfigurationSerializableAdapter;

public class ConfigurationSerializableSaver implements IMapSaver {
	
	private File folder;
	
	private File listingFile;
	
	public ConfigurationSerializableSaver(File mapFolder) {
		folder = mapFolder;
		if (!folder.exists()) {
			folder.mkdir();
		}
	}
	
	@Override
	public void save(MapIdentifier identifier, Object map) {
		var mapFile = new File(folder, identifier.getName());
		if (!mapFile.exists()) {
			try {
				mapFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);// TODO mieux que ca
			}
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(mapFile);
		config.set("map", new ConfigurationSerializableAdapter(map));
		try {
			config.save(mapFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object load(MapIdentifier identifier) {
		return null;
	}
}
