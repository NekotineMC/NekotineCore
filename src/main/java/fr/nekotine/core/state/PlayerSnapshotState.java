package fr.nekotine.core.state;

import fr.nekotine.core.snapshot.PlayerStatusSnaphot;
import fr.nekotine.core.snapshot.Snapshot;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class PlayerSnapshotState implements ItemState<Player> {

	private Map<Player, Snapshot<Player>> snapshots = new HashMap<>();

	@Override
	public void setup(Player item) {
		snapshots.put(item, new PlayerStatusSnaphot().snapshot(item));
	}

	@Override
	public void teardown(Player item) {
		snapshots.get(item).patch(item);
	}
}
