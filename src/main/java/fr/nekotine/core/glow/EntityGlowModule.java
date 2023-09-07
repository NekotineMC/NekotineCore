package fr.nekotine.core.glow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.module.PluginModule;

public class EntityGlowModule extends PluginModule {

	private static final int entityMetadataGlowIndex = 0; // https://wiki.vg/Entity_metadata#Entity
	
	private static final byte entityMetadataGlowMask = 0x40; // https://wiki.vg/Entity_metadata#Entity
	
	private Map<Player, Set<Integer>> map = new HashMap<>();
	
	private PacketListener packetAdapter = new PacketAdapter(NekotineCore.getAttachedPlugin(),PacketType.Play.Server.ENTITY_METADATA) {
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
			var values = packet.getDataValueCollectionModifier().read(0);
			var optionalValue =  values.stream().filter(v -> v.getIndex() == entityMetadataGlowIndex).findFirst();
			if (optionalValue.isPresent()) {
				var value = optionalValue.get();
				value.setRawValue((byte)value.getRawValue() | entityMetadataGlowMask); // Add glow to bitmask
			}else {
				var serializer = WrappedDataWatcher.Registry.get(Byte.class);
				values.add(new WrappedDataValue(0, serializer, entityMetadataGlowMask));
			}
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
			triggerUpdate(glowed, viewer);
		}
	}
	
	public void unglowEntityFor(Entity glowed, Player viewer) {
		var set = map.get(viewer);
		if (set == null) {
			return;
		}
		if (set.remove(glowed.getEntityId())) {
			triggerUpdate(glowed, viewer);
		}
		if (set.isEmpty()) {
			map.remove(viewer);
		}
	}
	
	private void triggerUpdate(Entity glowed, Player viewer) {
		var pmanager = ProtocolLibrary.getProtocolManager();
		var packet = pmanager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, glowed.getEntityId());
		pmanager.sendServerPacket(viewer, packet);
	}
	
}
