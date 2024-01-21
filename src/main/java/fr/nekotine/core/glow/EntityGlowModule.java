package fr.nekotine.core.glow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.PluginModule;

public class EntityGlowModule extends PluginModule {

	private static final int entityMetadataGlowIndex = 0; // https://wiki.vg/Entity_metadata#Entity
	
	private static final byte entityMetadataGlowMask = 0x40; // https://wiki.vg/Entity_metadata#Entity
	
	private Map<Player, Map<Integer,TeamColor>> map = new HashMap<>();
	
	private PacketListener packetAdapter = new PacketAdapter(Ioc.resolve(JavaPlugin.class),PacketType.Play.Server.ENTITY_METADATA) {
		@Override
		public void onPacketSending(PacketEvent event) {
			PacketContainer packet = event.getPacket();
			Player viewer = event.getPlayer();
			var list = map.get(viewer);
			if (list == null) {
				return;
			}
			var eid = packet.getIntegers().read(0);
			if (!list.containsKey(eid)) {
				return;
			}
			var newPacket = packet.deepClone();
			var values = newPacket.getDataValueCollectionModifier().read(0);
			var optionalValue =  values.stream().filter(v -> v.getIndex() == entityMetadataGlowIndex).findFirst();
			if (optionalValue.isPresent()) {
				var value = optionalValue.get();
				value.setRawValue((byte)((byte)value.getRawValue() | entityMetadataGlowMask)); // Add glow to bitmask
			}else {
				var serializer = WrappedDataWatcher.Registry.get(Byte.class);
				values.add(new WrappedDataValue(0, serializer, entityMetadataGlowMask));
			}
			event.setPacket(newPacket);
		}
	};
	
	public EntityGlowModule() {
		var pmanager = ProtocolLibrary.getProtocolManager();
		pmanager.addPacketListener(packetAdapter);
	}
	
	@Override
	protected void unload() {
		var pmanager = ProtocolLibrary.getProtocolManager();
		pmanager.removePacketListener(packetAdapter);
		map.clear();
	}
	
	public void glowEntityFor(Entity glowed, Player viewer) {
		glowEntityFor(glowed, viewer, null);
	}
	
	public void glowEntityFor(Entity glowed, Player viewer, @Nullable TeamColor color) {

		var eid = glowed.getEntityId();
		var set = map.computeIfAbsent(viewer, p -> new HashMap<>());
		if (!set.containsKey(eid) || set.get(eid) != color) {
			set.put(eid, color);
			triggerUpdate(glowed, viewer, true, color);
		}
	}
	
	public void unglowEntityFor(Entity glowed, Player viewer) {
		var set = map.get(viewer);
		if (set == null) {
			return;
		}
		if (set.containsKey(glowed.getEntityId())) {
			set.remove(glowed.getEntityId());
			triggerUpdate(glowed, viewer, false, null);
		}
		if (set.isEmpty()) {
			map.remove(viewer);
		}
	}
	
	private void triggerUpdate(Entity glowed, Player viewer, boolean isGlowed, @Nullable TeamColor color) {
		var pmanager = ProtocolLibrary.getProtocolManager();
		var metadataPacket = pmanager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		metadataPacket.getIntegers().write(0, glowed.getEntityId());
		var dataValues = new ArrayList<WrappedDataValue>(2);
		var serializer = WrappedDataWatcher.Registry.get(Byte.class);
		dataValues.add(new WrappedDataValue(0, serializer,(byte)(makeMaskFor(glowed) | (isGlowed ? entityMetadataGlowMask : 0x0)))); // Invisible + Glowing effect
		metadataPacket.getDataValueCollectionModifier().write(0, dataValues);
		pmanager.sendServerPacket(viewer, metadataPacket);
		if (color != null) {
			var teamName = "EntityGlowModule"+color+"Team";
			var tp = new ScoreboardTeamCreatePacketWrapper();
			tp.setTeamName(teamName);
			tp.setColor(color);
			tp.getEntities().add(glowed);
			var p = tp.buildPacket();
			pmanager.sendServerPacket(viewer, p);
		}
	}
	
	private byte makeMaskFor(Entity entity) {
		var value = (byte)0x0;
		// Values from https://wiki.vg/Entity_metadata#Entity
		value |= entity.isVisualFire() ? 0x01 : 0x0; // is on fire
		value |= entity.isSneaking() ? 0x02 : 0x0; // is crouching
		// 0x04 is unused (previously riding)
		if (entity instanceof Player player) {
			value |= player.isSprinting() ? 0x08 : 0x0; // is sprinting
		}
		if (entity instanceof LivingEntity living) {
			value |= living.isSwimming() ? 0x10 : 0x0; // is swimming
			value |= living.isInvisible() ? 0x20 : 0x0; // is invisible
			value |= living.isGliding() ? 0x80 : 0x0; // is glowing
		}
		value |= entity.isGlowing() ? entityMetadataGlowMask : 0x0; // is glowing
		
		return value;
	}
}
