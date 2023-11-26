package fr.nekotine.core;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.ModuleManager;

public class NekotinePlugin {

	public void disable() {
		Ioc.resolve(ModuleManager.class).unloadAll();
	}
	
}
