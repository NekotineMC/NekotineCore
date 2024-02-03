package fr.nekotine.core.map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.map.finder.IMapFinder;
import fr.nekotine.core.map.save.config.IMapConfigSaver;
import fr.nekotine.core.map.save.metadata.IMapMetadataSaver;

public class MapHandle<MapConfigType extends ConfigurationSerializable>{
	
	private @NotNull Class<MapConfigType> configType;
	private @NotNull String name;
	private @NotNull IMapFinder finder;
	private @NotNull IMapConfigSaver configSaver;
	private @NotNull IMapMetadataSaver metadataSaver;
	
	public MapHandle(@NotNull Class<MapConfigType> configType,
			@NotNull String name,
			@NotNull IMapFinder finder,
			@NotNull IMapConfigSaver configSaver,
			@NotNull IMapMetadataSaver metadataSaver){
		this.configType = configType;
		this.name = name;
		this.finder = finder;
		this.configSaver = configSaver;
		this.metadataSaver = metadataSaver;
	}
	
	public String getName() {
		return name;
	}
	
	public void setMapFinder(IMapFinder finder) {
		this.finder = finder;
	}
	
	public void setConfigSaver(IMapConfigSaver saver) {
		configSaver = saver;
	}
	
	public void setMetadataSaver(IMapMetadataSaver saver) {
		metadataSaver = saver;
	}
	
	public Class<MapConfigType> getConfigType(){
		return configType;
	}

	public MapConfigType loadConfig() {
		return configSaver.load(this);
	}
	
	public void saveConfig(MapConfigType map) {
		configSaver.save(this, map);
	}
	
	public MapMetadata loadMetadata() {
		return metadataSaver.loadMetadata(this);
	}
	
	public void saveMetadata(MapMetadata metadata) {
		metadataSaver.saveMetadata(this, metadata);
	}
	
	public void delete() {
		finder.delete(this);
		configSaver.delete(this);
		metadataSaver.delete(this);
	}
	
}
