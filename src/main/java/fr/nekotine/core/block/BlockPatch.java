package fr.nekotine.core.block;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;

import fr.nekotine.core.block.fakeblock.AppliedFakeBlockPatch;
import fr.nekotine.core.block.fakeblock.FakeBlockModule;
import fr.nekotine.core.block.tempblock.AppliedTempBlockPatch;
import fr.nekotine.core.block.tempblock.TempBlockModule;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.module.ModuleManager;

public class BlockPatch {

	private Consumer<BlockState> patch;
	
	public BlockPatch(Consumer<BlockState> patch) {
		this.patch = patch;
		Ioc.resolve(ModuleManager.class).tryLoad(TempBlockModule.class); // Silently ensure module is loaded
		Ioc.resolve(ModuleManager.class).tryLoad(FakeBlockModule.class); // Silently ensure module is loaded
	}
	
	public Consumer<BlockState> getPatchingFunction(){
		return patch;
	}
	
	public AppliedTempBlockPatch patch(Block block, boolean applyPhysics) {
		return Ioc.resolve(TempBlockModule.class).applyPatch(this, block, applyPhysics);
	}
	
	public AppliedTempBlockPatch patch(Block block) {
		return patch(block, false);
	}
	
	public AppliedFakeBlockPatch patchPlayer(Player player, Block block) {
		return Ioc.resolve(FakeBlockModule.class).applyPatch(this, block, player);
	}
	
	public List<AppliedFakeBlockPatch> patchPlayer(Player player, BoundingBox boundingbox) {
		var fakeModule = Ioc.resolve(FakeBlockModule.class);
		var world = player.getWorld();
		var col = new LinkedList<AppliedFakeBlockPatch>();
		var min = new BlockVector(boundingbox.getMin());
		var max = new BlockVector(boundingbox.getMax());
		for (var x = min.getBlockX(); x < max.getBlockX(); x++) {
			for (var y = min.getBlockY(); y < max.getBlockY(); y++) {
				for (var z = min.getBlockZ(); z < max.getBlockZ(); z++) {
					col.add(fakeModule.applyPatch(this, world.getBlockAt(x, y, z), player));
				}
			}
		}
		return col;
	}
	
	public void unpatchAll(boolean applyPhysics) {
		Ioc.resolve(TempBlockModule.class).unpatchAll(this, applyPhysics);
		Ioc.resolve(FakeBlockModule.class).unpatchAll(this);
	}
	
	public void unpatchAll() {
		unpatchAll(false);
	}
	
}
