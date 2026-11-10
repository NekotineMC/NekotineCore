package fr.nekotine.core.bot.internal;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * A bot's {@link ServerPlayer}, which can also play the client's part of
 * movement: {@link #simulate} runs the server's own {@code travel} physics on
 * this player from the bot's client state, then rewinds every server field it
 * touched. Still a {@code ServerPlayer} to Paper, Bukkit and ProtocolLib. Main
 * thread only.
 */
final class BotPlayer extends ServerPlayer {

	/** Client ticks between two ground jumps, as {@code LivingEntity#aiStep}. */
	private static final int JUMP_DELAY = 10;

	/** Client input scale, as the client applies before any slowdown. */
	private static final float INPUT_SCALE = 0.98F;

	/** While true, movement emits nothing (no step sounds, no game events). */
	private boolean simulating;

	/** Movements {@code move} recorded during the simulation, to drop after it. */
	private int simulatedMoves;

	// The bot's client-side state: what its client would hold between ticks.
	private Vec3 clientVelocity = Vec3.ZERO;

	private boolean clientOnGround;

	private boolean clientHorizontalCollision;

	private boolean clientVerticalCollision;

	private boolean clientVerticalCollisionBelow;

	private boolean clientMinorHorizontalCollision;

	private int clientNoJumpDelay;

	BotPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation information) {
		super(server, level, profile, information);
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return simulating ? Entity.MovementEmission.NONE : super.getMovementEmission();
	}

	@Override
	public void gameEvent(Holder<GameEvent> event, Entity sourceEntity) {
		if (!simulating) {
			super.gameEvent(event, sourceEntity);
		}
	}

	@Override
	public void move(MoverType moverType, Vec3 delta) {
		if (!simulating || noPhysics) {
			// Without physics, move records no movement.
			super.move(moverType, delta);
			return;
		}
		var before = position();
		super.move(moverType, delta);
		if (!position().equals(before)) {
			simulatedMoves++;
		}
	}

	/** The client got a teleport: its velocity is zero again. */
	void resetClientVelocity() {
		clientVelocity = Vec3.ZERO;
	}

	/** @return the client's on-ground flag after the last {@link #simulate}. */
	boolean clientOnGround() {
		return clientOnGround;
	}

	/**
	 * @return the client's horizontal collision flag after the last
	 *         {@link #simulate}
	 */
	boolean clientHorizontalCollision() {
		return clientHorizontalCollision;
	}

	/**
	 * One client movement tick from the current server position: loads the client
	 * state, faces {@code heading}, maps {@code keys} as the client does, applies a
	 * jump as {@code LivingEntity#aiStep} would, runs the server's {@code travel},
	 * reads the result back into the client state, then restores every server field
	 * it touched. Nothing is awarded and movement emits nothing meanwhile.
	 *
	 * @return the position the client ends the tick at
	 */
	Vec3 simulate(Input keys, float heading, float pitch) {
		double x = getX();
		double y = getY();
		double z = getZ();
		double oldX = xo;
		double oldY = yo;
		double oldZ = zo;
		float yRot = getYRot();
		float xRot = getXRot();
		Vec3 deltaMovement = getDeltaMovement();
		boolean onGround = onGround();
		Optional<BlockPos> supportingBlock = mainSupportingBlockPos;
		double savedFallDistance = fallDistance;
		boolean savedHorizontalCollision = horizontalCollision;
		boolean savedVerticalCollision = verticalCollision;
		boolean savedVerticalCollisionBelow = verticalCollisionBelow;
		boolean savedMinorHorizontalCollision = minorHorizontalCollision;
		Vec3 savedStuckSpeedMultiplier = stuckSpeedMultiplier;
		float savedXxa = xxa;
		float savedYya = yya;
		float savedZza = zza;
		boolean savedJumping = jumping;
		float savedMoveDist = moveDist;
		float savedFlyDist = flyDist;
		simulating = true;
		simulatedMoves = 0;
		try {
			setOnGround(clientOnGround);
			horizontalCollision = clientHorizontalCollision;
			verticalCollision = clientVerticalCollision;
			verticalCollisionBelow = clientVerticalCollisionBelow;
			minorHorizontalCollision = clientMinorHorizontalCollision;
			setDeltaMovement(clientVelocity);
			setYRot(heading);
			setXRot(pitch);

			var move = new Vec2((keys.left() ? 1 : 0) - (keys.right() ? 1 : 0),
					(keys.forward() ? 1 : 0) - (keys.backward() ? 1 : 0)).normalized();
			if (move.lengthSquared() != 0.0F) {
				move = move.scale(INPUT_SCALE);
				boolean crouching = keys.shift() && !getAbilities().flying && !isSwimming();
				if (crouching || isVisuallyCrawling()) {
					move = move.scale((float) getAttributeValue(Attributes.SNEAKING_SPEED));
				}
			}
			xxa = move.x;
			yya = 0.0F;
			zza = move.y;
			jumping = keys.jump();
			jump();

			travel(new Vec3(xxa, yya, zza));

			clientVelocity = getDeltaMovement();
			clientOnGround = onGround();
			clientHorizontalCollision = horizontalCollision;
			clientVerticalCollision = verticalCollision;
			clientVerticalCollisionBelow = verticalCollisionBelow;
			clientMinorHorizontalCollision = minorHorizontalCollision;
			return position();
		} finally {
			for (int i = 0; i < simulatedMoves; i++) {
				removeLatestMovementRecording();
			}
			absSnapTo(x, y, z);
			xo = oldX;
			yo = oldY;
			zo = oldZ;
			setYRot(yRot);
			setXRot(xRot);
			setOnGround(onGround);
			mainSupportingBlockPos = supportingBlock;
			setDeltaMovement(deltaMovement);
			fallDistance = savedFallDistance;
			horizontalCollision = savedHorizontalCollision;
			verticalCollision = savedVerticalCollision;
			verticalCollisionBelow = savedVerticalCollisionBelow;
			minorHorizontalCollision = savedMinorHorizontalCollision;
			stuckSpeedMultiplier = savedStuckSpeedMultiplier;
			xxa = savedXxa;
			yya = savedYya;
			zza = savedZza;
			jumping = savedJumping;
			moveDist = savedMoveDist;
			flyDist = savedFlyDist;
			simulating = false;
		}
	}

	/**
	 * The jump branch of {@code LivingEntity#aiStep}, on the client's jump delay.
	 * The ground jump is {@code jumpFromGround}'s velocity change only: the
	 * server's move handler awards the jump when it sees it.
	 */
	private void jump() {
		if (clientNoJumpDelay > 0) {
			clientNoJumpDelay--;
		}
		if (!jumping || !isAffectedByFluids()) {
			clientNoJumpDelay = 0;
			return;
		}
		double fluidHeight = isInLava() ? getFluidHeight(FluidTags.LAVA) : getFluidHeight(FluidTags.WATER);
		boolean inWater = isInWater() && fluidHeight > 0.0;
		double threshold = getFluidJumpThreshold();
		if (inWater && !(onGround() && fluidHeight <= threshold)) {
			jumpInLiquid(FluidTags.WATER);
		} else if (isInLava() && !(onGround() && isInShallowFluid(FluidTags.LAVA))) {
			jumpInLiquid(FluidTags.LAVA);
		} else if ((onGround() || inWater && fluidHeight <= threshold) && clientNoJumpDelay == 0) {
			float power = getJumpPower();
			if (power > 1.0E-5F) {
				var velocity = getDeltaMovement();
				setDeltaMovement(velocity.x, Math.max(power, velocity.y), velocity.z);
				if (isSprinting()) {
					float angle = getYRot() * Mth.DEG_TO_RAD;
					setDeltaMovement(getDeltaMovement().add(-Mth.sin(angle) * 0.2, 0.0, Mth.cos(angle) * 0.2));
				}
			}
			clientNoJumpDelay = JUMP_DELAY;
		}
	}
}
