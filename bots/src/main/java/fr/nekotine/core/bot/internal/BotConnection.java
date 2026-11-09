package fr.nekotine.core.bot.internal;

import io.netty.channel.ChannelFutureListener;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

/**
 * The bot's server-side connection. {@link #send} may run on any thread: it
 * only records what the bot must answer (latest teleport id, keep-alive ids) in
 * thread-safe fields; the bot answers from its tick, on the main thread.
 */
final class BotConnection extends Connection {

	static final int NO_TELEPORT = -1;

	private final AtomicInteger pendingTeleportId = new AtomicInteger(NO_TELEPORT);

	private final Queue<Long> pendingKeepAlives = new ConcurrentLinkedQueue<>();

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
}
