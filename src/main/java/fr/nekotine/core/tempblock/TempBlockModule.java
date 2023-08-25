package fr.nekotine.core.tempblock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.block.Block;

import fr.nekotine.core.module.PluginModule;

public class TempBlockModule extends PluginModule{

	private Map<Block, LinkedList<AppliedBlockPatch>> map = new HashMap<>();
	
	public void addPatch(AppliedBlockPatch patch) {
		var block = patch.getTargetedBlock();
		if (!map.containsKey(block)) {
			map.put(block, new LinkedList<>());
		}
		map.get(block).addLast(patch);
	}
	
	public void unpatch(AppliedBlockPatch patch, boolean applyPhysics) {
		var block = patch.getTargetedBlock();
		if (!map.containsKey(block)) {
			return;
		}
		var list = map.get(block);
		var index = list.indexOf(patch);
		if (index <= 0) {
			map.remove(block);
			patch.unpatch(applyPhysics);
			return;
		}
		list.subList(index, list.size()-1).clear();
		patch.unpatch(applyPhysics);
	}
	
	public void unpatch(AppliedBlockPatch patch) {
		unpatch(patch, false);
	}
	
}
