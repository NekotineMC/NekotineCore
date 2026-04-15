package fr.nekotine.core.module;

import fr.nekotine.core.logging.NekotineLogger;
import fr.nekotine.core.util.Stopwatch;
import fr.nekotine.core.util.map.TypeHashMap;
import fr.nekotine.core.util.map.TypeMap;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

/**
 * Ioc pour les modules (des genres de service quoi).
 *
 * @author XxGoldenbluexX
 */
public class ModuleManager {

	private ComponentLogger logger = NekotineLogger.make();

	private TypeMap moduleMap = new TypeHashMap();

	public <M extends IPluginModule> M get(Class<M> type) {
		if (!moduleMap.containsKey(type)) {
			load(type);
		}
		return moduleMap.get(type);
	}

	public <M extends IPluginModule> boolean tryLoad(Class<M> type) {
		if (moduleMap.containsKey(type)) {
			return false;
		}
		load(type);
		return true;
	}

	public <M extends IPluginModule> void load(Class<M> type) {
		var name = type.getSimpleName();
		if (moduleMap.containsKey(type)) {
			logger.warn("Le module " + name + " est deja charge");
			return;
		}
		logger.info("Chargement du module " + name + "...");
		try (var watch = new Stopwatch(
				w -> logger.info("Le module " + name + " est charge (" + w.elapsedMillis() + " ms)"))) {
			var instance = type.getConstructor().newInstance();
			moduleMap.put(type, instance);
		} catch (Exception e) {
			logger.error("Impossible de charger le module " + name, e);
		}
	}

	public <M extends IPluginModule> void unload(Class<M> type) {
		var name = type.getSimpleName();
		if (!moduleMap.containsKey(type)) {
			logger.warn("Le module " + name + " n'est pas charge");
			return;
		}
		try (var watch = new Stopwatch(
				w -> logger.info("Le module " + name + " est decharge (" + w.elapsedMillis() + "ms)"))) {
			moduleMap.get(type).unload();
			moduleMap.remove(type);
		} catch (Exception e) {
			logger.error("Impossible de decharger le module " + name, e);
		}
	}

	@SuppressWarnings("unchecked")
	public void unloadAll() {
		var ite = moduleMap.keySet().iterator();
		while (ite.hasNext()) {
			var item = (Class<? extends IPluginModule>) ite.next();
			var name = item.getSimpleName();
			try (var watch = new Stopwatch(
					w -> logger.info("Le module " + name + " est decharge (" + w.elapsedMillis() + "ms)"))) {
				moduleMap.get(item).unload();
			} catch (Exception e) {
				logger.error("Impossible de decharger le module " + name, e);
			}
			ite.remove();
		}
	}
}
