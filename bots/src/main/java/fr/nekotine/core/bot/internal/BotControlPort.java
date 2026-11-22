package fr.nekotine.core.bot.internal;

import fr.nekotine.core.bot.BotState;
import fr.nekotine.core.navigation.Body;
import fr.nekotine.core.navigation.BodyControls;
import fr.nekotine.core.navigation.ControlArbiter;
import fr.nekotine.core.navigation.ControlPriority;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

/**
 * A bot's {@link Body}: reads from the bot and its client state, and an
 * {@link ControlArbiter} whose winning writes {@link BotBody} replays 1:1 into
 * its own setters at the start of its apply half, right after its tick event.
 *
 * <p>
 * Internal: hosts reach it through {@code Bot.body()}.
 */
final class BotControlPort implements Body {

	private final BotBody bot;

	private final BotPlayer handle;

	private final ControlArbiter arbiter;

	/** Maps every control 1:1 onto the bot's own setter. */
	private final BodyControls sink;

	BotControlPort(BotBody bot, BotPlayer handle) {
		this.bot = bot;
		this.handle = handle;
		this.arbiter = new ControlArbiter(bot.name(), BotBody::requireMainThread, () -> bot.player().getWorld(),
				() -> bot.state() != BotState.FAILED && !bot.isRemoved());
		this.sink = new Sink();
	}

	@Override
	public Location getLocation() {
		BotBody.requireMainThread();
		return bot.player().getLocation();
	}

	@Override
	public Vector getVelocity() {
		BotBody.requireMainThread();
		var velocity = handle.clientVelocity();
		return new Vector(velocity.x, velocity.y, velocity.z);
	}

	@Override
	public boolean isOnGround() {
		BotBody.requireMainThread();
		return handle.clientOnGround();
	}

	@Override
	public float getLookYaw() {
		BotBody.requireMainThread();
		return bot.getLookYaw();
	}

	@Override
	public float getLookPitch() {
		BotBody.requireMainThread();
		return bot.getLookPitch();
	}

	@Override
	public BodyControls controls(ControlPriority priority) {
		return arbiter.writer(priority);
	}

	/** Replays this tick's winning writes into the bot's controls. */
	void apply() {
		arbiter.applyTo(sink);
	}

	/** Drops the pending writes. */
	void clear() {
		arbiter.clear();
	}

	private boolean inWorld(World world) {
		// A target checked against the bot's world when written: the bot may have
		// changed world since (write outside its tick event); drop rather than
		// fail the bot.
		return world.equals(bot.player().getWorld());
	}

	private final class Sink implements BodyControls {

		@Override
		public void setForward(boolean forward) {
			bot.setForward(forward);
		}

		@Override
		public void setBackward(boolean backward) {
			bot.setBackward(backward);
		}

		@Override
		public void setLeft(boolean left) {
			bot.setLeft(left);
		}

		@Override
		public void setRight(boolean right) {
			bot.setRight(right);
		}

		@Override
		public void setJump(boolean jump) {
			bot.setJump(jump);
		}

		@Override
		public void setSneak(boolean sneak) {
			bot.setSneak(sneak);
		}

		@Override
		public void setSprint(boolean sprint) {
			bot.setSprint(sprint);
		}

		@Override
		public void setHeading(Float yaw) {
			bot.setHeading(yaw);
		}

		@Override
		public void setLook(float yaw, float pitch) {
			bot.setLook(yaw, pitch);
		}

		@Override
		public void setSlot(int slot) {
			bot.setSlot(slot);
		}

		@Override
		public void useItem(EquipmentSlot hand) {
			bot.useItem(hand);
		}

		@Override
		public void useItemOn(Block block, BlockFace face, EquipmentSlot hand) {
			if (inWorld(block.getWorld())) {
				bot.useItemOn(block, face, hand);
			}
		}

		@Override
		public void leftClick() {
			bot.leftClick();
		}

		@Override
		public void startBreaking(Block block, BlockFace face) {
			if (inWorld(block.getWorld())) {
				bot.startBreaking(block, face);
			}
		}

		@Override
		public void attack(Entity target) {
			if (inWorld(target.getWorld())) {
				bot.attack(target);
			}
		}

		@Override
		public void interact(Entity target, EquipmentSlot hand) {
			if (inWorld(target.getWorld())) {
				bot.interact(target, hand);
			}
		}

		@Override
		public void swapHands() {
			bot.swapHands();
		}
	}
}
