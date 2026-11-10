package fr.nekotine.core.bot.internal;

import fr.nekotine.core.bot.Bot;
import fr.nekotine.core.bot.BotState;
import fr.nekotine.core.bot.BotWipe;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

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

	private final BotPlayer handle;

	private final BotWipe wipe;

	private BotState state = BotState.ACTIVE;

	private boolean loadedAckSent;

	private boolean removed;

	private RuntimeException fault;

	// Controls, applied by the next apply step.
	private boolean forward;

	private boolean backward;

	private boolean left;

	private boolean right;

	private boolean jump;

	private boolean sneak;

	private boolean sprint;

	private Float heading;

	private float lookYaw;

	private float lookPitch;

	/** The selected hotbar slot. */
	private int slot;

	/** The last hotbar slot sent, or adopted from the server. */
	private int sentSlot;

	/**
	 * One-shots queued since the last apply step, in call order: each builds its
	 * packet when sent.
	 */
	private final List<Supplier<Packet<?>>> oneShots = new ArrayList<>();

	/** The client's prediction sequence, pre-incremented from 0. */
	private int sequence;

	/** The last input sent; the server starts from an empty one. */
	private Input sentInput = Input.EMPTY;

	private BotBody(String name, EmbeddedChannel channel, BotConnection connection, BotPlayer handle, BotWipe wipe,
			float lookYaw, float lookPitch) {
		this.name = name;
		this.channel = channel;
		this.connection = connection;
		this.handle = handle;
		this.wipe = wipe;
		this.lookYaw = lookYaw;
		this.lookPitch = lookPitch;
		this.slot = handle.getInventory().getSelectedSlot();
		this.sentSlot = slot;
	}

	/** @return the offline-mode UUID a player named {@code name} gets. */
	public static UUID offlineUuid(String name) {
		return UUIDUtil.createOfflinePlayerUUID(name);
	}

	/**
	 * Builds the fake connection and joins a new player named {@code name} at
	 * {@code location} through {@code PlayerList.placeNewPlayer}, which fires
	 * {@code PlayerJoinEvent}. Saved player data is not loaded. {@code wipe} says
	 * which saved files {@link #wipe} deletes once the bot has left.
	 */
	public static BotBody join(String name, Location location, BotWipe wipe) throws Exception {
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
			var player = new BotPlayer(server, level, profile, ClientInformation.createDefault());
			player.setServerLevel(level);
			player.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			server.getPlayerList().placeNewPlayer(connection, player,
					CommonListenerCookie.createInitial(profile, false));
			var body = new BotBody(name, channel, connection, player, wipe, location.getYaw(), location.getPitch());
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
		return state;
	}

	@Override
	public void setForward(boolean forward) {
		requireMainThread();
		this.forward = forward;
	}

	@Override
	public boolean isForward() {
		return forward;
	}

	@Override
	public void setBackward(boolean backward) {
		requireMainThread();
		this.backward = backward;
	}

	@Override
	public boolean isBackward() {
		return backward;
	}

	@Override
	public void setLeft(boolean left) {
		requireMainThread();
		this.left = left;
	}

	@Override
	public boolean isLeft() {
		return left;
	}

	@Override
	public void setRight(boolean right) {
		requireMainThread();
		this.right = right;
	}

	@Override
	public boolean isRight() {
		return right;
	}

	@Override
	public void setJump(boolean jump) {
		requireMainThread();
		this.jump = jump;
	}

	@Override
	public boolean isJump() {
		return jump;
	}

	@Override
	public void setSneak(boolean sneak) {
		requireMainThread();
		this.sneak = sneak;
	}

	@Override
	public boolean isSneak() {
		return sneak;
	}

	@Override
	public void setSprint(boolean sprint) {
		requireMainThread();
		this.sprint = sprint;
	}

	@Override
	public boolean isSprint() {
		return sprint;
	}

	@Override
	public void setHeading(Float yaw) {
		requireMainThread();
		if (yaw != null && !Float.isFinite(yaw)) {
			throw new IllegalArgumentException(name + " heading must be finite: " + yaw);
		}
		this.heading = yaw;
	}

	@Override
	public Float getHeading() {
		return heading;
	}

	@Override
	public void setLook(float yaw, float pitch) {
		requireMainThread();
		if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
			throw new IllegalArgumentException(name + " look must be finite: " + yaw + ", " + pitch);
		}
		this.lookYaw = yaw;
		this.lookPitch = pitch;
	}

	@Override
	public float getLookYaw() {
		return lookYaw;
	}

	@Override
	public float getLookPitch() {
		return lookPitch;
	}

	@Override
	public void setSlot(int slot) {
		requireMainThread();
		if (slot < 0 || slot > 8) {
			throw new IllegalArgumentException(name + " slot must be in 0-8: " + slot);
		}
		this.slot = slot;
	}

	@Override
	public int getSlot() {
		return slot;
	}

	@Override
	public void useItem(EquipmentSlot hand) {
		requireMainThread();
		var interactionHand = hand(hand);
		// The look sent with this tick's move.
		queue(() -> new ServerboundUseItemPacket(interactionHand, ++sequence, lookYaw, lookPitch));
	}

	@Override
	public void useItemOn(Block block, BlockFace face, EquipmentSlot hand) {
		requireMainThread();
		var pos = position(block);
		var direction = direction(face);
		var interactionHand = hand(hand);
		var hit = new BlockHitResult(Vec3.atCenterOf(pos).relative(direction, 0.5), direction, pos, false);
		queue(() -> new ServerboundUseItemOnPacket(interactionHand, hit, ++sequence));
	}

	@Override
	public void leftClick() {
		requireMainThread();
		queue(() -> new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
	}

	@Override
	public void startBreaking(Block block, BlockFace face) {
		requireMainThread();
		var pos = position(block);
		var direction = direction(face);
		queue(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos,
				direction, ++sequence));
		queue(() -> new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
	}

	@Override
	public void attack(Entity target) {
		requireMainThread();
		int id = entityId(target);
		queue(() -> new ServerboundAttackPacket(id));
		queue(() -> new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
	}

	@Override
	public void interact(Entity target, EquipmentSlot hand) {
		requireMainThread();
		int id = entityId(target);
		var interactionHand = hand(hand);
		queue(() -> {
			var at = target.getBoundingBox().getCenter().subtract(target.getLocation().toVector());
			// The sneak key as sent with this tick's input.
			return new ServerboundInteractPacket(id, interactionHand, new Vec3(at.getX(), at.getY(), at.getZ()),
					sentInput.shift());
		});
	}

	@Override
	public void swapHands() {
		requireMainThread();
		queue(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
				BlockPos.ZERO, Direction.DOWN));
	}

	@Override
	public void sendCommand(String command) {
		requireMainThread();
		if (command == null || command.isBlank()) {
			throw new IllegalArgumentException(name + " command must not be blank");
		}
		var line = command.startsWith("/") ? command.substring(1) : command;
		if (line.isBlank()) {
			throw new IllegalArgumentException(name + " command must not be blank");
		}
		queue(() -> new ServerboundChatCommandPacket(line));
	}

	@Override
	public void respawn() {
		requireMainThread();
		queue(() -> new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
	}

	/**
	 * Queues a one-shot for the next apply step; dropped at once when the bot is
	 * {@link BotState#FAILED} or removed, since it never applies again.
	 */
	private void queue(Supplier<Packet<?>> oneShot) {
		if (state == BotState.FAILED || removed) {
			return;
		}
		oneShots.add(oneShot);
	}

	private InteractionHand hand(EquipmentSlot hand) {
		if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
			throw new IllegalArgumentException(name + " hand must be HAND or OFF_HAND: " + hand);
		}
		return CraftEquipmentSlot.getHand(hand);
	}

	private BlockPos position(Block block) {
		Objects.requireNonNull(block, "block");
		requireSameWorld(block.getWorld(), "block");
		return new BlockPos(block.getX(), block.getY(), block.getZ());
	}

	private Direction direction(BlockFace face) {
		var direction = CraftBlock.blockFaceToNotch(face);
		if (direction == null) {
			throw new IllegalArgumentException(name + " face must be one of the six cube faces: " + face);
		}
		return direction;
	}

	private int entityId(Entity target) {
		Objects.requireNonNull(target, "target");
		requireSameWorld(target.getWorld(), "target");
		return target.getEntityId();
	}

	private void requireSameWorld(World world, String what) {
		var own = player().getWorld();
		if (!world.equals(own)) {
			throw new IllegalArgumentException(
					name + " " + what + " is in world " + world.getName() + ", not in the bot's " + own.getName());
		}
	}

	private static void requireMainThread() {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("Bot controls must be used from the main thread");
		}
	}

	/**
	 * Marks the bot {@link BotState#FAILED}, releases every key (heading back to
	 * the look) and drops queued one-shots: its roster no longer runs
	 * {@link #tickBeforeEvent()} or {@link #tickAfterEvent()} for it, only
	 * {@link #tickFailed()}, so it sends no input and runs no physics.
	 */
	public void fail() {
		state = BotState.FAILED;
		forward = false;
		backward = false;
		left = false;
		right = false;
		jump = false;
		sneak = false;
		sprint = false;
		heading = null;
		oneShots.clear();
	}

	/** @return whether the connection is still open. */
	public boolean isConnected() {
		return connection.isConnected();
	}

	/**
	 * Test seam, internal: makes the next {@link #tickBeforeEvent()} throw
	 * {@code fault} before doing anything, as a library failure would.
	 */
	public void failNextTick(RuntimeException fault) {
		this.fault = fault;
	}

	/**
	 * First half of a server tick, before the bot's tick event: the connection tick
	 * (listener tick: position reset, player tick, keep-alive, throttlers), then
	 * the client acks if the connection is still open.
	 */
	public void tickBeforeEvent() {
		var injected = fault;
		if (injected != null) {
			fault = null;
			throw injected;
		}
		connection.tick();
		if (!connection.isConnected()) {
			return;
		}
		sendAcks();
	}

	/**
	 * Second half of a server tick, after the bot's tick event: the apply step
	 * (changed inputs, sprint command, physics, move, slot, one-shots) if the
	 * connection is still open, then the flush.
	 */
	public void tickAfterEvent() {
		if (connection.isConnected()) {
			apply();
		}
		flush();
	}

	/**
	 * Sends the controls as a client would: the input if it changed (and the sprint
	 * command if sprint changed), then one client physics step along the heading
	 * and the resulting position with the look, then the slot if it changed, then
	 * the queued one-shots in call order. Stops writing as soon as the connection
	 * closes; what is left is dropped.
	 */
	private void apply() {
		var input = new Input(forward, backward, left, right, jump, sneak, sprint);
		if (!input.equals(sentInput)) {
			var previous = sentInput;
			sentInput = input;
			writeInbound(new ServerboundPlayerInputPacket(input));
			if (input.sprint() != previous.sprint() && connection.isConnected()) {
				writeInbound(new ServerboundPlayerCommandPacket(handle,
						input.sprint()
								? ServerboundPlayerCommandPacket.Action.START_SPRINTING
								: ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
			}
		}
		if (!connection.isConnected()) {
			return;
		}
		var target = handle.simulate(input, heading != null ? heading : lookYaw, lookPitch);
		writeInbound(new ServerboundMovePlayerPacket.PosRot(target, lookYaw, lookPitch, handle.clientOnGround(),
				handle.clientHorizontalCollision()));
		if (slot != sentSlot && connection.isConnected()) {
			sentSlot = slot;
			writeInbound(new ServerboundSetCarriedItemPacket(slot));
		}
		// Taken first: one-shots queued by listeners of the events these packets
		// fire wait for the next tick.
		var queued = List.copyOf(oneShots);
		oneShots.clear();
		for (var oneShot : queued) {
			if (!connection.isConnected()) {
				return;
			}
			writeInbound(oneShot.get());
		}
	}

	/**
	 * The only tick of a {@link BotState#FAILED} bot: flushes and releases what the
	 * server sent it (so a kick still closes the channel), ignoring any error.
	 */
	public void tickFailed() {
		try {
			flush();
		} catch (RuntimeException e) {
			// Already failed and logged once: stay quiet.
		}
	}

	private void sendAcks() {
		var teleportId = connection.takeTeleportId();
		if (teleportId != BotConnection.NO_TELEPORT) {
			// The client lands on the teleport target with no velocity.
			handle.resetClientVelocity();
			writeInbound(new ServerboundAcceptTeleportationPacket(teleportId));
		}
		for (var keepAlive = connection.pollKeepAlive(); keepAlive != null; keepAlive = connection.pollKeepAlive()) {
			writeInbound(new ServerboundKeepAlivePacket(keepAlive));
		}
		var heldSlot = connection.takeHeldSlot();
		if (heldSlot != BotConnection.NO_SLOT) {
			// The client adopts the server's slot without answering.
			slot = heldSlot;
			sentSlot = heldSlot;
		}
		boolean respawned = connection.takeRespawn();
		if (respawned) {
			// A fresh client player: it resends its held keys.
			sentInput = Input.EMPTY;
		}
		if (!loadedAckSent || respawned) {
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
		try {
			flush();
		} catch (RuntimeException e) {
			// A broken flush (often what failed the bot) must not stop the quit.
		}
		connection.disconnect(Component.literal("Bot removed"));
		connection.handleDisconnection();
		channel.releaseOutbound();
		channel.releaseInbound();
	}

	/**
	 * Deletes the saved files selected by the bot's {@link BotWipe}. Call it once
	 * {@link #remove()} returned, never from a quit listener: the server saves
	 * after the quit event. A missing file is not an error; a failed delete logs
	 * one warning starting with the bot's name.
	 */
	public void wipe(ComponentLogger logger) {
		var server = MinecraftServer.getServer();
		var uuid = handle.getUUID().toString();
		var files = new ArrayList<Path>();
		if (wipe.state()) {
			var data = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
			files.addAll(List.of(data.resolve(uuid + ".dat"), data.resolve(uuid + ".dat_old")));
		}
		if (wipe.statistics()) {
			files.add(server.getWorldPath(LevelResource.PLAYER_STATS_DIR).resolve(uuid + ".json"));
			files.add(server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(uuid + ".json"));
		}
		for (var file : files) {
			try {
				Files.deleteIfExists(file);
			} catch (IOException | RuntimeException e) {
				logger.warn(name + " could not delete its saved file " + file, e);
			}
		}
	}

	/** @return whether {@link #remove()} ran. */
	public boolean isRemoved() {
		return removed;
	}
}
