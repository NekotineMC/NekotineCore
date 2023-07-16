package fr.nekotine.core.map;

import java.io.File;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.map.command.IMapCommandGenerator;
import fr.nekotine.core.map.command.MapCommandGenerator;
import fr.nekotine.core.map.finder.ConfigurationSerializableMapFinder;
import fr.nekotine.core.map.finder.IMapFinder;
import fr.nekotine.core.module.PluginModule;

public class MapModule extends PluginModule{
	
	private final File defaultMapFolder = new File(NekotineCore.getAttachedPlugin().getDataFolder(), "Maps");
	
	private IMapFinder finder = new ConfigurationSerializableMapFinder(defaultMapFolder);
	
	private IMapCommandGenerator generator = new MapCommandGenerator();
	
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
}
