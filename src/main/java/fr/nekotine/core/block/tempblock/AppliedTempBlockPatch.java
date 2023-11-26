package fr.nekotine.core.block.tempblock;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import fr.nekotine.core.block.BlockPatch;
import fr.nekotine.core.ioc.Ioc;

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
		Ioc.resolve(TempBlockModule.class).unpatch(this, applyPhysics);
	}
	
	public void unpatch() {
		Ioc.resolve(TempBlockModule.class).unpatch(this);
	}
	
}
