package fr.nekotine.core.map;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.extent.clipboard.Clipboard;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.serialization.configurationserializable.ConfigurationSerializableUtil;

public class MapModule implements IMapModule {

	private final File mapFolder = new File(Ioc.resolve(JavaPlugin.class).getDataFolder(), "Maps");
	
	public MapModule() {
		if (!mapFolder.exists()) {
			mapFolder.mkdir();
		}
	}
	
	@Override
	public Collection<MapMetadata> listMaps() {
		var list = new LinkedList<MapMetadata>();
		var mapDirs = mapFolder.listFiles(f -> f.isDirectory());
		for (var dir : mapDirs) {
			var metadata = getMapMetadata(dir.getName());
			if (metadata != null) {
				list.add(metadata);
			}
		}
		return list;
	}

	@Override
	public MapMetadata getMapMetadata(String name) {
		return getContent(name, MapMetadata.class);
	}

	@Override
	public void saveMapMetadata(MapMetadata metadata) {
		saveContent(metadata.getName(), metadata);
	}

	@Override
	public Clipboard getStructure(MapMetadata metadata) {
		return null;
	}

	@Override
	public void saveStructure(MapMetadata metadata, Clipboard structure) {
	}

	@Override
	public <T> @Nullable T getContent(MapMetadata metadata, Class<T> contentType) {
		return getContent(metadata.getName(), contentType);
	}

	@Override
	public <T> void saveContent(MapMetadata metadata, T content) {
		saveContent(metadata.getName(), content);
	}
	
	@SuppressWarnings("unchecked")
	private <T> @Nullable T getContent(String mapName, Class<T> contentType) {
		if (!ConfigurationSerializable.class.isAssignableFrom(contentType)) {
			throw new NotImplementedException(
					String.format("Cette implémentation du MapModule (%s) ne peut deserializer que des ConfigrationSerializable",
					contentType.getTypeName()));
		}
		var mapNameFolder = new File(mapFolder, mapName);
		if (mapNameFolder.exists() && mapNameFolder.isDirectory()) {
			var nameStart = contentType.getSimpleName() + '.';
			var matchingFiles = mapNameFolder.listFiles(f -> f.getName().startsWith(nameStart));
			if (matchingFiles.length > 0) {
				var file = matchingFiles[0];
				var config = YamlConfiguration.loadConfiguration(file);
				return contentType.cast(ConfigurationSerializableUtil.getObjectFrom(config, (Class<? extends ConfigurationSerializable>)contentType));
			}
		}
		return null;
	}
	
	private <T> void saveContent(String mapName, T content) {
		var contentType = content.getClass();
		if (!ConfigurationSerializable.class.isAssignableFrom(contentType)) {
			throw new NotImplementedException(
					String.format("Cette implémentation du MapModule (%s) ne peut deserializer que des ConfigrationSerializable",
					contentType.getTypeName()));
		}
		var mapNameFolder = new File(mapFolder, mapName);
		if (!mapNameFolder.exists()) {
			mapNameFolder.mkdir();
		}
		if (mapNameFolder.isDirectory()) {
			var nameStart = contentType.getSimpleName() + '.';
			var matchingFiles = mapNameFolder.listFiles(f -> f.getName().startsWith(nameStart));
			File file;
			if (matchingFiles.length > 0) {
				file = matchingFiles[0];
			}else {
				file = new File(mapNameFolder,nameStart+"yml");
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			var config = YamlConfiguration.loadConfiguration(file);
			ConfigurationSerializableUtil.setFromObject(config, (ConfigurationSerializable)content);
			try {
				config.save(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void deleteMap(MapMetadata metadata) {
		var mapNameFolder = new File(mapFolder, metadata.getName());
		if (mapNameFolder.exists() && mapNameFolder.isDirectory()) {
			for (var file : mapNameFolder.listFiles()) {
				file.delete();
			}
			mapNameFolder.delete();
		}
	}

	@Override
	public void unload() {
	}

}
