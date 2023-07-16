package fr.nekotine.core.map.save.saver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import fr.nekotine.core.map.MapMetadata;
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
	public boolean delete(MapMetadata identifier) {
		var file = new File(folder, identifier.getName() + fileExtention);
		return file.delete();
	}
	
	@Override
	public void save(MapMetadata identifier, Object map) {
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
	public Pair<MapMetadata,Object> load(MapMetadata identifier) {
		var mapFile = new File(folder, identifier.getName() + fileExtention);
		if (!mapFile.exists()) {
			throw new RuntimeException(new FileNotFoundException(
					String.format("Le fichier pour la carte %s n'existe pas. (Chemin: %s)", identifier.getName(), mapFile.getAbsolutePath())
					));
		}
		var result = loadFile(mapFile);
		identifier.updateWith(result.a());
		return result;
	}
	
	public Pair<MapMetadata,Object> loadFile(File file){
		var config = YamlConfiguration.loadConfiguration(file);
		var identity = (MapMetadata)config.get(identityKey);
		identity.setSaver(this);
		var map = config.get(contentKey);
		if (map instanceof ConfigurationSerializableAdapter adapter) {
			map = adapter.getContent();
		}
		return new Pair<>(identity, map);
	}
}
