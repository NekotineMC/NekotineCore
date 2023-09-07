package fr.nekotine.core.module;

import java.util.logging.Level;
import java.util.logging.Logger;

import fr.nekotine.core.logging.FormatingRemoteLogger;
import fr.nekotine.core.text.Text;
import fr.nekotine.core.util.Stopwatch;
import fr.nekotine.core.util.map.TypeHashMap;
import fr.nekotine.core.util.map.TypeMap;

/**
 * Ioc pour les modules (des genres de service quoi).
 * 
 * @author XxGoldenbluexX
 *
 */
public class ModuleManager {

	public Logger LOGGER = new FormatingRemoteLogger(Text.namedLoggerFormat("ModuleManager"));
	
	private TypeMap moduleMap = new TypeHashMap();

	public <M extends PluginModule> M get(Class<M> type) {
		if (!moduleMap.containsKey(type)) {
			load(type);
		}
		return moduleMap.get(type);
	}

	public <M extends PluginModule> void load(Class<M> type) {
		var name = type.getSimpleName();
		if (moduleMap.containsKey(type)) {
			LOGGER.log(Level.WARNING, "Le module " + name + " est deja charge");
			return;
		}
		LOGGER.log(Level.INFO, "Chargement du module "+name+"...");
		try (var watch = new Stopwatch(w -> LOGGER.log(Level.INFO, "Le module "+name+" est charge ("+w.elapsedMillis()+" ms)"))){
			var instance = type.getConstructor().newInstance();
			moduleMap.put(type, instance);
		}catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Impossible de charger le module "+name, e);
		}
	}

	public <M extends PluginModule> void unload(Class<M> type) {
		var name = type.getSimpleName();
		if (!moduleMap.containsKey(type)) {
			LOGGER.log(Level.WARNING, "Le module " + name + " n'est pas charge");
			return;
		}
		try (var watch = new Stopwatch(w -> LOGGER.log(Level.INFO, "Le module "+name+" est decharge ("+w.elapsedMillis()+"ms)"))){
			moduleMap.get(type).unload();
		}catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Impossible de decharger le module "+name, e);
		}
		moduleMap.remove(type);
	}
	
	@SuppressWarnings("unchecked")
	public void unloadAll() {
		var ite = moduleMap.keySet().iterator();
		while(ite.hasNext()) {
			var item = (Class<? extends PluginModule>)ite.next();
			var name = item.getSimpleName();
			try (var watch = new Stopwatch(w -> LOGGER.log(Level.INFO, "Le module "+name+" est decharge ("+w.elapsedMillis()+"ms)"))){
				moduleMap.get(item).unload();
			}catch(Exception e) {
				LOGGER.log(Level.SEVERE, "Impossible de decharger le module "+name, e);
			}
			ite.remove();
		}
	}
}
