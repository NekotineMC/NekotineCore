package fr.nekotine.core.navigation;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;

/**
 * The controls of a {@link Body}, in three groups:
 * <ul>
 * <li>movement: {@link #setForward}, {@link #setBackward}, {@link #setLeft},
 * {@link #setRight}, {@link #setJump}, {@link #setSneak}, {@link #setSprint},
 * {@link #setHeading};</li>
 * <li>aim: {@link #setLook};</li>
 * <li>hands: {@link #setSlot}, {@link #useItem}, {@link #useItemOn},
 * {@link #leftClick}, {@link #startBreaking}, {@link #attack},
 * {@link #interact}, {@link #swapHands}.</li>
 * </ul>
 *
 * <p>
 * Keys, heading, look and slot persist until changed; hand actions other than
 * {@link #setSlot} are one-shots. Through {@link Body#controls}, a write never
 * fails for a priority reason: it may only be dropped by a higher
 * {@link ControlPriority} writing the same group in the same tick.
 *
 * <p>
 * Main thread only: off it, every write throws {@link IllegalStateException}.
 * Arguments are checked when called, before any priority applies.
 */
public interface BodyControls {

	/** Holds or releases the forward key. Movement group. */
	void setForward(boolean forward);

	/** Holds or releases the backward key. Movement group. */
	void setBackward(boolean backward);

	/** Holds or releases the strafe-left key. Movement group. */
	void setLeft(boolean left);

	/** Holds or releases the strafe-right key. Movement group. */
	void setRight(boolean right);

	/** Holds or releases the jump key. Movement group. */
	void setJump(boolean jump);

	/** Holds or releases the sneak key. Movement group. */
	void setSneak(boolean sneak);

	/** Holds or releases the sprint key. Movement group. */
	void setSprint(boolean sprint);

	/**
	 * Sets the yaw, in degrees (Minecraft convention), the movement keys move the
	 * body along, independently of where it looks. Movement group.
	 *
	 * @param yaw
	 *            the movement yaw, or {@code null} to follow the look yaw
	 * @throws IllegalArgumentException
	 *             if {@code yaw} is not finite
	 */
	void setHeading(Float yaw);

	/**
	 * Sets where the body looks, in degrees (Minecraft convention). Aim group.
	 *
	 * @throws IllegalArgumentException
	 *             if {@code yaw} or {@code pitch} is not finite
	 */
	void setLook(float yaw, float pitch);

	/**
	 * Selects a hotbar slot. Hands group.
	 *
	 * @param slot
	 *            the hotbar slot, 0 to 8
	 * @throws IllegalArgumentException
	 *             if {@code slot} is not in 0-8
	 */
	void setSlot(int slot);

	/**
	 * Right-clicks with the item in {@code hand}, at the look. One-shot, hands
	 * group.
	 *
	 * @throws IllegalArgumentException
	 *             if {@code hand} is not {@link EquipmentSlot#HAND} or
	 *             {@link EquipmentSlot#OFF_HAND}
	 */
	void useItem(EquipmentSlot hand);

	/**
	 * Right-clicks {@code face} of {@code block} with the item in {@code hand}.
	 * One-shot, hands group.
	 *
	 * @throws IllegalArgumentException
	 *             if {@code hand} is another slot than {@link EquipmentSlot#HAND}
	 *             or {@link EquipmentSlot#OFF_HAND}, {@code face} is not one of the
	 *             six cube faces or {@code block} is in another world than the body
	 */
	void useItemOn(Block block, BlockFace face, EquipmentSlot hand);

	/** Swings the main hand at nothing in particular. One-shot, hands group. */
	void leftClick();

	/**
	 * Starts breaking {@code face} of {@code block}. One-shot, hands group.
	 *
	 * @throws IllegalArgumentException
	 *             if {@code face} is not one of the six cube faces or {@code block}
	 *             is in another world than the body
	 */
	void startBreaking(Block block, BlockFace face);

	/**
	 * Attacks {@code target}. One-shot, hands group.
	 *
	 * @throws IllegalArgumentException
	 *             if {@code target} is in another world than the body
	 */
	void attack(Entity target);

	/**
	 * Right-clicks {@code target} with the item in {@code hand}. One-shot, hands
	 * group.
	 *
	 * @throws IllegalArgumentException
	 *             if {@code hand} is another slot than {@link EquipmentSlot#HAND}
	 *             or {@link EquipmentSlot#OFF_HAND}, or {@code target} is in
	 *             another world than the body
	 */
	void interact(Entity target, EquipmentSlot hand);

	/** Swaps the items of the main and off hands. One-shot, hands group. */
	void swapHands();
}
