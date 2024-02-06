package fr.nekotine.core.map;

import java.io.File;
import java.util.function.Consumer;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.map.command.IMapCommandGenerator;
import fr.nekotine.core.map.command.MapCommandGenerator;
import fr.nekotine.core.map.finder.ConfigurationSerializableMapFolderFinder;
import fr.nekotine.core.map.finder.IMapFinder;
import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.serialization.configurationserializable.ConfigurationSerializableAdapted;
import fr.nekotine.core.util.AsyncUtil;

public class OldMapModule extends IPluginModule{
	
	private final File defaultMapFolder = new File(Ioc.resolve(JavaPlugin.class).getDataFolder(), "Maps");
	
	private IMapFinder finder = new ConfigurationSerializableMapFolderFinder(defaultMapFolder);
	
	private IMapCommandGenerator generator = new MapCommandGenerator();
	
	public OldMapModule() {
		ConfigurationSerialization.registerClass(ConfigurationSerializableAdapted.class);
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
	
	public <T  extends ConfigurationSerializable> void getMapConfigAsync(MapHandle<T> handle, Consumer<T> thenSync) {
		AsyncUtil.runAsync(AsyncUtil.thenSync(handle::loadConfig, thenSync));
	}
	
	public <T  extends ConfigurationSerializable> void saveMapConfigAsync(MapHandle<T> handle, T config) {
		AsyncUtil.runAsync(()->handle.saveConfig(config));
	}
	
	public <T  extends ConfigurationSerializable> void saveMapConfigAsync(MapHandle<T> handle, T config, Runnable thenSync) {
		AsyncUtil.runAsync(AsyncUtil.thenSync(()->handle.saveConfig(config), thenSync));
	}
	
	public <T  extends ConfigurationSerializable> void getMapMetadataAsync(MapHandle<T> handle, Consumer<MapMetadata> thenSync) {
		AsyncUtil.runAsync(AsyncUtil.thenSync(handle::loadMetadata, thenSync));
	}
	
	public <T  extends ConfigurationSerializable> void saveMapMetadataAsync(MapHandle<T> handle, T config) {
		AsyncUtil.runAsync(()->handle.saveConfig(config));
	}
	
	public <T  extends ConfigurationSerializable> void addMapAsync(Class<T> mapConfigType, String name, Consumer<MapHandle<T>> thenSync){
		AsyncUtil.runAsync(AsyncUtil.thenSync(()->finder.add(mapConfigType, name), thenSync));
	}
	
	public <T  extends ConfigurationSerializable> void addMapAsync(Class<T> mapConfigType, String name){
		AsyncUtil.runAsync(()->finder.add(mapConfigType, name));
	}
	
	public <T  extends ConfigurationSerializable> void addMapAsync(Class<T> mapConfigType, String name, Consumer<MapHandle<T>> thenSync, Consumer<Exception> exceptionCallback){
		AsyncUtil.runAsync(AsyncUtil.thenSync(()->finder.add(mapConfigType, name), thenSync, exceptionCallback), exceptionCallback);
	}
	
	public <T  extends ConfigurationSerializable> void deleteMapAsync(Class<T> mapConfigType, String name, Runnable thenSync){
		AsyncUtil.pipe().async(() -> finder.findByName(mapConfigType, name).delete()).sync(thenSync).run();
	}
	
	public <T  extends ConfigurationSerializable> void deleteMapAsync(Class<T> mapConfigType, String name){
		AsyncUtil.runAsync(() -> finder.findByName(mapConfigType, name).delete());
	}
	
	public <T  extends ConfigurationSerializable> void deleteMapAsync(Class<T> mapConfigType, String name, Runnable thenSync, Consumer<Exception> exceptionCallback){
		AsyncUtil.pipe(exceptionCallback).async(() -> finder.findByName(mapConfigType, name).delete()).sync(thenSync).run();
	}
	
	public <T  extends ConfigurationSerializable> void deleteMapAsync(Class<T> mapConfigType, String name, Consumer<Exception> exceptionCallback){
		AsyncUtil.runAsync(() -> finder.findByName(mapConfigType, name).delete(), exceptionCallback);
	}
}
