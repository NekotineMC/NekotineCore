package fr.nekotine.core.block.fakeblock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.nekotine.core.block.BlockPatch;
import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.util.EventUtil;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;

public class FakeBlockModule implements IPluginModule, Listener{

	private Map<Player, Map<Block, LinkedList<AppliedFakeBlockPatch>>> map = new HashMap<>();
	
	public FakeBlockModule() {
		EventUtil.register(this);
	}
	
	public void addPatch(Player player, AppliedFakeBlockPatch patch) {
		map.computeIfAbsent(player,o -> new HashMap<>()).computeIfAbsent(patch.getTargetedBlock(),o -> new LinkedList<>()).addLast(patch);
		player.sendBlockChange(patch.getTargetedBlock().getLocation(), appliedBlockData(patch));
	}
	
	public AppliedFakeBlockPatch applyPatch(BlockPatch patch, Block block, Player player) {
		var applied = new AppliedFakeBlockPatch(player, block, patch);
		addPatch(player, applied);
		return applied;
	}
	
	public void unpatch(AppliedFakeBlockPatch patch) {
		var block = patch.getTargetedBlock();
		var player = patch.getAffectedPlayer();
		var bmap = map.get(player);
		if (bmap == null) {
			return;
		}
		var list = bmap.get(block);
		if (list == null) {
			return;
		}
		if (list.getLast() == patch) {
			list.removeLast();
			if (list.isEmpty()) {
				player.sendBlockChange(patch.getTargetedBlock().getLocation(), patch.getTargetedBlock().getBlockData());
				bmap.remove(block);
			}else {
				player.sendBlockChange(patch.getTargetedBlock().getLocation(), appliedBlockData(list.getLast()));
			}
		}else {
			list.remove(patch);
		}
	}
	
	public void unpatchAll(BlockPatch patch) {
		for (var applied : map.values().stream()
				.flatMap(m -> m.values().stream())
				.flatMap(LinkedList::stream)
				.filter(a -> a.getPatchSource()==patch)
				.collect(Collectors.toUnmodifiableSet())) {
			unpatch(applied);
		}
	}
	
	@Override
	public void unload() {
		EventUtil.unregister(this);
		for (var applied : map.values().stream()
				.flatMap(m -> m.values().stream())
				.flatMap(LinkedList::stream)
				.collect(Collectors.toUnmodifiableSet())) {
			unpatch(applied);
		}
	}
	
	private BlockData appliedBlockData(AppliedFakeBlockPatch patch) {
		var state = patch.getTargetedBlock().getState();
		patch.getPatchSource().getPatchingFunction().accept(state);
		return state.getBlockData();
	}
	
	// Events
	
	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent evt) {
		map.remove(evt.getPlayer());
	}
	
	@EventHandler
	private void onPlayerLoadChuck(PlayerChunkLoadEvent evt) {
		var player = evt.getPlayer();
		var bmap = map.get(player);
		if (bmap == null) {
			return;
		}
		var chunk = evt.getChunk();
		LinkedList<AppliedFakeBlockPatch> list;
		AppliedFakeBlockPatch patch;
		for (var b : bmap.keySet().stream().filter(b -> b.getChunk().equals(chunk)).collect(Collectors.toUnmodifiableSet())) {
			list = bmap.get(b);
			if (list == null) {
				continue;
			}
			patch = list.getLast();
			player.sendBlockChange(patch.getTargetedBlock().getLocation(), appliedBlockData(patch));
		}
	}
	
}
