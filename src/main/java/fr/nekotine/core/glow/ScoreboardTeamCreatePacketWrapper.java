package fr.nekotine.core.glow;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedTeamParameters;

public class ScoreboardTeamCreatePacketWrapper {

	private final PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
	
	private Collection<Entity> entities = new LinkedList<>();
	
	public PacketContainer buildPacket() {
		packet.getIntegers().write(0, 0);
		var entitiew = entities.stream().map(e -> {
			var entityName = e.getUniqueId().toString();
			if (e instanceof Player player) {
				entityName = player.getName();
			}
			return entityName;
		}).collect(Collectors.toCollection(LinkedList::new));
		packet.getSpecificModifier(Collection.class).write(0, entitiew);
		return packet;
	}
	
	public void setTeamName(String teamName) {
		packet.getStrings().write(0, teamName);
	}
	
	public void setColor(EnumWrappers. ChatFormatting color) {
		var param = WrappedTeamParameters.newBuilder()
				.displayName(WrappedChatComponent.fromText(color.name()))
				.prefix(WrappedChatComponent.fromText(""))
				.suffix(WrappedChatComponent.fromText(""))
				.nametagVisibility("never")
				.collisionRule("never")
				.color(color)
				.build();
		packet.getOptionalTeamParameters().write(0, Optional.of(param));
	}
	
	public Collection<Entity> getEntities() {
		return entities;
	}
	
}
