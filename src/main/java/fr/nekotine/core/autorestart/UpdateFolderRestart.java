package fr.nekotine.core.autorestart;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.logging.NekotineLogger;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public class UpdateFolderRestart implements AutoCloseable{

	private final ComponentLogger logger = NekotineLogger.make();
	
	private WatchService watchService;
	
	private BukkitTask task;
	
	public UpdateFolderRestart() {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			var updateFolder = Bukkit.getUpdateFolderFile();
			updateFolder.toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			task = new BukkitRunnable() {
				@Override
				public void run() {
					// Check for update
					try {
						var key = watchService.take(); // This call is blocking until file changed
						if (key.pollEvents().size() > 0) {
							new BukkitRunnable() {
								public void run() {
									Bukkit.getServer().restart();
								};
							}.runTaskLater(Ioc.resolve(JavaPlugin.class), 10);
						}
					}
					catch(ClosedWatchServiceException _) {
						logger.info("La lecture de fichier du système de restart automatique à été désactivée");
					}
					catch (Exception e) {
						logger.error("Erreur du system de restart automatique: ", e);
					}
				}
			}.runTaskAsynchronously(Ioc.resolve(JavaPlugin.class));
		} catch (Exception e) {
			logger.error("Erreur du system de restart automatique: ", e);
		}
	}
	
	@Override
	public void close() {
		if (!task.isCancelled()) {
			task.cancel();
		}
		try {
			watchService.close();
		} catch (IOException e) {
			logger.error("Erreur lors de la désactivation du service de redémarrage de serveur", e);
		}
	}

}
