package fr.nekotine.core.block.tempblock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import fr.nekotine.core.block.BlockPatch;
import fr.nekotine.core.module.IPluginModule;

public class TempBlockModule implements IPluginModule{

	private Map<Block, LinkedList<AppliedTempBlockPatch>> map = new HashMap<>();
	
	public void addPatch(AppliedTempBlockPatch patch) {
		map.computeIfAbsent(patch.getTargetedBlock(),o -> new LinkedList<>()).addLast(patch);
	}
	
	public AppliedTempBlockPatch applyPatch(BlockPatch patch, Block block, boolean applyPhysics) {
		var existing = getPatch(block, patch);
		if (existing.isPresent()) {
			return existing.get();
		}
		var applied = new AppliedTempBlockPatch(baseState(block), patch);
		applyState(applied, applyPhysics);
		addPatch(applied);
		return applied;
	}
	
	public void unpatch(AppliedTempBlockPatch patch, boolean applyPhysics) {
		var block = patch.getTargetedBlock();
		var list = map.get(block);
		if (list == null) {
			return;
		}
		if (list.getLast() == patch) {
			list.removeLast();
			if (list.isEmpty()) {
				patch.getInitialState().update(true, applyPhysics);
				map.remove(block);
			}else {
				applyState(list.getLast(), applyPhysics);
			}
		}else {
			list.remove(patch);
		}
	}
	
	private void applyState(AppliedTempBlockPatch patch, boolean applyPhysics) {
		var state = patch.getTargetedBlock().getState();
		patch.getPatchSource().getPatchingFunction().accept(state);
		state.update(true, applyPhysics);
	}
	
	private BlockState baseState(Block block) {
		var list = map.get(block);
		if (list == null) {
			return block.getState();
		}
		return list.getFirst().getInitialState();
	}
	
	public AppliedTempBlockPatch applyPatch(BlockPatch patch, Block block) {
		return applyPatch(patch, block, false);
	}
	
	public void unpatch(AppliedTempBlockPatch patch) {
		unpatch(patch, false);
	}
	
	public void unpatchAll(BlockPatch patch, boolean applyPhysics) {
		for (var applied : map.values().stream().flatMap(LinkedList::stream).filter(a -> a.getPatchSource()==patch).collect(Collectors.toUnmodifiableSet())) {
			unpatch(applied, applyPhysics);
		}
	}
	
	public void unpatchAll(BlockPatch patch) {
		unpatchAll(patch, false);
	}
	
	public Optional<AppliedTempBlockPatch> getPatch(Block block, BlockPatch patch) {
		var list = map.get(block);
		if (list != null) {
			return list.stream().filter(p -> p.getPatchSource()==patch).findFirst();
		}
		return Optional.empty();
	}
	
	@Override
	public void unload() {
		for (var patch : map.values().stream().flatMap(LinkedList::stream).collect(Collectors.toUnmodifiableSet())) {
			unpatch(patch);
		}
	}
	
}
