package fr.nekotine.core.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import fr.nekotine.core.NekotineCore;

public class AppliedBlockPatch {

	private BlockState initialState;
	
	protected AppliedBlockPatch(BlockState state) {
		initialState = state;
		NekotineCore.MODULES.get(TempBlockModule.class).addPatch(this);
	}
	
	public Block getTargetedBlock() {
		return initialState.getBlock();
	}
	
	public void unpatch() {
		unpatch(false);
	}
	
	public void unpatch(boolean applyPhysics) {
		initialState.update(true, applyPhysics);
	}
	
}
