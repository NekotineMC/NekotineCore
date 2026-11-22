package fr.nekotine.core.navigation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Buffers a {@link Body}'s prioritized control writes between two of its ticks
 * and replays the winners: a reusable piece for body adapters (bots, later
 * mobs).
 *
 * <p>
 * A {@link #writer(ControlPriority) writer} checks the thread and its arguments
 * when called (throwing as {@link BodyControls} says), drops the write if the
 * body is not live, then records it in its group. Per group, the highest
 * priority that wrote since the last {@link #applyTo} wins: a higher write
 * discards the lower writes recorded in that group, a lower write is dropped,
 * an equal write is appended. Never throws for a priority reason.
 *
 * <p>
 * Not thread-safe: the adapter's thread check keeps it on one thread.
 */
public final class ControlArbiter {

	/** The control groups, in replay order. */
	private enum Group {
		MOVEMENT, AIM, HANDS
	}

	/** The writes recorded in one group since the last replay. */
	private static final class Pending {
		ControlPriority winner;
		final List<Consumer<BodyControls>> writes = new ArrayList<>();

		void record(ControlPriority priority, Consumer<BodyControls> write) {
			if (winner != null && priority.compareTo(winner) < 0) {
				return;
			}
			if (winner == null || priority.compareTo(winner) > 0) {
				winner = priority;
				writes.clear();
			}
			writes.add(write);
		}

		void clear() {
			winner = null;
			writes.clear();
		}
	}

	private final String name;

	private final Runnable threadCheck;

	private final Supplier<World> world;

	private final BooleanSupplier live;

	private final Map<Group, Pending> pending = new EnumMap<>(Group.class);

	private final Map<ControlPriority, BodyControls> writers = new EnumMap<>(ControlPriority.class);

	/**
	 * @param name
	 *            the body's name, prefixed to argument errors
	 * @param threadCheck
	 *            run first by every write; throws {@link IllegalStateException} off
	 *            the allowed thread
	 * @param world
	 *            the body's current world, which block and entity targets must be
	 *            in
	 * @param live
	 *            whether the body still applies writes; a write made while it is
	 *            false is dropped once checked
	 */
	public ControlArbiter(String name, Runnable threadCheck, Supplier<World> world, BooleanSupplier live) {
		this.name = Objects.requireNonNull(name, "name");
		this.threadCheck = Objects.requireNonNull(threadCheck, "threadCheck");
		this.world = Objects.requireNonNull(world, "world");
		this.live = Objects.requireNonNull(live, "live");
		for (var group : Group.values()) {
			pending.put(group, new Pending());
		}
		for (var priority : ControlPriority.values()) {
			writers.put(priority, new Writer(priority));
		}
	}

	/** @return the writer at {@code priority}, the same for every call */
	public BodyControls writer(ControlPriority priority) {
		return writers.get(Objects.requireNonNull(priority, "priority"));
	}

	/**
	 * Replays the winning writes of each group into {@code sink}, in call order,
	 * movement first, then aim, then hands, and forgets every recorded write.
	 * Writes made while replaying wait for the next replay.
	 */
	public void applyTo(BodyControls sink) {
		var replay = new ArrayList<Consumer<BodyControls>>();
		for (var group : Group.values()) {
			var p = pending.get(group);
			replay.addAll(p.writes);
			p.clear();
		}
		for (var write : replay) {
			write.accept(sink);
		}
	}

	/** Forgets every recorded write. */
	public void clear() {
		for (var p : pending.values()) {
			p.clear();
		}
	}

	private final class Writer implements BodyControls {

		private final ControlPriority priority;

		Writer(ControlPriority priority) {
			this.priority = priority;
		}

		private void record(Group group, Consumer<BodyControls> write) {
			if (!live.getAsBoolean()) {
				return;
			}
			pending.get(group).record(priority, write);
		}

		@Override
		public void setForward(boolean forward) {
			threadCheck.run();
			record(Group.MOVEMENT, s -> s.setForward(forward));
		}

		@Override
		public void setBackward(boolean backward) {
			threadCheck.run();
			record(Group.MOVEMENT, s -> s.setBackward(backward));
		}

		@Override
		public void setLeft(boolean left) {
			threadCheck.run();
			record(Group.MOVEMENT, s -> s.setLeft(left));
		}

		@Override
		public void setRight(boolean right) {
			threadCheck.run();
			record(Group.MOVEMENT, s -> s.setRight(right));
		}

		@Override
		public void setJump(boolean jump) {
			threadCheck.run();
			record(Group.MOVEMENT, s -> s.setJump(jump));
		}

		@Override
		public void setSneak(boolean sneak) {
			threadCheck.run();
			record(Group.MOVEMENT, s -> s.setSneak(sneak));
		}

		@Override
		public void setSprint(boolean sprint) {
			threadCheck.run();
			record(Group.MOVEMENT, s -> s.setSprint(sprint));
		}

		@Override
		public void setHeading(Float yaw) {
			threadCheck.run();
			if (yaw != null && !Float.isFinite(yaw)) {
				throw new IllegalArgumentException(name + " heading must be finite: " + yaw);
			}
			record(Group.MOVEMENT, s -> s.setHeading(yaw));
		}

		@Override
		public void setLook(float yaw, float pitch) {
			threadCheck.run();
			if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
				throw new IllegalArgumentException(name + " look must be finite: " + yaw + ", " + pitch);
			}
			record(Group.AIM, s -> s.setLook(yaw, pitch));
		}

		@Override
		public void setSlot(int slot) {
			threadCheck.run();
			if (slot < 0 || slot > 8) {
				throw new IllegalArgumentException(name + " slot must be in 0-8: " + slot);
			}
			record(Group.HANDS, s -> s.setSlot(slot));
		}

		@Override
		public void useItem(EquipmentSlot hand) {
			threadCheck.run();
			requireHand(hand);
			record(Group.HANDS, s -> s.useItem(hand));
		}

		@Override
		public void useItemOn(Block block, BlockFace face, EquipmentSlot hand) {
			threadCheck.run();
			requireBlock(block);
			requireFace(face);
			requireHand(hand);
			record(Group.HANDS, s -> s.useItemOn(block, face, hand));
		}

		@Override
		public void leftClick() {
			threadCheck.run();
			record(Group.HANDS, BodyControls::leftClick);
		}

		@Override
		public void startBreaking(Block block, BlockFace face) {
			threadCheck.run();
			requireBlock(block);
			requireFace(face);
			record(Group.HANDS, s -> s.startBreaking(block, face));
		}

		@Override
		public void attack(Entity target) {
			threadCheck.run();
			requireEntity(target);
			record(Group.HANDS, s -> s.attack(target));
		}

		@Override
		public void interact(Entity target, EquipmentSlot hand) {
			threadCheck.run();
			requireEntity(target);
			requireHand(hand);
			record(Group.HANDS, s -> s.interact(target, hand));
		}

		@Override
		public void swapHands() {
			threadCheck.run();
			record(Group.HANDS, BodyControls::swapHands);
		}
	}

	private void requireHand(EquipmentSlot hand) {
		if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
			throw new IllegalArgumentException(name + " hand must be HAND or OFF_HAND: " + hand);
		}
	}

	private void requireFace(BlockFace face) {
		if (face == null || !face.isCartesian()) {
			throw new IllegalArgumentException(name + " face must be one of the six cube faces: " + face);
		}
	}

	private void requireBlock(Block block) {
		Objects.requireNonNull(block, "block");
		requireSameWorld(block.getWorld(), "block");
	}

	private void requireEntity(Entity target) {
		Objects.requireNonNull(target, "target");
		requireSameWorld(target.getWorld(), "target");
	}

	private void requireSameWorld(World target, String what) {
		var own = world.get();
		if (!Objects.equals(target, own)) {
			throw new IllegalArgumentException(
					name + " " + what + " is in world " + (target == null ? null : target.getName())
							+ ", not in the body's " + (own == null ? null : own.getName()));
		}
	}
}
