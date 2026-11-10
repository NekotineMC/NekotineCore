package fr.nekotine.botstesthost.it;

import static fr.nekotine.botstesthost.it.BotLifecycleIT.cleanUp;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.register;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.roster;
import static fr.nekotine.botstesthost.it.BotMovementIT.FLOOR_Y;
import static fr.nekotine.botstesthost.it.BotMovementIT.build;
import static fr.nekotine.botstesthost.it.BotMovementIT.clear;
import static fr.nekotine.botstesthost.it.BotMovementIT.structure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import fr.nekotine.botstesthost.BotsTestHost;
import fr.nekotine.botstesthost.it.BotLifecycleIT.LogCapture;
import fr.nekotine.botstesthost.it.BotLifecycleIT.PlayerEvents;
import fr.nekotine.botstesthost.it.BotMovementIT.Structure;
import fr.nekotine.botstesthost.it.BotMovementIT.Ticks;
import fr.nekotine.core.bot.Bot;
import fr.nekotine.core.bot.BotRoster;
import fr.nekotine.core.bot.internal.BotBody;
import fr.xxgoldenbluexx.paperjunitserver.PaperServer;
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Story 1.7: bots pick a hotbar slot, use their hands, send commands and
 * respawn through real packets, on a stone platform built high above spawn.
 */
class BotHandsIT {

	private static final String NAME = "Bot_Hands";

	/** The entity interact events one interact packet fires, in order. */
	private static final List<String> INTERACT_EVENTS = List.of("PlayerInteractAtEntityEvent");

	/** One received packet of the watched bot: its NMS class and server tick. */
	private record Seen(String packet, int tick) {
	}

	/** Records the hand, slot, command and move packets the bot sends. */
	private static final class Packets extends PacketAdapter {
		final Queue<Seen> seen = new ConcurrentLinkedQueue<>();

		Packets() {
			super(PaperServer.plugin(), PacketType.Play.Client.HELD_ITEM_SLOT, PacketType.Play.Client.USE_ITEM,
					PacketType.Play.Client.USE_ITEM_ON, PacketType.Play.Client.BLOCK_PLACE,
					PacketType.Play.Client.ARM_ANIMATION, PacketType.Play.Client.BLOCK_DIG,
					PacketType.Play.Client.ATTACK, PacketType.Play.Client.USE_ENTITY,
					PacketType.Play.Client.CHAT_COMMAND, PacketType.Play.Client.CLIENT_COMMAND,
					PacketType.Play.Client.PLAYER_LOADED, PacketType.Play.Client.STEER_VEHICLE,
					PacketType.Play.Client.POSITION_LOOK);
		}

		@Override
		public void onPacketReceiving(PacketEvent event) {
			if (event.getPlayer().getName().equals(NAME)) {
				seen.add(new Seen(event.getPacket().getHandle().getClass().getSimpleName(), Bukkit.getCurrentTick()));
			}
		}

		/** @return the packets received, by simple class name, without moves. */
		List<String> sent() {
			return seen.stream().map(Seen::packet).filter(p -> !p.equals("PosRot")).toList();
		}
	}

	/** The Bukkit events the bot's packets fire, for the bot only. */
	public static final class HandEvents implements Listener {
		final Queue<PlayerInteractEvent> interacts = new ConcurrentLinkedQueue<>();
		final Queue<PlayerItemHeldEvent> held = new ConcurrentLinkedQueue<>();
		final Queue<EntityDamageByEntityEvent> damages = new ConcurrentLinkedQueue<>();
		final Queue<PlayerInteractEntityEvent> entityInteracts = new ConcurrentLinkedQueue<>();
		final Queue<PlayerSwapHandItemsEvent> swaps = new ConcurrentLinkedQueue<>();
		final Queue<String> commands = new ConcurrentLinkedQueue<>();
		final Queue<PlayerRespawnEvent> respawns = new ConcurrentLinkedQueue<>();
		final Queue<Boolean> loadedTimeouts = new ConcurrentLinkedQueue<>();
		volatile boolean cancelHeld;

		private static boolean bot(org.bukkit.entity.Player player) {
			return player.getName().equals(NAME);
		}

		@EventHandler
		public void onInteract(PlayerInteractEvent event) {
			if (bot(event.getPlayer())) {
				interacts.add(event);
			}
		}

		@EventHandler
		public void onHeld(PlayerItemHeldEvent event) {
			if (bot(event.getPlayer())) {
				held.add(event);
				event.setCancelled(cancelHeld);
			}
		}

		@EventHandler
		public void onDamage(EntityDamageByEntityEvent event) {
			if (event.getDamager() instanceof org.bukkit.entity.Player player && bot(player)) {
				damages.add(event);
			}
		}

		@EventHandler
		public void onInteractEntity(PlayerInteractEntityEvent event) {
			if (bot(event.getPlayer())) {
				entityInteracts.add(event);
			}
		}

		@EventHandler
		public void onSwap(PlayerSwapHandItemsEvent event) {
			if (bot(event.getPlayer())) {
				swaps.add(event);
			}
		}

		@EventHandler
		public void onCommand(PlayerCommandPreprocessEvent event) {
			if (bot(event.getPlayer())) {
				commands.add(event.getMessage());
			}
		}

		@EventHandler
		public void onRespawn(PlayerRespawnEvent event) {
			if (bot(event.getPlayer())) {
				respawns.add(event);
			}
		}

		@EventHandler
		public void onLoaded(PlayerClientLoadedWorldEvent event) {
			if (bot(event.getPlayer())) {
				loadedTimeouts.add(event.isTimeout());
			}
		}
	}

	/** A spawned bot on a fresh platform, with listeners attached. */
	private record Scene(BotRoster roster, Structure structure, Bot bot, HandEvents events, Packets packets,
			PlayerEvents players) {
	}

	/**
	 * Builds the platform, spawns the bot at its centre facing +Z, lets it settle,
	 * then registers the listeners.
	 */
	private static Scene scene() throws Exception {
		var roster = roster();
		var structure = structure();
		build(structure);
		var bot = PaperServer.onMain(() -> roster.spawn(NAME, structure.at(FLOOR_Y + 1)));
		PaperServer.awaitTicks(10);
		var scene = new Scene(roster, structure, bot, new HandEvents(), new Packets(), new PlayerEvents());
		register(scene.events());
		register(scene.packets());
		register(scene.players());
		return scene;
	}

	private static void tearDown(Scene scene, Object... extra) throws Exception {
		if (scene == null) {
			return;
		}
		var listeners = new Object[extra.length + 3];
		listeners[0] = scene.events();
		listeners[1] = scene.packets();
		listeners[2] = scene.players();
		System.arraycopy(extra, 0, listeners, 3, extra.length);
		cleanUp(scene.roster(), listeners);
		clear(scene.structure());
	}

	private static void control(Runnable change) throws Exception {
		PaperServer.onMain(() -> {
			change.run();
			return null;
		});
	}

	/** No kick, no server complaint about the bot's packets. */
	private static void assertClean(Scene scene, LogCapture logs) {
		assertTrue(scene.players().kicked.isEmpty(), "the bot was kicked");
		var complaints = logs.lines.stream()
				.filter(l -> l.contains("moved wrongly") || l.contains("moved too quickly")
						|| l.contains("invalid carried item") || l.contains("kicked") || l.contains("Hacking"))
				.toList();
		assertEquals(List.of(), complaints, "the server complained about the bot");
	}

	/**
	 * A NoAI pig standing on the platform, {@code dz} blocks in front of the bot.
	 */
	private static Pig pig(Structure structure, double dz) throws Exception {
		return PaperServer.onMain(() -> {
			var at = structure.at(FLOOR_Y + 1).add(0, 0, dz);
			var pig = (Pig) at.getWorld().spawnEntity(at, EntityType.PIG);
			pig.setAI(false);
			return pig;
		});
	}

	private static void removeEntity(Entity entity) throws Exception {
		if (entity != null) {
			control(entity::remove);
		}
	}

	@Test
	void slotSendsOnePacketPerChange() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			assertEquals(0, PaperServer.onMain(bot::getSlot), "the initial slot is not the server's");
			control(() -> bot.setSlot(3));
			PaperServer.awaitTicks(5);
			PaperServer.onMain(() -> {
				assertEquals(3, bot.getSlot());
				assertEquals(3, bot.player().getInventory().getHeldItemSlot(), "the server did not select slot 3");
				return null;
			});
			assertEquals(List.of("ServerboundSetCarriedItemPacket"), scene.packets().sent(),
					"not one carried-item packet");
			assertEquals(1, scene.events().held.size(), "not one PlayerItemHeldEvent");
			var held = scene.events().held.peek();
			assertEquals(0, held.getPreviousSlot());
			assertEquals(3, held.getNewSlot());
			assertClean(scene, logs);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void serverSlotIsAdopted() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			control(() -> bot.player().getInventory().setHeldItemSlot(5));
			PaperServer.awaitTicks(3);
			assertEquals(5, PaperServer.onMain(bot::getSlot), "the bot did not adopt the server's slot");
			assertEquals(List.of(), scene.packets().sent(), "the bot answered the server's slot");
			// A cancelled change: the server sends its slot back, the bot adopts it.
			scene.events().cancelHeld = true;
			control(() -> bot.setSlot(2));
			PaperServer.awaitTicks(5);
			PaperServer.onMain(() -> {
				assertEquals(5, bot.getSlot(), "the bot did not adopt the slot of the cancelled change");
				assertEquals(5, bot.player().getInventory().getHeldItemSlot());
				return null;
			});
			assertEquals(List.of("ServerboundSetCarriedItemPacket"), scene.packets().sent(),
					"not one carried-item packet");
			assertEquals(1, scene.events().held.size(), "not one PlayerItemHeldEvent");
			assertClean(scene, logs);
		} finally {
			tearDown(scene);
		}
	}

	/**
	 * Acceptance: a bot right-clicks with a diamond, seen by the host, then leaves.
	 */
	@Test
	void useItemRightClicksAirWithTheItem() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			control(() -> {
				bot.player().getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND));
				bot.setLook(0, -90);
				bot.useItem(EquipmentSlot.HAND);
			});
			PaperServer.awaitTicks(5);
			var interacts = List.copyOf(scene.events().interacts);
			assertEquals(1, interacts.size(), "not one PlayerInteractEvent: " + interacts);
			var interact = interacts.getFirst();
			assertEquals(Action.RIGHT_CLICK_AIR, interact.getAction());
			assertEquals(EquipmentSlot.HAND, interact.getHand());
			assertNotNull(interact.getItem());
			assertEquals(Material.DIAMOND, interact.getItem().getType(), "the event did not carry the diamond");
			assertEquals(List.of("ServerboundUseItemPacket"), scene.packets().sent(), "not one use-item packet");
			assertClean(scene, logs);
			var roster = scene.roster();
			var player = bot.player();
			control(() -> roster.remove(bot));
			assertFalse(PaperServer.onMain(player::isOnline), "the bot is still online after removal");
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void useItemOnRightClicksTheBlock() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			var structure = scene.structure();
			Block target = PaperServer
					.onMain(() -> structure.world.getBlockAt(structure.cx, FLOOR_Y, structure.cz + 2));
			control(() -> bot.useItemOn(target, BlockFace.UP, EquipmentSlot.HAND));
			PaperServer.awaitTicks(5);
			var clicks = scene.events().interacts.stream().filter(e -> e.getAction() == Action.RIGHT_CLICK_BLOCK)
					.toList();
			assertEquals(1, clicks.size(), "not one RIGHT_CLICK_BLOCK: " + scene.events().interacts);
			assertEquals(target, clicks.getFirst().getClickedBlock());
			assertEquals(BlockFace.UP, clicks.getFirst().getBlockFace());
			assertEquals(List.of("ServerboundUseItemOnPacket"), scene.packets().sent(), "not one use-item-on packet");
			assertClean(scene, logs);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void leftClickClicksAir() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			control(() -> {
				bot.setLook(0, -90);
				bot.leftClick();
			});
			PaperServer.awaitTicks(5);
			var interacts = List.copyOf(scene.events().interacts);
			assertEquals(1, interacts.size(), "not one PlayerInteractEvent: " + interacts);
			assertEquals(Action.LEFT_CLICK_AIR, interacts.getFirst().getAction());
			assertEquals(List.of("ServerboundSwingPacket"), scene.packets().sent(), "not one swing packet");
			assertClean(scene, logs);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void startBreakingLeftClicksTheBlock() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			var structure = scene.structure();
			Block target = PaperServer
					.onMain(() -> structure.world.getBlockAt(structure.cx, FLOOR_Y, structure.cz + 1));
			control(() -> {
				// Looking at the block, as a client breaking it would.
				bot.setLook(0, 58);
				bot.startBreaking(target, BlockFace.UP);
			});
			PaperServer.awaitTicks(5);
			var clicks = scene.events().interacts.stream().filter(e -> e.getAction() == Action.LEFT_CLICK_BLOCK)
					.toList();
			assertEquals(1, clicks.size(), "not one LEFT_CLICK_BLOCK: " + scene.events().interacts);
			assertEquals(target, clicks.getFirst().getClickedBlock(), "LEFT_CLICK_BLOCK on another block");
			assertTrue(scene.events().interacts.stream().noneMatch(e -> e.getAction() == Action.LEFT_CLICK_AIR),
					"the swing clicked air");
			assertEquals(List.of("ServerboundPlayerActionPacket", "ServerboundSwingPacket"), scene.packets().sent(),
					"not one action packet then one swing");
			assertClean(scene, logs);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void attackDamagesThePig() throws Exception {
		Scene scene = null;
		Pig pig = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			var target = pig(scene.structure(), 2);
			pig = target;
			PaperServer.awaitTicks(2);
			control(() -> {
				bot.setLook(0, 20);
				bot.attack(target);
			});
			PaperServer.awaitTicks(5);
			var damages = List.copyOf(scene.events().damages);
			assertEquals(1, damages.size(), "not one EntityDamageByEntityEvent: " + damages);
			assertSame(bot.player(), damages.getFirst().getDamager(), "the damager is not the bot");
			assertSame(target, damages.getFirst().getEntity(), "the damaged entity is not the pig");
			assertEquals(List.of("ServerboundAttackPacket", "ServerboundSwingPacket"), scene.packets().sent(),
					"not one attack packet then one swing");
			assertClean(scene, logs);
		} finally {
			removeEntity(pig);
			tearDown(scene);
		}
	}

	@Test
	void interactRightClicksThePig() throws Exception {
		Scene scene = null;
		Pig pig = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			var target = pig(scene.structure(), 2);
			pig = target;
			PaperServer.awaitTicks(2);
			control(() -> bot.interact(target, EquipmentSlot.HAND));
			PaperServer.awaitTicks(5);
			var interacts = List.copyOf(scene.events().entityInteracts);
			assertEquals(INTERACT_EVENTS, interacts.stream().map(e -> e.getClass().getSimpleName()).toList(),
					"unexpected entity interact events");
			assertTrue(
					interacts.stream()
							.allMatch(e -> e.getRightClicked() == target && e.getHand() == EquipmentSlot.HAND),
					"PlayerInteractEntityEvent on another entity or hand: " + interacts);
			assertEquals(List.of("ServerboundInteractPacket"), scene.packets().sent(), "not one interact packet");
			assertClean(scene, logs);
		} finally {
			removeEntity(pig);
			tearDown(scene);
		}
	}

	@Test
	void swapHandsSwapsTheItems() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			control(() -> {
				var inventory = bot.player().getInventory();
				inventory.setItemInMainHand(new ItemStack(Material.DIAMOND));
				inventory.setItemInOffHand(new ItemStack(Material.STICK));
				bot.swapHands();
			});
			PaperServer.awaitTicks(5);
			assertEquals(1, scene.events().swaps.size(), "not one PlayerSwapHandItemsEvent");
			PaperServer.onMain(() -> {
				var inventory = bot.player().getInventory();
				assertEquals(Material.STICK, inventory.getItemInMainHand().getType(), "main hand not swapped");
				assertEquals(Material.DIAMOND, inventory.getItemInOffHand().getType(), "off hand not swapped");
				return null;
			});
			assertEquals(List.of("ServerboundPlayerActionPacket"), scene.packets().sent(), "not one action packet");
			assertClean(scene, logs);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void sendCommandRunsTheHostCommand() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			BotsTestHost.INVOCATIONS.clear();
			control(() -> bot.sendCommand("/bottest hi"));
			PaperServer.awaitTicks(5);
			assertEquals(List.of("/bottest hi"), List.copyOf(scene.events().commands),
					"not one PlayerCommandPreprocessEvent");
			var invocations = List.copyOf(BotsTestHost.INVOCATIONS);
			assertEquals(1, invocations.size(), "the host command did not run once: " + invocations);
			assertSame(bot.player(), invocations.getFirst().sender(), "the command's sender is not the bot");
			assertEquals("hi", invocations.getFirst().word());
			assertEquals(List.of("ServerboundChatCommandPacket"), scene.packets().sent(), "not one command packet");
			assertClean(scene, logs);
		} finally {
			BotsTestHost.INVOCATIONS.clear();
			tearDown(scene);
		}
	}

	@Test
	void respawnRevivesTheBot() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			// Alive: the server ignores the request.
			control(bot::respawn);
			PaperServer.awaitTicks(3);
			assertTrue(scene.events().respawns.isEmpty(), "a living bot respawned");
			control(() -> bot.player().setHealth(0));
			PaperServer.awaitTicks(3);
			assertTrue(PaperServer.onMain(() -> bot.player().isDead()), "the bot did not die");
			scene.events().loadedTimeouts.clear();
			control(bot::respawn);
			PaperServer.awaitTicks(5);
			assertEquals(1, scene.events().respawns.size(), "not one PlayerRespawnEvent");
			PaperServer.onMain(() -> {
				assertFalse(bot.player().isDead(), "the bot is still dead");
				assertTrue(bot.player().getHealth() > 0, "the bot has no health");
				assertTrue(bot.player().isOnline(), "the bot left");
				return null;
			});
			assertEquals(List.of(false), List.copyOf(scene.events().loadedTimeouts),
					"PlayerClientLoadedWorldEvent not once, without timeout");
			assertEquals(
					List.of("ServerboundClientCommandPacket", "ServerboundClientCommandPacket",
							"ServerboundPlayerLoadedPacket"),
					scene.packets().sent(), "not two respawn requests then one loaded ack");
			assertClean(scene, logs);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void oneShotSetInTickEventIsSentThatTickAfterMoveAndSlot() throws Exception {
		Scene scene = null;
		Ticks ticks = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			var setAt = new AtomicInteger(-1);
			ticks = new Ticks(bot);
			ticks.hook = e -> {
				if (setAt.get() < 0) {
					setAt.set(Bukkit.getCurrentTick());
					e.getBot().setLook(0, -90);
					e.getBot().setSlot(4);
					e.getBot().leftClick();
					e.getBot().swapHands();
				}
			};
			register(ticks);
			PaperServer.awaitTicks(5);
			assertTrue(setAt.get() >= 0, "the tick listener never ran");
			int tick = setAt.get();
			var thatTick = scene.packets().seen.stream().filter(s -> s.tick() == tick).map(Seen::packet).toList();
			assertEquals(
					List.of("PosRot", "ServerboundSetCarriedItemPacket", "ServerboundSwingPacket",
							"ServerboundPlayerActionPacket"),
					thatTick, "not move, slot, then the one-shots in call order, that tick");
			assertEquals(List.of("ServerboundSetCarriedItemPacket", "ServerboundSwingPacket",
					"ServerboundPlayerActionPacket"), scene.packets().sent(), "a packet was sent again");
			assertClean(scene, logs);
		} finally {
			tearDown(scene, ticks);
		}
	}

	@Test
	void invalidControlsQueueNothing() throws Exception {
		Scene scene = null;
		Pig pig = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			var structure = scene.structure();
			World other = PaperServer.onMain(() -> Bukkit.getWorlds().stream().filter(w -> !w.equals(structure.world))
					.findFirst().orElseThrow());
			pig = PaperServer.onMain(() -> {
				var spawned = (Pig) other.spawnEntity(other.getSpawnLocation(), EntityType.PIG);
				spawned.setAI(false);
				return spawned;
			});
			var otherPig = pig;
			assertThrows(IllegalStateException.class, () -> bot.setSlot(1));
			assertThrows(IllegalStateException.class, () -> bot.useItem(EquipmentSlot.HAND));
			assertThrows(IllegalStateException.class, bot::leftClick);
			assertThrows(IllegalStateException.class, () -> bot.sendCommand("bottest hi"));
			assertThrows(IllegalStateException.class, bot::respawn);
			PaperServer.onMain(() -> {
				var ownBlock = structure.world.getBlockAt(structure.cx, FLOOR_Y, structure.cz + 1);
				var otherBlock = other.getBlockAt(0, 64, 0);
				assertThrows(IllegalArgumentException.class, () -> bot.setSlot(9));
				assertThrows(IllegalArgumentException.class, () -> bot.setSlot(-1));
				assertThrows(IllegalArgumentException.class, () -> bot.useItem(EquipmentSlot.HEAD));
				assertThrows(IllegalArgumentException.class,
						() -> bot.useItemOn(otherBlock, BlockFace.UP, EquipmentSlot.HAND));
				assertThrows(IllegalArgumentException.class,
						() -> bot.useItemOn(ownBlock, BlockFace.NORTH_EAST, EquipmentSlot.HAND));
				assertThrows(IllegalArgumentException.class, () -> bot.startBreaking(otherBlock, BlockFace.UP));
				assertThrows(IllegalArgumentException.class, () -> bot.attack(otherPig));
				assertThrows(IllegalArgumentException.class, () -> bot.interact(otherPig, EquipmentSlot.HAND));
				assertThrows(IllegalArgumentException.class, () -> bot.sendCommand(null));
				assertThrows(IllegalArgumentException.class, () -> bot.sendCommand(""));
				assertThrows(IllegalArgumentException.class, () -> bot.sendCommand("   "));
				assertThrows(IllegalArgumentException.class, () -> bot.sendCommand("/"));
				assertThrows(IllegalArgumentException.class, () -> bot.sendCommand("/  "));
				assertEquals(0, bot.getSlot(), "a refused slot was kept");
				// A failed bot sends nothing, these included.
				bot.leftClick();
				bot.swapHands();
				assertInstanceOf(BotBody.class, bot).fail();
				return null;
			});
			PaperServer.awaitTicks(5);
			assertEquals(List.of(), scene.packets().sent(), "a refused or dropped control was sent");
			assertTrue(scene.events().interacts.isEmpty(), "a refused or dropped control fired an event");
			assertClean(scene, logs);
		} finally {
			removeEntity(pig);
			tearDown(scene);
		}
	}

	@Test
	void useItemRightClicksTheBlockItLooksAt() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			var structure = scene.structure();
			Block target = PaperServer
					.onMain(() -> structure.world.getBlockAt(structure.cx, FLOOR_Y, structure.cz + 1));
			control(() -> {
				bot.player().getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND));
				// The look ray meets the floor on the block in front of the bot.
				bot.setLook(0, 60);
				bot.useItem(EquipmentSlot.HAND);
			});
			PaperServer.awaitTicks(5);
			var interacts = List.copyOf(scene.events().interacts);
			assertEquals(1, interacts.size(), "not one PlayerInteractEvent: " + interacts);
			assertEquals(Action.RIGHT_CLICK_BLOCK, interacts.getFirst().getAction(),
					"the look did not reach the server");
			assertEquals(target, interacts.getFirst().getClickedBlock());
			assertEquals(List.of("ServerboundUseItemPacket"), scene.packets().sent(), "not one use-item packet");
			assertClean(scene, logs);
		} finally {
			tearDown(scene);
		}
	}

	@Test
	void interactKeepsTheBotSneaking() throws Exception {
		Scene scene = null;
		Pig pig = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			var target = pig(scene.structure(), 2);
			pig = target;
			control(() -> bot.setSneak(true));
			PaperServer.awaitTicks(2);
			assertTrue(PaperServer.onMain(() -> bot.player().isSneaking()), "the bot is not sneaking");
			control(() -> bot.interact(target, EquipmentSlot.HAND));
			PaperServer.awaitTicks(3);
			assertEquals(INTERACT_EVENTS.size(), scene.events().entityInteracts.size(), "the interact did not land");
			assertTrue(PaperServer.onMain(() -> bot.player().isSneaking()), "the interact released sneak");
			assertClean(scene, logs);
		} finally {
			removeEntity(pig);
			tearDown(scene);
		}
	}

	@Test
	void respawnResendsHeldKeys() throws Exception {
		Scene scene = null;
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			control(() -> bot.setSneak(true));
			PaperServer.awaitTicks(2);
			control(() -> bot.player().setHealth(0));
			PaperServer.awaitTicks(3);
			assertTrue(PaperServer.onMain(() -> bot.player().isDead()), "the bot did not die");
			scene.packets().seen.clear();
			control(bot::respawn);
			PaperServer.awaitTicks(5);
			assertEquals(1, scene.events().respawns.size(), "not one PlayerRespawnEvent");
			var sent = scene.packets().sent();
			int loaded = sent.indexOf("ServerboundPlayerLoadedPacket");
			assertTrue(loaded >= 0, "no loaded ack: " + sent);
			assertTrue(sent.subList(loaded, sent.size()).contains("ServerboundPlayerInputPacket"),
					"no input packet after the loaded ack: " + sent);
			PaperServer.onMain(() -> {
				assertTrue(bot.isSneak(), "the sneak key was released");
				assertTrue(bot.player().isSneaking(), "the respawned bot is not sneaking");
				return null;
			});
			assertClean(scene, logs);
		} finally {
			tearDown(scene);
		}
	}

	/** Kicks the bot on its first left click into air. */
	public static final class KickOnLeftClick implements Listener {
		@EventHandler
		public void onInteract(PlayerInteractEvent event) {
			if (event.getPlayer().getName().equals(NAME) && event.getAction() == Action.LEFT_CLICK_AIR) {
				event.getPlayer().kick(net.kyori.adventure.text.Component.text("kicked by BotHandsIT"));
			}
		}
	}

	@Test
	void kickDuringOneShotsDropsTheRest() throws Exception {
		Scene scene = null;
		var kicker = new KickOnLeftClick();
		try (var logs = new LogCapture()) {
			scene = scene();
			var bot = scene.bot();
			var roster = scene.roster();
			register(kicker);
			var player = bot.player();
			control(() -> {
				bot.setLook(0, -90);
				bot.leftClick();
				bot.swapHands();
			});
			PaperServer.awaitTicks(5);
			PaperServer.onMain(() -> {
				assertEquals(List.of(), roster.list(), "kicked bot is still listed");
				assertFalse(player.isOnline(), "kicked bot is still online");
				return null;
			});
			assertEquals(List.of(player), List.copyOf(scene.players().quit), "PlayerQuitEvent not fired once");
			assertTrue(scene.events().swaps.isEmpty(), "the swap after the kick reached the server");
			assertEquals(List.of("ServerboundSwingPacket"), scene.packets().sent(), "a packet was sent after the kick");
			var errors = logs.lines.stream()
					.filter(l -> l.contains("ClosedChannelException") || l.contains(" failed, it stops ticking"))
					.toList();
			assertEquals(List.of(), errors, "errors logged on the kick");
		} finally {
			tearDown(scene, kicker);
		}
	}
}
