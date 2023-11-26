package fr.nekotine.core.map.finder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.map.MapHandle;
import fr.nekotine.core.map.save.config.ConfigurationSerializableMapConfigSaver;
import fr.nekotine.core.map.save.metadata.ConfigurationSerializableMapMetadataSaver;
import fr.nekotine.core.util.AssertUtil;
import fr.nekotine.core.util.FileUtil;

/**
 * Cette classe permet la recherche de carte sur un dossier ou avec un fichier index.
 * @author XxGoldenbluexX
 *
 */
public class ConfigurationSerializableMapFolderFinder implements IMapFinder {

	private static final String fileExtension = ".yml";
	
	private static final String mapConfigTypeKey = "configType";
	
	private File mapFolder;
	
	private ConfigurationSerializableMapConfigSaver configSaver;
	
	private ConfigurationSerializableMapMetadataSaver metadataSaver;
	
	private Logger logger = new NekotineLogger(ConfigurationSerializableMapFolderFinder.class);
	
	/**
	 * Constructeur
	 * @param mapIndex fichier/dossier utilisé comme source de carte.
	 */
	public ConfigurationSerializableMapFolderFinder(File mapFolder) {
		if (mapFolder.isFile()) {
			throw new NotImplementedException("Le fichier donne doit etre un dossier");
		}else {
			configSaver = new ConfigurationSerializableMapConfigSaver(mapFolder);
			metadataSaver = new ConfigurationSerializableMapMetadataSaver(mapFolder);
		}
		this.mapFolder = mapFolder;
	}
	
	@Override
	public <T> boolean delete(MapHandle<T> handle) {
		var mapFile = new File(mapFolder, handle.getName() + fileExtension);
		return mapFile.delete();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<MapHandle<?>> list() {
		var list = new LinkedList<MapHandle<?>>();
		var config = new YamlConfiguration();
		for (var file : mapFolder.listFiles((f,fileName)->fileName.endsWith(fileExtension))) {
			try {
				config.load(file);
				var mapConfigType = Class.forName((String)config.get(mapConfigTypeKey));
				list.add(new MapHandle(
						mapConfigType,
						FileUtil.fileNameWithoutExtension(file),
						this,
						configSaver,
						metadataSaver));
			}catch(Exception e) {
				logger.log(Level.WARNING,
						"[ConfigurationSerializableMapFinder] > Erreur lors de la tentative de chargement de la map "+file.getName(), e);
			}
		}
		return list;
	}

	@Override
	public <T> MapHandle<T> add(Class<T> mapConfigType, String name) {
		var mapFile = new File(mapFolder, name + fileExtension);
		if (mapFile.exists()) {
			throw new RuntimeException(new FileAlreadyExistsException("Une carte de ce nom existe deja"));
		}
		var config = YamlConfiguration.loadConfiguration(mapFile);
		config.set(mapConfigTypeKey, mapConfigType.getName());
		try {
			config.save(mapFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new MapHandle<T>(mapConfigType, name, this, configSaver, metadataSaver);
	}

	@Override
	public <T> @Nullable MapHandle<T> findByName(@NotNull Class<T> type, @NotNull String name) {
		AssertUtil.nonNull(name, "Le nom de ne peut être null");
		var fileName = name + fileExtension;
		if (mapFolder.listFiles((dir, n) -> n.equals(fileName)).length > 0) {
			return new MapHandle<T>(type, name, this, configSaver, metadataSaver);
		}
		throw new RuntimeException(new FileNotFoundException("impossible de trouver le fichier "+fileName+" pour la map."));
	}

}
