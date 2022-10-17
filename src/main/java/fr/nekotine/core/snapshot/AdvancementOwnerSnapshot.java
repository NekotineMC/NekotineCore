package fr.nekotine.core.snapshot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

public class AdvancementOwnerSnapshot implements Snapshot<Player>{

	private Map<Advancement, Snapshot<AdvancementProgress> > advancements;
	
	/**
	 * @implNote c'est l'équivalent du {@link fr.nekotine.core.snapshot.PlayerStatusSnaphot#deepSnapshot deepSnapshot}
	 */
	@Override
	public Snapshot<Player> snapshot(Player item) {
		deepSnapshot(item);
		return this;
	}

	@Override
	public Snapshot<Player> deepSnapshot(Player item) {
		advancements = new HashMap<>(140);// J'ai compté 102 achevements sur le wiki
		var ite = Bukkit.getServer().advancementIterator();
		while (ite.hasNext()) {
			var adv = ite.next();
			var advProg = item.getAdvancementProgress(adv);
			advancements.put(adv, new AdvancementProgressSnapshot().deepSnapshot(advProg));
		}
		return this;
	}

	@Override
	public void patch(Player item) {
		for (var adv : advancements.keySet()) {
			var advProg = item.getAdvancementProgress(adv);
			advancements.get(adv).patch(advProg);
		}
	}

}
