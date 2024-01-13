package fr.nekotine.core.glow;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;

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
	
	public void setColor(TeamColor color) {
		packet.getOptionalStructures().read(0).map((structure) ->
        structure.getEnumModifier(TeamColor.class,
                MinecraftReflection.getMinecraftClass("EnumChatFormat"))
                .write(0, color));
	}
	
	public Collection<Entity> getEntities() {
		return entities;
	}
	
}
