package fr.nekotine.core.gamemode;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.nekotine.core.lobby.GameMode;
import fr.nekotine.core.minigame.Game;
import fr.nekotine.core.minigame.GameTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class GM_TeamDeathMatch extends Game{

	@Override
	public void addPlayerToOptimalTeam(Player player) {
		List<GameTeam> teamClone = new LinkedList<>(_teams);
		teamClone.sort((a,b) -> a.getPlayerList().size() - b.getPlayerList().size());
		teamClone.get(0).addPlayer(player);
	}
	
	public static final GameMode IDENTIFIER = new GameMode("tdm", Component.text("Match à mort en équipe").color(TextColor.color(255, 45, 45))) {
		@Override
		public Game generateTypedGame() {
			return new GM_TeamDeathMatch();
		}
	};
	
}
