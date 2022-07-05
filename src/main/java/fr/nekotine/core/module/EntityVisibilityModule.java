package fr.nekotine.core.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

public class EntityVisibilityModule extends PluginModule implements Listener{
	
	private PacketListener metadataListener;
	
	private Map<Player, Set<Player>> hiddenMap; // Map<JoueurCaché, CachéPour>
	
	public EntityVisibilityModule(JavaPlugin plugin) {
		super("EntityVisibilityModule");
		hiddenMap = new HashMap<>();
		metadataListener = new PacketAdapter(plugin, PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
			@Override
			public void onPacketSending(PacketEvent event) {
				super.onPacketSending(event);
				PacketContainer packet = event.getPacket();
				Optional<Player> hiddenOne = hiddenMap.keySet().stream().filter((p) -> p.getEntityId() == packet.getIntegers().read(0)).findFirst();
				if (hiddenOne.isPresent() && hiddenMap.get(hiddenOne.get()).contains(event.getPlayer())) {
					event.setCancelled(true);
				}
			}
		};
	}
	
	@Override
	protected void onEnable() {
		super.onEnable();
		ProtocolLibrary.getProtocolManager().addPacketListener(metadataListener);
		Bukkit.getPluginManager().registerEvents(this, getPlugin());
	}

	@Override
	protected void onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListener(metadataListener);
		super.onDisable();
	}
	
	public static boolean IsPlayerIdMatching(int id) {
		return false;
	}
}
