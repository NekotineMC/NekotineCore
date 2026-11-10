package fr.nekotine.core.bot;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

/**
 * A server-side fake player spawned by a {@link BotRoster}. The server, other
 * plugins and ProtocolLib see it as a real online player.
 *
 * <h2>Controls</h2>
 *
 * <p>
 * Movement keys (forward, backward, left, right, jump, sneak, sprint), the
 * movement heading and the look are client controls: each persists until
 * changed. Once per tick, after the bot's {@link BotTickEvent}, the bot sends
 * what changed as real serverbound packets (input, then a sprint command when
 * sprint changed), runs the server's own movement physics along its heading,
 * and sends its new position with its look, which the server handles as from a
 * real client ({@code PlayerToggleSneakEvent}, {@code PlayerToggleSprintEvent},
 * {@code PlayerMoveEvent}, {@code PlayerJumpEvent}...). A control set inside a
 * {@link BotTickEvent} listener for this bot applies that same tick; set
 * anywhere else, it applies at the bot's next tick.
 *
 * <p>
 * The hotbar slot ({@link #setSlot}) is state too: sent once when it differs
 * from the last slot sent. When the server sets the slot itself (a plugin's
 * {@code setHeldItemSlot}, a cancelled {@code PlayerItemHeldEvent}), the bot
 * adopts it at its next tick, before its {@link BotTickEvent}, as a client
 * does: {@link #getSlot()} returns it and nothing is sent back.
 *
 * <p>
 * Hand actions ({@link #useItem}, {@link #useItemOn}, {@link #leftClick},
 * {@link #startBreaking}, {@link #attack}, {@link #interact},
 * {@link #swapHands}), {@link #sendCommand} and {@link #respawn} are one-shots:
 * each is queued, then sent exactly once, in call order, as the packet a client
 * sends, in the apply step after the move packet (so the server ray-traces with
 * the look sent that tick) and after the slot. Like the other controls, a
 * one-shot queued inside a {@link BotTickEvent} listener for this bot is sent
 * that same tick; queued anywhere else, at the bot's next tick (one queued by a
 * listener of an event a one-shot fires waits for the next tick too). The
 * library checks no reach: the server's own checks decide, as for a client. The
 * server ignores hand packets while the bot is dead and until it has loaded
 * after a respawn. Writes stop as soon as the connection closes; queued
 * one-shots are then dropped, as they are on {@link BotState#FAILED}, and a
 * one-shot called on a failed or removed bot is dropped at once.
 *
 * <p>
 * After each respawn the server sends (death or world change), the bot answers
 * with the player-loaded ack and resends its held keys, as a fresh client
 * player does.
 *
 * <p>
 * Initially every key is released, the heading follows the look and the look is
 * the spawn location's yaw and pitch. {@link BotState#FAILED} sets the getters
 * to released keys and no heading, but sends nothing, so the server keeps the
 * last input it got (e.g. the player stays sneaking), and drops queued
 * one-shots.
 *
 * <p>
 * Not modelled yet: velocity the server or a plugin applies (setVelocity,
 * knockback, explosions) and creative or spectator flight up and down.
 *
 * <p>
 * Setters are main-thread only: off it, they throw
 * {@link IllegalStateException}.
 */
public interface Bot {

	/** @return the bot's player name, as given to {@link BotRoster#spawn}. */
	String name();

	/** @return the bot's Bukkit player handle. */
	Player player();

	/** @return the bot's current state. */
	BotState state();

	/** Holds or releases the forward key. */
	void setForward(boolean forward);

	/** @return whether the forward key is held. */
	boolean isForward();

	/** Holds or releases the backward key. */
	void setBackward(boolean backward);

	/** @return whether the backward key is held. */
	boolean isBackward();

	/** Holds or releases the strafe-left key. */
	void setLeft(boolean left);

	/** @return whether the strafe-left key is held. */
	boolean isLeft();

	/** Holds or releases the strafe-right key. */
	void setRight(boolean right);

	/** @return whether the strafe-right key is held. */
	boolean isRight();

	/**
	 * Holds or releases the jump key. Held, the bot jumps whenever it can, like a
	 * player holding space (swims up in fluids, climbs ladders).
	 */
	void setJump(boolean jump);

	/** @return whether the jump key is held. */
	boolean isJump();

	/** Holds or releases the sneak key. */
	void setSneak(boolean sneak);

	/** @return whether the sneak key is held. */
	boolean isSneak();

	/**
	 * Holds or releases the sprint key. The bot sends the matching sprint command
	 * on each change; client sprint rules (hunger, walls) are not modelled.
	 */
	void setSprint(boolean sprint);

	/** @return whether the sprint key is held. */
	boolean isSprint();

	/**
	 * Sets the yaw, in degrees (Minecraft convention), the movement keys move the
	 * bot along, independently of where it looks.
	 *
	 * @param yaw
	 *            the movement yaw, or {@code null} to follow the look yaw (the
	 *            default)
	 * @throws IllegalArgumentException
	 *             if {@code yaw} is not finite
	 */
	void setHeading(Float yaw);

	/**
	 * @return the movement yaw set by {@link #setHeading}, or {@code null} when the
	 *         movement follows the look yaw
	 */
	Float getHeading();

	/**
	 * Sets where the bot looks, in degrees (Minecraft convention). The getters
	 * return the values set; the server clamps the pitch to [-90, 90].
	 *
	 * @throws IllegalArgumentException
	 *             if {@code yaw} or {@code pitch} is not finite
	 */
	void setLook(float yaw, float pitch);

	/** @return the look yaw set by {@link #setLook}. */
	float getLookYaw();

	/** @return the look pitch set by {@link #setLook}. */
	float getLookPitch();

	/**
	 * Selects a hotbar slot. Persists; sent when it differs from the last slot sent
	 * (fires {@code PlayerItemHeldEvent}).
	 *
	 * @param slot
	 *            the hotbar slot, 0 to 8
	 * @throws IllegalArgumentException
	 *             if {@code slot} is not in 0-8
	 */
	void setSlot(int slot);

	/**
	 * @return the selected hotbar slot: the last one set, or the one the server set
	 *         since
	 */
	int getSlot();

	/**
	 * Right-clicks with the item in {@code hand}, at the look sent this tick (fires
	 * {@code PlayerInteractEvent} with {@code RIGHT_CLICK_AIR}, or
	 * {@code RIGHT_CLICK_BLOCK} when a block is in reach along the look, then uses
	 * the item). One-shot; the server ignores it with an empty hand. Sends no
	 * swing.
	 *
	 * @param hand
	 *            {@link EquipmentSlot#HAND} or {@link EquipmentSlot#OFF_HAND}
	 * @throws IllegalArgumentException
	 *             if {@code hand} is another slot
	 */
	void useItem(EquipmentSlot hand);

	/**
	 * Right-clicks {@code face} of {@code block}, at the centre of that face, with
	 * the item in {@code hand} (fires {@code PlayerInteractEvent} with
	 * {@code RIGHT_CLICK_BLOCK}, then uses the block or places the item). One-shot.
	 * Sends no swing.
	 *
	 * @param hand
	 *            {@link EquipmentSlot#HAND} or {@link EquipmentSlot#OFF_HAND}
	 * @throws IllegalArgumentException
	 *             if {@code hand} is another slot, {@code face} is not one of the
	 *             six cube faces or {@code block} is in another world than the bot
	 */
	void useItemOn(Block block, BlockFace face, EquipmentSlot hand);

	/**
	 * Swings the main hand at nothing in particular: the server ray-traces along
	 * the look sent this tick and fires {@code PlayerInteractEvent} with
	 * {@code LEFT_CLICK_AIR} when it hits nothing. One-shot. To hit a block or an
	 * entity, use {@link #startBreaking} or {@link #attack}.
	 */
	void leftClick();

	/**
	 * Starts breaking {@code face} of {@code block}, then swings the main hand
	 * (fires {@code PlayerInteractEvent} with {@code LEFT_CLICK_BLOCK}). One-shot.
	 * Only starts: a block that breaks instantly (creative mode, soft blocks)
	 * breaks; longer breaks are not continued.
	 *
	 * @throws IllegalArgumentException
	 *             if {@code face} is not one of the six cube faces or {@code block}
	 *             is in another world than the bot
	 */
	void startBreaking(Block block, BlockFace face);

	/**
	 * Attacks {@code target}, then swings the main hand (fires
	 * {@code EntityDamageByEntityEvent} when the server accepts the hit). One-shot.
	 *
	 * @throws IllegalArgumentException
	 *             if {@code target} is in another world than the bot
	 */
	void attack(Entity target);

	/**
	 * Right-clicks {@code target} with the item in {@code hand}, at the centre of
	 * its bounding box, sneaking if the sneak key is held (fires
	 * {@code PlayerInteractAtEntityEvent} and {@code PlayerInteractEntityEvent}).
	 * One-shot. Sends no swing.
	 *
	 * @param hand
	 *            {@link EquipmentSlot#HAND} or {@link EquipmentSlot#OFF_HAND}
	 * @throws IllegalArgumentException
	 *             if {@code hand} is another slot or {@code target} is in another
	 *             world than the bot
	 */
	void interact(Entity target, EquipmentSlot hand);

	/**
	 * Swaps the items of the main and off hands (fires
	 * {@code PlayerSwapHandItemsEvent}). One-shot.
	 */
	void swapHands();

	/**
	 * Runs {@code command} as the bot, as a client typing it in chat: the server
	 * fires {@code PlayerCommandPreprocessEvent}, then runs the command with the
	 * bot as sender. One-shot. Unsigned: a command with signed arguments (e.g.
	 * {@code /msg}) is refused when the server enforces secure profiles.
	 *
	 * @param command
	 *            the command line, with or without its leading {@code /}
	 * @throws IllegalArgumentException
	 *             if {@code command} is null or blank, leading {@code /} aside
	 */
	void sendCommand(String command);

	/**
	 * Asks to respawn, as the death screen's button does (fires
	 * {@code PlayerRespawnEvent}). One-shot; the server ignores it while the bot is
	 * alive.
	 */
	void respawn();
}
