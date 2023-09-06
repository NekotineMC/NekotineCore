package fr.nekotine.core.block.tempblock;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.block.BlockPatch;

public class AppliedTempBlockPatch{
	
	private BlockState initialState;
	
	private BlockPatch patchSource;
	
	protected AppliedTempBlockPatch(BlockState state, BlockPatch patchSource) {
		initialState = state;
		this.patchSource = patchSource;
	}
	
	public Block getTargetedBlock() {
		return initialState.getBlock();
	}
	
	public BlockState getInitialState() {
		return initialState;
	}

	public BlockPatch getPatchSource() {
		return patchSource;
	}
	public void unpatch(boolean applyPhysics) {
		NekotineCore.MODULES.get(TempBlockModule.class).unpatch(this, applyPhysics);
	}
	
	public void unpatch() {
		NekotineCore.MODULES.get(TempBlockModule.class).unpatch(this);
	}
	
}
