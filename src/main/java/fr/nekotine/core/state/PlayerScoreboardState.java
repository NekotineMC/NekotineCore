package fr.nekotine.core.state;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerScoreboardState implements ItemState<Player>{

	private final Scoreboard scoreboard;
	
	public PlayerScoreboardState(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}
	
	@Override
	public void setup(Player item) {
		item.setScoreboard(scoreboard);
	}

	@Override
	public void teardown(Player item) {
		item.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

}
