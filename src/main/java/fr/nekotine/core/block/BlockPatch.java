package fr.nekotine.core.block;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import fr.nekotine.core.NekotineCore;

public class BlockPatch {

	private Consumer<BlockState> patch;
	
	private List<AppliedBlockPatch> appliedPatches = new LinkedList<>();
	
	public BlockPatch(Consumer<BlockState> patch) {
		this.patch = patch;
	}
	
	public AppliedBlockPatch patch(Block block, boolean applyPhysics) {
		var existing = appliedPatches.stream().filter(a -> a.getTargetedBlock().equals(block)).findFirst();
		if (existing.isPresent()) {
			return existing.get();
		}
		var applied = new AppliedBlockPatch(block.getState());
		var state = block.getState();
		patch.accept(state);
		state.update(true, applyPhysics);
		appliedPatches.add(applied);
		return applied;
	}
	
	public AppliedBlockPatch patch(Block block) {
		return patch(block, false);
	}
	
	public void unpatchAll(boolean applyPhysics) {
		var tempBlock = NekotineCore.MODULES.get(TempBlockModule.class);
		for (var applied : appliedPatches) {
			tempBlock.unpatch(applied, applyPhysics);
		}
	}
	
	public void unpatchAll() {
		unpatchAll(false);
	}
	
}
