package fr.nekotine.core.block.fakeblock;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.nekotine.core.block.BlockPatch;
import fr.nekotine.core.ioc.Ioc;

public class AppliedFakeBlockPatch{
	
	private Player affected;
	
	private Block target;
	
	private BlockPatch patchSource;
	
	protected AppliedFakeBlockPatch(Player affectedPlayer, Block block, BlockPatch patchSource) {
		affected = affectedPlayer;
		target = block;
		this.patchSource = patchSource;
	}
	
	public Block getTargetedBlock() {
		return target;
	}
	
	public Player getAffectedPlayer() {
		return affected;
	}
	
	public BlockPatch getPatchSource() {
		return patchSource;
	}
	
	public void unpatch() {
		Ioc.resolve(FakeBlockModule.class).unpatch(this);
	}
	
}
