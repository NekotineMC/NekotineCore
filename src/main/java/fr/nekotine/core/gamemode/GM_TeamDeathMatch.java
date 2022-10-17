package fr.nekotine.core.gamemode;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.nekotine.core.game.Game;
import fr.nekotine.core.game.GameTeam;
import fr.nekotine.core.lobby.GameModeIdentifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class GM_TeamDeathMatch extends Game{

	@Override
	public void addPlayerToOptimalTeam(Player player) {
		List<GameTeam> teamClone = new LinkedList<>(_teams);
		teamClone.sort((a,b) -> a.getPlayerList().size() - b.getPlayerList().size());
		teamClone.get(0).addPlayer(player);
	}
	
	public static final GameModeIdentifier IDENTIFIER = new GameModeIdentifier("tdm", Component.text("Match à mort en équipe").color(TextColor.color(255, 45, 45))) {
		@Override
		public Game generateTypedGame() {
			return new GM_TeamDeathMatch();
		}
	};

	@Override
	protected void setup() {
	}

	@Override
	protected void end() {
	}

	@Override
	protected void collectGameData() {
	}

	@Override
	protected void asyncManageGameData() {
	}
	
}
