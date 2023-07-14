package fr.nekotine.core.map.finder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.map.MapIdentifier;
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
	private ConfigurationSerializableMapFinder(File mapIndex) {
		if (mapIndex.isFile()) {
			throw new NotImplementedException("Cette fonctionnalitée n'est pas encore implémentée, veuillez donner un dossier.");
		}else {
			saver = new ConfigurationSerializableSaver(mapIndex);
		}
		this.mapIndex = mapIndex;
	}
	
	@Override
	public boolean delete(MapIdentifier identifier) {// Doit il être ici ou dans le saver?
		return saver.delete(identifier);
	}
	
	@Override
	public List<MapIdentifier> list() {
		if (mapIndex.isDirectory()) {
			return listFolder();
		}else {
			return listFile();
		}
	}

	@Override
	public boolean add(@NotNull MapIdentifier identifier) {
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
	public @Nullable MapIdentifier findByName(@NotNull String name) {
		if (mapIndex.isDirectory()) {
			return findByNameFolder(name);
		}else {
			return findByNameFile(name);
		}
	}
	
	private List<MapIdentifier> listFolder(){
		var list = new LinkedList<MapIdentifier>();
		for (var file : mapIndex.listFiles()) {
			list.add(saver.loadFile(file).a());
		}
		return list;
	}
	
	private List<MapIdentifier> listFile(){
		throw new NotImplementedException("Cette fonctionnalitée n'est pas encore implémentée, veuillez donner un dossier.");
	}

	private boolean addFolder(MapIdentifier identifier) {
		var returns = new File(mapIndex, identifier.getName() + ConfigurationSerializableSaver.fileExtention).exists();
		saver.save(identifier, null);
		return returns;
	}
	
	private boolean addFile(MapIdentifier identifier) {
		throw new NotImplementedException("Cette fonctionnalitée n'est pas encore implémentée, veuillez donner un dossier.");
	}
	
	private @Nullable MapIdentifier findByNameFolder(@NotNull String name) {
		AssertUtil.nonNull(name, "Le nom de ne peut être null");
		for (var file : mapIndex.listFiles((dir, n) -> n.equals(name))) {
			return saver.loadFile(file).a();
		}
		return null;
	}
	
	private @Nullable MapIdentifier findByNameFile(@NotNull String name) {
		throw new NotImplementedException("Cette fonctionnalitée n'est pas encore implémentée, veuillez donner un dossier.");
	}

}
