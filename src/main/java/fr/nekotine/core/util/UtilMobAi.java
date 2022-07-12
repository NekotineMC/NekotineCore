package fr.nekotine.core.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;

/**
 * Fonctions utilitaires pour la gestion du système d'ia de minecraft.
 * Cette classe enveloppe potentiellement des fonctions de papermc.
 * @author XxGoldenbluexX
 *
 */
public final class UtilMobAi {

	/**
	 * Retire tout les goals du brain du mob donné.
	 * @param mob Le mob à qui retirer les goals.
	 */
	public static void clearBrain(Mob mob) {
		Bukkit.getServer().getMobGoals().removeAllGoals(mob);;
	}
	
}
