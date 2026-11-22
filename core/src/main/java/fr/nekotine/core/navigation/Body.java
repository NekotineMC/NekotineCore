package fr.nekotine.core.navigation;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * A body navigation strategies (walkers, melee controllers, brains) drive,
 * whatever it is (a bot, later a mob): world-space reads and
 * {@link BodyControls} per {@link ControlPriority}.
 *
 * <h2>Tick</h2>
 *
 * <p>
 * A body ticks once per server tick and fires its own tick event (for a bot,
 * {@code BotTickEvent}). Strategies run only inside that event, never from
 * their own tick listener: controls written in it apply that same tick; written
 * anywhere else, they apply at the body's next tick.
 *
 * <h2>Priority</h2>
 *
 * <p>
 * Any writer may write any group. Per control group (movement, aim, hands) and
 * per tick, the highest {@link ControlPriority} that wrote anything in that
 * group wins: only its writes in that group apply, in call order; lower writes
 * in that group are dropped. Controls the winner did not set keep their
 * persisted value, and a tick with no write in a group leaves that group as it
 * is. No write fails for a priority reason: there is no claim, release or
 * ownership. A losing write is dropped without notice, so a strategy that
 * writes only on change can fall out of sync (e.g. its release of forward, lost
 * to a brain's movement write that tick, leaves forward held): strategies
 * should write their whole group state every tick they drive that group. Writes
 * to a body that no longer ticks (e.g. a failed or removed bot) are dropped.
 *
 * <p>
 * Main thread only.
 */
public interface Body {

	/** @return a copy of the body's location, with its world. */
	Location getLocation();

	/**
	 * @return the body's velocity, in blocks per tick, world-space, as the body's
	 *         own movement produced it: an adapter may return a simulated velocity
	 *         that ignores velocity the server or a plugin applies (a bot returns
	 *         its client velocity, which ignores knockback and {@code setVelocity},
	 *         see {@code Bot})
	 */
	Vector getVelocity();

	/** @return whether the body stands on the ground. */
	boolean isOnGround();

	/** @return the body's look yaw, in degrees (Minecraft convention). */
	float getLookYaw();

	/** @return the body's look pitch, in degrees (Minecraft convention). */
	float getLookPitch();

	/**
	 * @return the writer of the body's controls at {@code priority}; the same
	 *         writer for every call with the same priority
	 */
	BodyControls controls(ControlPriority priority);
}
