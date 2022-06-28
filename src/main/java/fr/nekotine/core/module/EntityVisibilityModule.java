package fr.nekotine.core.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

public class EntityVisibilityModule extends PluginModule implements Listener{
	
	private PacketListener metadataListener;
	
	private Map<Player, Set<Player>> hiddenMap; // Map<JoueurCaché,CachéPour>
	
	public EntityVisibilityModule(JavaPlugin plugin) {
		super(plugin, "EntityVisibilityModule");
		hiddenMap = new HashMap<>();
		metadataListener = new PacketAdapter(plugin, PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
			@Override
			public void onPacketSending(PacketEvent event) {
				super.onPacketSending(event);
				PacketContainer packet = event.getPacket();
				
				if (hiddenMap.containsKey(event.getPlayer()) && false/*packet.getEntityId()==packet.getIntegers().read(0)*/){
					
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
}
