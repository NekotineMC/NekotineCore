package fr.nekotine.core.track;

import java.util.Collection;
import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.PluginModule;

public class ClientTrackModule extends PluginModule{
	private Collection<Player> map = new LinkedList<>();
	private PacketListener packetAdapter = new PacketAdapter(Ioc.resolve(JavaPlugin.class),
			PacketType.Play.Client.POSITION,
			PacketType.Play.Client.POSITION_LOOK,
			PacketType.Play.Client.LOOK,
			PacketType.Play.Client.GROUND,
			PacketType.Play.Client.HELD_ITEM_SLOT) {
		
		public void onPacketReceiving(PacketEvent event) {
			Player player = event.getPlayer();
			if (map.contains(player)) {
				event.setCancelled(true);
			}
		}
	};
	
	public ClientTrackModule() {
		var pmanager = ProtocolLibrary.getProtocolManager();
		pmanager.addPacketListener(packetAdapter);
	}
	@Override
	protected void unload() {
		var pmanager = ProtocolLibrary.getProtocolManager();
		pmanager.removePacketListener(packetAdapter);
		map.clear();
	}
	public void untrack(Player player) {
		if (!map.contains(player)) {
			map.add(player);
		}
	}
	
	public void track(Player player) {
		map.remove(player);
	}
}
