package fr.nekotine.core.glow;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.TeamColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ScoreboardTeamCreatePacketWrapper {

	private String teamName;

	private ChatFormatting color;

	private Collection<Entity> entities = new LinkedList<>();

	public Packet<?> buildPacket() {
		var dummyScoreboard = new Scoreboard();
		var team = new PlayerTeam(dummyScoreboard, teamName);
		team.setColor(Optional.ofNullable(TeamColor.valueOf(color.name())));
		team.setDisplayName(Component.literal(teamName));
		team.setNameTagVisibility(Team.Visibility.NEVER);
		team.setCollisionRule(Team.CollisionRule.NEVER);
		var playerNames = entities.stream().map(e -> {
			if (e instanceof Player player) {
				return player.getName();
			}
			return e.getUniqueId().toString();
		}).collect(Collectors.toCollection(LinkedList::new));
		team.getPlayers().addAll(playerNames);
		return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public void setColor(ChatFormatting color) {
		this.color = color;
	}

	public Collection<Entity> getEntities() {
		return entities;
	}
}
