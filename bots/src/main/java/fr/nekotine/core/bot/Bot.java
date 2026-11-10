package fr.nekotine.core.bot;

import org.bukkit.entity.Player;

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
 * Initially every key is released, the heading follows the look and the look is
 * the spawn location's yaw and pitch. A {@link BotState#FAILED} bot has every
 * key released and its heading back to the look, and sends nothing.
 *
 * <p>
 * Controls are main-thread only: off it, they throw
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
	 * Sets where the bot looks, in degrees (Minecraft convention); the server
	 * clamps the pitch to [-90, 90].
	 *
	 * @throws IllegalArgumentException
	 *             if {@code yaw} or {@code pitch} is not finite
	 */
	void setLook(float yaw, float pitch);

	/** @return the look yaw set by {@link #setLook}. */
	float getLookYaw();

	/** @return the look pitch set by {@link #setLook}. */
	float getLookPitch();
}
