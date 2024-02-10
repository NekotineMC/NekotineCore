package fr.nekotine.core.track;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.util.EventUtil;

public class ClientTrackModule extends PluginModule{
	private class ActionState {
		private boolean isSneaking;
		private boolean isSprinting;
		public ActionState(boolean isSneaking, boolean isSprinting) {
			setSneaking(isSneaking);
			setSprinting(isSprinting);
		}
		public boolean isSneaking() {
			return isSneaking;
		}
		public void setSneaking(boolean isSneaking) {
			this.isSneaking = isSneaking;
		}
		public boolean isSprinting() {
			return isSprinting;
		}
		public void setSprinting(boolean isSprinting) {
			this.isSprinting = isSprinting;
		}
	}
	private static final int STOP_SNEAKING = 1;
	private static final int STOP_SPRINTING = 4;
	/*private static final byte ENTITY_ACTION_INDEX = 0;	
	private static final byte SNEAKING_MASK = 0x02;
	private static final byte SPRINTING_MASK = 0x08;*/
	private HashMap<Player,ActionState> map = new HashMap<>();
	private PacketListener receiverAdapter = new PacketAdapter(Ioc.resolve(JavaPlugin.class),
			PacketType.Play.Client.POSITION,
			PacketType.Play.Client.POSITION_LOOK,
			PacketType.Play.Client.ENTITY_ACTION,
			PacketType.Play.Client.LOOK,
			PacketType.Play.Client.GROUND,
			PacketType.Play.Client.HELD_ITEM_SLOT) {
		
		public void onPacketReceiving(PacketEvent event) {
			Player player = event.getPlayer();
			if (map.containsKey(player)) {
				event.setCancelled(true);
				if(event.getPacketType() != PacketType.Play.Client.ENTITY_ACTION) return;
				
				var state = map.get(player);
				var action = event.getPacket().getPlayerActions().read(0);
				switch(action) {
				case START_SNEAKING:
					state.setSneaking(true);
					break;
				case STOP_SNEAKING:
					state.setSneaking(false);
					break;
				case START_SPRINTING:
					state.setSprinting(true);
					break;
				case STOP_SPRINTING:
					state.setSprinting(false);
					break;
				default:
					break;
				}
			}
		}
	};
	/*
	private PacketListener sendingAdapter = new PacketAdapter(Ioc.resolve(JavaPlugin.class),
			PacketType.Play.Server.ENTITY_METADATA) {
		public void onPacketSending(PacketEvent event) {
			Player player = event.getPlayer();
			if (map.containsKey(player)) {
				
				var newPacket = event.getPacket();
				var metadatas = newPacket.getDataValueCollectionModifier().read(0);
				var filteredMetadatas = metadatas.stream().filter(v -> v.getIndex() == ENTITY_ACTION_INDEX).toList();
				if(filteredMetadatas.size() > 0) {
					var metadata = filteredMetadatas.get(0);
					var infos = map.get(player);
					//for(var metadata : filteredMetadatas) {

						var value = (byte)metadata.getRawValue();
						value = (byte) (infos.isSneaking ? (value | SNEAKING_MASK) : (value & ~SNEAKING_MASK));
						value = (byte) (infos.isSprinting ? (value | SPRINTING_MASK) : (value & ~SPRINTING_MASK));
						metadata.setValue(value);
					
	
					//modifier.write(0,metadatas);
					event.setPacket(newPacket);
				}
			}
		}
	};*/
	
	public ClientTrackModule() {
		var pmanager = ProtocolLibrary.getProtocolManager();
		pmanager.addPacketListener(receiverAdapter);
		//pmanager.addPacketListener(sendingAdapter);
	}
	@Override
	protected void unload() {
		var pmanager = ProtocolLibrary.getProtocolManager();
		pmanager.removePacketListener(receiverAdapter);
		//pmanager.removePacketListener(sendingAdapter);
		map.clear();
	}
	public void untrack(Player player) {
		if (!map.containsKey(player)) {
			//Informe les joueurs et le serveur que le joueur s'est arrêté
			var state = new ActionState(player.isSneaking(), player.isSprinting());
			map.put(player,state);
			unsprint(player);
			unsneak(player);
		}
	}
	
	public void track(Player player) {
		if(!map.containsKey(player)) return;
		var state = map.remove(player);
		//On met à jour le serveur et les joueurs sur l'état du joueur
		EventUtil.call(new PlayerToggleSprintEvent(player, state.isSprinting()));
		EventUtil.call(new PlayerToggleSneakEvent(player, state.isSneaking()));
	}
	
	//
	
	private void unsprint(Player player) {
		EventUtil.call(new PlayerToggleSprintEvent(player, false));
		sendActionPacket(player, STOP_SPRINTING);
	}
	private void unsneak(Player player) {
		EventUtil.call(new PlayerToggleSneakEvent(player, false));
		sendActionPacket(player, STOP_SNEAKING);
	}
	private void sendActionPacket(Player player, int actionId) {
		var pmanager = ProtocolLibrary.getProtocolManager();
		var metadataPacket = pmanager.createPacket(PacketType.Play.Client.ENTITY_ACTION);
		
		metadataPacket.getIntegers().write(0, player.getEntityId());
		metadataPacket.getIntegers().write(1, actionId);

		Bukkit.getOnlinePlayers().stream().filter(p -> !p.equals(player)).forEach(
				p -> pmanager.sendServerPacket(p, metadataPacket));
	}
}
