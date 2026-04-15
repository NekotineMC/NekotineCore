package fr.nekotine.core.game.team;

import fr.nekotine.core.util.collection.ObservableCollection;
import java.util.LinkedList;
import java.util.List;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Team extends ObservableCollection<Player> implements ForwardingAudience {

	private final List<Player> players = new LinkedList<>();

	public Team() {
		setInnerCollection(players);
	}

	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return players;
	}
}
