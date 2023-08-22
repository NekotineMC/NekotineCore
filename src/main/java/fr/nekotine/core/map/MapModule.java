package fr.nekotine.core.map;

import java.io.File;
import java.util.function.Consumer;

import org.bukkit.configuration.serialization.ConfigurationSerialization;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.map.command.IMapCommandGenerator;
import fr.nekotine.core.map.command.MapCommandGenerator;
import fr.nekotine.core.map.finder.ConfigurationSerializableMapFolderFinder;
import fr.nekotine.core.map.finder.IMapFinder;
import fr.nekotine.core.map.save.configurationserialization.ConfigurationSerializableAdapter;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.util.AsyncUtil;

public class MapModule extends PluginModule{
	
	private final File defaultMapFolder = new File(NekotineCore.getAttachedPlugin().getDataFolder(), "Maps");
	
	private IMapFinder finder = new ConfigurationSerializableMapFolderFinder(defaultMapFolder);
	
	private IMapCommandGenerator generator = new MapCommandGenerator();
	
	public MapModule() {
		ConfigurationSerialization.registerClass(ConfigurationSerializableAdapter.class);
		ConfigurationSerialization.registerClass(MapMetadata.class);
	}
	
	public IMapFinder getMapFinder() {
		return finder;
	}

	public void setMapFinder(IMapFinder finder) {
		this.finder = finder;
	}

	public IMapCommandGenerator getGenerator() {
		return generator;
	}

	public void setGenerator(IMapCommandGenerator generator) {
		this.generator = generator;
	}
	
	public <T> void getMapConfigAsync(MapHandle<T> handle, Consumer<T> thenSync) {
		AsyncUtil.runAsync(AsyncUtil.thenSync(handle::loadConfig, thenSync));
	}
	
	public <T> void saveMapConfigAsync(MapHandle<T> handle, T config) {
		AsyncUtil.runAsync(()->handle.saveConfig(config));
	}
	
	public <T> void saveMapConfigAsync(MapHandle<T> handle, T config, Runnable thenSync) {
		AsyncUtil.runAsync(AsyncUtil.thenSync(()->handle.saveConfig(config), thenSync));
	}
	
	public <T> void getMapMetadataAsync(MapHandle<T> handle, Consumer<MapMetadata> thenSync) {
		AsyncUtil.runAsync(AsyncUtil.thenSync(handle::loadMetadata, thenSync));
	}
	
	public <T> void saveMapMetadataAsync(MapHandle<T> handle, T config) {
		AsyncUtil.runAsync(()->handle.saveConfig(config));
	}
	
	public <T> void addMapAsync(Class<T> mapConfigType, String name, Consumer<MapHandle<T>> thenSync){
		AsyncUtil.runAsync(AsyncUtil.thenSync(()->finder.add(mapConfigType, name), thenSync));
	}
	
	public <T> void addMapAsync(Class<T> mapConfigType, String name){
		AsyncUtil.runAsync(()->finder.add(mapConfigType, name));
	}
	
	public <T> void deleteMapAsync(Class<T> mapConfigType, String name, Runnable thenSync){
		AsyncUtil.runAsync(
				AsyncUtil.thenSync(AsyncUtil.then(() -> finder.findByName(mapConfigType, name), finder::delete), thenSync));
	}
	
	public <T> void deleteMapAsync(Class<T> mapConfigType, String name){
		AsyncUtil.runAsync(AsyncUtil.then(() -> finder.findByName(mapConfigType, name), finder::delete));
	}
}
