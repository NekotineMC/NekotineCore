package fr.nekotine.core.visibility;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import fr.nekotine.core.module.PluginModule;
import fr.nekotine.core.module.annotation.ModuleNameAnnotation;

@ModuleNameAnnotation(Name = "EntityVisibilityModule")
public class EntityVisibilityModule extends PluginModule{
	
	private PacketListener metadataListener;
	
	/**
	 * Status actuel des joueurs (envoyé au joueurs durant le dernier tick).
	 */
	private Set<VisibilityData> currentVisibilityStatus; // Map<JoueurCaché, CachéPour>
	
	private Set<VisibilityData> toUpdateVisibilityStatus; // Map<JoueurCaché, CachéPour>
	
	private BukkitRunnable updateVisibilityStatus;
	
	BukkitTask updateTask;
	
	public EntityVisibilityModule() {
		currentVisibilityStatus = new HashSet<>();
		toUpdateVisibilityStatus = new HashSet<>();
		updateVisibilityStatus = new BukkitRunnable() {
			@Override
			public void run() {
				ProtocolManager pmanager = ProtocolLibrary.getProtocolManager();
				PacketContainer updatePacket;
				Iterator<VisibilityData> iterator = toUpdateVisibilityStatus.iterator(); 
				while (iterator.hasNext()) {
					VisibilityData vd = iterator.next();
					currentVisibilityStatus.remove(vd);
					currentVisibilityStatus.add(vd);
					// Envoi du packet correspondant au joueurs.
					if (vd.isHidden) {
						updatePacket = pmanager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
						updatePacket.getIntegerArrays().write(0, new int[]{vd.hidden.getEntityId()});
					}else {
						updatePacket = pmanager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
						updatePacket.getIntegers().write(0, vd.hidden.getEntityId()); // Entity Id for protocol
						updatePacket.getUUIDs().write(0, vd.hidden.getUniqueId()); // UUID
						updatePacket.getIntegers().write(1, 116); // Entity Type Id specified in https://wiki.vg/Entity_metadata#Mobs
						Location playerloc = vd.hidden.getLocation();
						updatePacket.getDoubles().write(0, playerloc.getX());
						updatePacket.getDoubles().write(1, playerloc.getY());
						updatePacket.getDoubles().write(2, playerloc.getZ());
						updatePacket.getBytes().write(0, (byte) (playerloc.getYaw() * 256.0F / 360.0F)); // https://wiki.vg/Protocol#Spawn_Entity
						updatePacket.getBytes().write(1, (byte) (playerloc.getPitch() * 256.0F / 360.0F));
						Vector playervelocity = vd.hidden.getVelocity();
						updatePacket.getIntegers().write(2, (int) (playervelocity.getX() * 8000.0D)); // https://github.com/dmulloy2/PacketWrapper/blob/master/PacketWrapper/src/main/java/com/comphenix/packetwrapper/WrapperPlayServerSpawnEntityLiving.java
						updatePacket.getIntegers().write(3, (int) (playervelocity.getY() * 8000.0D));
						updatePacket.getIntegers().write(4, (int) (playervelocity.getZ() * 8000.0D));
					}
					try {
						pmanager.sendServerPacket(vd.blind, updatePacket);
					} catch (InvocationTargetException e) {
					}
					iterator.remove();
				}
			}
		};
	}
	
	public void hideFrom(Entity hidden, Player blind) {
		VisibilityData vd = new VisibilityData(hidden, blind, true);
		toUpdateVisibilityStatus.remove(vd);
		toUpdateVisibilityStatus.add(vd);
	}
	
	public void showFrom(Entity showed, Player blind) {
		VisibilityData vd = new VisibilityData(showed, blind, false);
		toUpdateVisibilityStatus.remove(vd);
		toUpdateVisibilityStatus.add(vd);
	}
	
	@Override
	protected void onEnable() {
		super.onEnable();
		metadataListener = new PacketAdapter(getPlugin(), PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				if (currentVisibilityStatus.stream().anyMatch(
						vs -> vs.blind.equals(event.getPlayer()) && vs.hidden.getEntityId() == packet.getIntegers().read(0)
						))
				{
					event.setCancelled(true);
				}
			}
		};
		ProtocolLibrary.getProtocolManager().addPacketListener(metadataListener);
		updateTask = updateVisibilityStatus.runTaskTimer(getPlugin(), 0, 1);
	}

	@Override
	protected void onDisable() {
		if (updateTask != null && !updateTask.isCancelled()) {
			updateTask.cancel();
		}
		ProtocolLibrary.getProtocolManager().removePacketListener(metadataListener);
		super.onDisable();
	}
	
	private class VisibilityData{
		
		private Entity hidden;
		
		private Player blind;
		
		private boolean isHidden;
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof VisibilityData) {
				VisibilityData other = (VisibilityData) obj;
				return hidden.equals(other.hidden) && blind.equals(other.blind);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return hidden.hashCode() + blind.hashCode();
		}
		
		private VisibilityData(Entity hidden, Player blind, boolean isHidden) {
			this.hidden = hidden;
			this.blind = blind;
			this.isHidden = isHidden;
		}
		
		private VisibilityData(Entity hidden, Player blind) {
			this(hidden, blind, false);
		}
	}
	
}
