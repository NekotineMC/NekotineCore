package fr.nekotine.core.map.finder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.map.MapMetadata;
import fr.nekotine.core.map.save.saver.ConfigurationSerializableSaver;
import fr.nekotine.core.util.AssertUtil;

/**
 * Cette classe permet la recherche de carte sur un dossier ou avec un fichier index.
 * @author XxGoldenbluexX
 *
 */
public class ConfigurationSerializableMapFinder implements IMapFinder {

	private File mapIndex;
	
	private ConfigurationSerializableSaver saver;
	
	/**
	 * Constructeur
	 * @param mapIndex fichier/dossier utilisé comme source de carte.
	 */
	public ConfigurationSerializableMapFinder(File mapIndex) {
		if (mapIndex.isFile()) {
			throw new NotImplementedException("Cette fonctionnalitée n'est pas encore implémentée, veuillez donner un dossier.");
		}else {
			saver = new ConfigurationSerializableSaver(mapIndex);
		}
		this.mapIndex = mapIndex;
	}
	
	@Override
	public boolean delete(MapMetadata identifier) {// Doit il être ici ou dans le saver?
		return saver.delete(identifier);
	}
	
	@Override
	public List<MapMetadata> list() {
		if (mapIndex.isDirectory()) {
			return listFolder();
		}else {
			return listFile();
		}
	}

	@Override
	public boolean add(@NotNull MapMetadata identifier) {
		AssertUtil.nonNull(identifier, "L'identifier ne peut être null");
		AssertUtil.nonNull(identifier.getName(), "Le nom ne peut être null");
		AssertUtil.nonNull(identifier.getSaver(), "Le saver ne peut être null");
		AssertUtil.nonNull(identifier.getType(), "L'identifier ne peut être null");
		if (mapIndex.isDirectory()) {
			return addFolder(identifier);
		}else {
			return addFile(identifier);
		}
	}

	@Override
	public @Nullable MapMetadata findByName(@NotNull Class<?> type, @NotNull String name) {
		if (mapIndex.isDirectory()) {
			return findByNameFolder(name);
		}else {
			return findByNameFile(name);
		}
	}
	
	private List<MapMetadata> listFolder(){
		var list = new LinkedList<MapMetadata>();
		for (var file : mapIndex.listFiles()) {
			list.add(saver.loadFile(file).a());
		}
		return list;
	}
	
	private List<MapMetadata> listFile(){
		throw new NotImplementedException("Cette fonctionnalitée n'est pas encore implémentée, veuillez donner un dossier.");
	}

	private boolean addFolder(MapMetadata identifier) {
		var returns = new File(mapIndex, identifier.getName() + ConfigurationSerializableSaver.fileExtention).exists();
		if (identifier.getSaver() == null) {
			identifier.setSaver(saver);
		}
		saver.save(identifier, null);
		return returns;
	}
	
	private boolean addFile(MapMetadata identifier) {
		throw new NotImplementedException("Cette fonctionnalitée n'est pas encore implémentée, veuillez donner un dossier.");
	}
	
	private @Nullable MapMetadata findByNameFolder(@NotNull String name) {
		AssertUtil.nonNull(name, "Le nom de ne peut être null");
		for (var file : mapIndex.listFiles((dir, n) -> n.equals(name))) {
			return saver.loadFile(file).a();
		}
		return null;
	}
	
	private @Nullable MapMetadata findByNameFile(@NotNull String name) {
		throw new NotImplementedException("Cette fonctionnalitée n'est pas encore implémentée, veuillez donner un dossier.");
	}

}
