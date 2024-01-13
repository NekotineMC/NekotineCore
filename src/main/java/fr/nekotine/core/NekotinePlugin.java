package fr.nekotine.core;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.ModuleManager;
import fr.nekotine.core.util.DebugUtil;

public class NekotinePlugin {

	public void disable() {
		DebugUtil.clearDebugInstances();
		Ioc.resolve(ModuleManager.class).unloadAll();
	}
	
}
