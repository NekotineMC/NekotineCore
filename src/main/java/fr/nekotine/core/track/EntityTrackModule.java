package fr.nekotine.core.track;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.PluginModule;

public class EntityTrackModule extends PluginModule{
	private Map<Player, Set<Integer>> map = new HashMap<>();
	private PacketListener packetAdapter = new PacketAdapter(Ioc.resolve(JavaPlugin.class),PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
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
			event.setCancelled(true);
		}
	};
	public EntityTrackModule() {
		var pmanager = ProtocolLibrary.getProtocolManager();
		pmanager.addPacketListener(packetAdapter);
	}
	@Override
	protected void unload() {
		var pmanager = ProtocolLibrary.getProtocolManager();
		pmanager.removePacketListener(packetAdapter);
		map.clear();
	}
	public void untrackEntityFor(Entity untracked, Player viewer) {
		if (map.computeIfAbsent(viewer, p -> new HashSet<>()).add(untracked.getEntityId())) {
			//ptetre qqch
		}
	}
	
	public void trackEntityFor(Entity untracked, Player viewer) {
		var set = map.get(viewer);
		if (set == null) {
			return;
		}
		if (set.remove(untracked.getEntityId())) {
			//ptetre qqch
		}
		if (set.isEmpty()) {
			map.remove(viewer);
		}
	}
}
