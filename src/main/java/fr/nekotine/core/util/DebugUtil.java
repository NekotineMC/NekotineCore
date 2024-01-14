package fr.nekotine.core.util;

import java.util.Collection;
import java.util.LinkedList;

import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.BoundingBox;

public class DebugUtil {

	private static DebugUtil instance = new DebugUtil();
	
	private Collection<BlockDisplay> debugEntities = new LinkedList<>();
	
	private void clear() {
		for (var e : debugEntities) {
			e.remove();
		}
		debugEntities.clear();
	}
	
	public static BlockDisplay debugBoundingBox(World world, BoundingBox box, BlockData data) {
		var display = SpatialUtil.fillBoundingBox(world, box, data);
		instance.debugEntities.add(display);
		return display;
	}
	
	public static void clearDebugEntities() {
		instance.clear();
	}
	
}
