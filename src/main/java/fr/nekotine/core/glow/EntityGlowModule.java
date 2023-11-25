package fr.nekotine.core.glow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	private Map<Player, Set<Integer>> map = new HashMap<>();
	
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
			if (!list.contains(eid)) {
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
		if (map.computeIfAbsent(viewer, p -> new HashSet<>()).add(glowed.getEntityId())) {
			triggerUpdate(glowed, viewer, true);
		}
	}
	
	public void unglowEntityFor(Entity glowed, Player viewer) {
		var set = map.get(viewer);
		if (set == null) {
			return;
		}
		if (set.remove(glowed.getEntityId())) {
			triggerUpdate(glowed, viewer, false);
		}
		if (set.isEmpty()) {
			map.remove(viewer);
		}
	}
	
	private void triggerUpdate(Entity glowed, Player viewer, boolean isGlowed) {
		var pmanager = ProtocolLibrary.getProtocolManager();
		var packet = pmanager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, glowed.getEntityId());
		var dataValues = new ArrayList<WrappedDataValue>(2);
		var serializer = WrappedDataWatcher.Registry.get(Byte.class);
		dataValues.add(new WrappedDataValue(0, serializer,(byte)(makeMaskFor(glowed) | (isGlowed ? entityMetadataGlowMask : 0x0)))); // Invisible + Glowing effect
		packet.getDataValueCollectionModifier().write(0, dataValues);
		pmanager.sendServerPacket(viewer, packet);
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
