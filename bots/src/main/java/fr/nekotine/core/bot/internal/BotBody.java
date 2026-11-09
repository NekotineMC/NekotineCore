package fr.nekotine.core.bot.internal;

import fr.nekotine.core.bot.Bot;
import fr.nekotine.core.bot.BotState;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.UnconfiguredPipelineHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

/**
 * A bot's body: the fake connection on an {@link EmbeddedChannel} and the
 * {@link ServerPlayer} joined through it. Main thread only, except
 * {@link BotConnection#send}.
 *
 * <p>
 * Internal: hosts use {@link Bot} through {@code BotRoster}.
 */
public final class BotBody implements Bot {

	private final String name;

	private final EmbeddedChannel channel;

	private final BotConnection connection;

	private final ServerPlayer handle;

	private boolean loadedAckSent;

	private boolean removed;

	private BotBody(String name, EmbeddedChannel channel, BotConnection connection, ServerPlayer handle) {
		this.name = name;
		this.channel = channel;
		this.connection = connection;
		this.handle = handle;
	}

	/** @return the offline-mode UUID a player named {@code name} gets. */
	public static UUID offlineUuid(String name) {
		return UUIDUtil.createOfflinePlayerUUID(name);
	}

	/**
	 * Builds the fake connection and joins a new player named {@code name} at
	 * {@code location} through {@code PlayerList.placeNewPlayer}, which fires
	 * {@code PlayerJoinEvent}. Saved player data is not loaded.
	 */
	public static BotBody join(String name, Location location) throws Exception {
		var server = MinecraftServer.getServer();
		var level = ((CraftWorld) location.getWorld()).getHandle();
		var profile = UUIDUtil.createOfflineProfile(name);
		var connection = new BotConnection();
		// Handlers in place before register(), so channelActive sets
		// Connection.channel (ProtocolLib skips inactive channels).
		var channel = new EmbeddedChannel(false, false);
		var pipeline = channel.pipeline();
		Connection.configureInMemoryPipeline(pipeline, PacketFlow.SERVERBOUND);
		connection.configurePacketHandler(pipeline);
		channel.register();
		try {
			// handleDisconnection casts the address to InetSocketAddress.
			connection.address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
			// As the terminal configuration packet does on a real connection:
			// placeNewPlayer then installs the PLAY decoder, which ProtocolLib maps
			// packets by.
			pipeline.replace("decoder", "inbound_config", new UnconfiguredPipelineHandler.Inbound());
			connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE
					.bind(RegistryFriendlyByteBuf.decorator(server.registryAccess())));
			var player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
			player.setServerLevel(level);
			player.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			server.getPlayerList().placeNewPlayer(connection, player,
					CommonListenerCookie.createInitial(profile, false));
			var body = new BotBody(name, channel, connection, player);
			body.flush();
			return body;
		} catch (Exception | Error e) {
			if (channel.isOpen()) {
				channel.close();
			}
			throw e;
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Player player() {
		return handle.getBukkitEntity();
	}

	@Override
	public BotState state() {
		return BotState.ACTIVE;
	}

	/** @return whether the connection is still open. */
	public boolean isConnected() {
		return connection.isConnected();
	}

	/**
	 * One server tick: the connection tick (listener tick: position reset, player
	 * tick, keep-alive, throttlers), then the client acks, then the flush.
	 */
	public void tick() {
		connection.tick();
		if (!connection.isConnected()) {
			return;
		}
		sendAcks();
		flush();
	}

	private void sendAcks() {
		var teleportId = connection.takeTeleportId();
		if (teleportId != BotConnection.NO_TELEPORT) {
			writeInbound(new ServerboundAcceptTeleportationPacket(teleportId));
		}
		for (var keepAlive = connection.pollKeepAlive(); keepAlive != null; keepAlive = connection.pollKeepAlive()) {
			writeInbound(new ServerboundKeepAlivePacket(keepAlive));
		}
		if (!loadedAckSent) {
			loadedAckSent = true;
			writeInbound(new ServerboundPlayerLoadedPacket());
		}
	}

	/**
	 * Writes a decoded serverbound packet into the raw channel, as if the client
	 * sent it: ProtocolLib's receiving listeners see it, then the real handler
	 * runs. Main thread only.
	 */
	public void writeInbound(Packet<?> packet) {
		channel.writeInbound(packet);
	}

	private void flush() {
		if (channel.isOpen()) {
			channel.flushOutbound();
		}
		// Nothing reads what the server sends: drop the encoded buffers.
		channel.releaseOutbound();
	}

	/**
	 * Leaves through the server's own quit path: flush, close, then
	 * {@code handleDisconnection} (saves and fires {@code PlayerQuitEvent}). A
	 * second call is a no-op.
	 */
	public void remove() {
		if (removed) {
			return;
		}
		removed = true;
		flush();
		connection.disconnect(Component.literal("Bot removed"));
		connection.handleDisconnection();
		channel.releaseOutbound();
		channel.releaseInbound();
	}

	/** @return whether {@link #remove()} ran. */
	public boolean isRemoved() {
		return removed;
	}
}
