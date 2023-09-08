package fr.nekotine.core.util;

import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class DebugUtil {

	public static BlockDisplay debugBoundingBox(World world, BoundingBox box, BlockData data) {
		var min = box.getMin();
		var max = box.getMax();
		var scale = new Vector3f((float)(max.getX()-min.getX()), (float)(max.getY()-min.getY()), (float)(max.getZ()-min.getZ()));
		var transform = new Transformation(new Vector3f(), new AxisAngle4f(), scale, new AxisAngle4f());
		var display = (BlockDisplay)world.spawnEntity(min.toLocation(world), EntityType.BLOCK_DISPLAY);
		display.setBlock(data);
		display.setTransformation(transform);
		return display;
	}
	
}