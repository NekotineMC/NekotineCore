package fr.nekotine.core.bot.internal;

import io.netty.channel.ChannelFutureListener;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;

/**
 * The bot's server-side connection. {@link #send} may run on any thread: it
 * only records what the bot must answer or adopt (latest teleport id,
 * keep-alive ids, latest held slot, a respawn) in thread-safe fields; the bot
 * answers from its tick, on the main thread.
 */
final class BotConnection extends Connection {

	static final int NO_TELEPORT = -1;

	static final int NO_SLOT = -1;

	private final AtomicInteger pendingTeleportId = new AtomicInteger(NO_TELEPORT);

	private final Queue<Long> pendingKeepAlives = new ConcurrentLinkedQueue<>();

	private final AtomicInteger pendingHeldSlot = new AtomicInteger(NO_SLOT);

	private final AtomicBoolean pendingRespawn = new AtomicBoolean();

	BotConnection() {
		super(PacketFlow.SERVERBOUND);
	}

	@Override
	public void send(Packet<?> packet, ChannelFutureListener listener, boolean flush) {
		record(packet);
		super.send(packet, listener, flush);
	}

	private void record(Packet<?> packet) {
		switch (packet) {
			case ClientboundPlayerPositionPacket position -> pendingTeleportId.set(position.id());
			case ClientboundKeepAlivePacket keepAlive -> pendingKeepAlives.add(keepAlive.getId());
			case ClientboundSetHeldSlotPacket heldSlot -> pendingHeldSlot.set(heldSlot.slot());
			case ClientboundRespawnPacket _ -> pendingRespawn.set(true);
			case BundlePacket<?> bundle -> {
				for (Packet<?> sub : bundle.subPackets()) {
					record(sub);
				}
			}
			default -> {
			}
		}
	}

	/**
	 * @return the latest teleport id sent since the last call, or
	 *         {@link #NO_TELEPORT}.
	 */
	int takeTeleportId() {
		return pendingTeleportId.getAndSet(NO_TELEPORT);
	}

	/** @return the oldest unanswered keep-alive id, or {@code null}. */
	Long pollKeepAlive() {
		return pendingKeepAlives.poll();
	}

	/**
	 * @return the latest held slot the server set since the last call, or
	 *         {@link #NO_SLOT}.
	 */
	int takeHeldSlot() {
		return pendingHeldSlot.getAndSet(NO_SLOT);
	}

	/** @return whether the server sent a respawn since the last call. */
	boolean takeRespawn() {
		return pendingRespawn.getAndSet(false);
	}
}
