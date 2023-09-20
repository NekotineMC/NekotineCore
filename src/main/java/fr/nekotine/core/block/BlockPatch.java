package fr.nekotine.core.block;

import java.util.function.Consumer;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import fr.nekotine.core.NekotineCore;
import fr.nekotine.core.block.fakeblock.AppliedFakeBlockPatch;
import fr.nekotine.core.block.fakeblock.FakeBlockModule;
import fr.nekotine.core.block.tempblock.AppliedTempBlockPatch;
import fr.nekotine.core.block.tempblock.TempBlockModule;

public class BlockPatch {

	private Consumer<BlockState> patch;
	
	public BlockPatch(Consumer<BlockState> patch) {
		this.patch = patch;
		NekotineCore.MODULES.tryLoad(TempBlockModule.class); // Silently load module
		NekotineCore.MODULES.tryLoad(FakeBlockModule.class); // Silently load module
	}
	
	public Consumer<BlockState> getPatchingFunction(){
		return patch;
	}
	
	public AppliedTempBlockPatch patch(Block block, boolean applyPhysics) {
		return NekotineCore.MODULES.get(TempBlockModule.class).applyPatch(this, block, applyPhysics);
	}
	
	public AppliedTempBlockPatch patch(Block block) {
		return patch(block, false);
	}
	
	public AppliedFakeBlockPatch patchPlayer(Player player, Block block) {
		return NekotineCore.MODULES.get(FakeBlockModule.class).applyPatch(this, block, player);
	}
	
	public void unpatchAll(boolean applyPhysics) {
		NekotineCore.MODULES.get(TempBlockModule.class).unpatchAll(this, applyPhysics);
		NekotineCore.MODULES.get(FakeBlockModule.class).unpatchAll(this);
	}
	
	public void unpatchAll() {
		unpatchAll(false);
	}
	
}
