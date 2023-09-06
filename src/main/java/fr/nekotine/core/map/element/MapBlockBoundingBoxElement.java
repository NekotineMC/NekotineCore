package fr.nekotine.core.map.element;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.block.BlockPatch;
import fr.nekotine.core.block.fakeblock.AppliedFakeBlockPatch;
import fr.nekotine.core.block.tempblock.AppliedTempBlockPatch;

/**
 * Une élément de carte qui représente un boite de collision en block plein.
 * @author XxGoldenbluexX
 *
 */
public class MapBlockBoundingBoxElement implements ConfigurationSerializable{

	// ---------------------- Serialization

		public static MapBlockBoundingBoxElement deserialize(Map<String, Object> map) {
			var corner1 = (BlockVector) 			map.get("corner1");
			var corner2 = (BlockVector) 			map.get("corner2");
			return new MapBlockBoundingBoxElement(corner1, corner2);
		}

		@Override
		public @NotNull Map<String, Object> serialize() {
			var map = new HashMap<String, Object>();
			map.put("corner1", new BlockVector(boundingBox.getMin()));
			map.put("corner2", new BlockVector(boundingBox.getMax()));
			return map;
		}

		// ----------------------
		
		private BoundingBox boundingBox = new BoundingBox();
		
		public MapBlockBoundingBoxElement() {}
		
		public MapBlockBoundingBoxElement(BlockVector corner1, BlockVector corner2) {
			boundingBox.resize(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
		}
		
		public BoundingBox get() {
			return boundingBox;
		}
		
		public Collection<AppliedTempBlockPatch> applyBlockPatch(World world, BlockPatch patch, boolean applyPhysics){
			var col = new LinkedList<AppliedTempBlockPatch>();
			var min = new BlockVector(boundingBox.getMin());
			var max = new BlockVector(boundingBox.getMax());
			for (var x = min.getBlockX(); x < max.getBlockX(); x++) {
				for (var y = min.getBlockY(); y < max.getBlockY(); y++) {
					for (var z = min.getBlockZ(); z < max.getBlockZ(); z++) {
						col.add(patch.patch(world.getBlockAt(x, y, z), applyPhysics));
					}
				}
			}
			return col;
		}
	
		public Collection<AppliedTempBlockPatch> applyBlockPatch(World world, BlockPatch patch){
			var col = new LinkedList<AppliedTempBlockPatch>();
			var min = new BlockVector(boundingBox.getMin());
			var max = new BlockVector(boundingBox.getMax());
			for (var x = min.getBlockX(); x < max.getBlockX(); x++) {
				for (var y = min.getBlockY(); y < max.getBlockY(); y++) {
					for (var z = min.getBlockZ(); z < max.getBlockZ(); z++) {
						col.add(patch.patch(world.getBlockAt(x, y, z)));
					}
				}
			}
			return col;
		}
		
		public Collection<AppliedFakeBlockPatch> applyFakeBlockPatch(Player player, BlockPatch patch){
			var col = new LinkedList<AppliedFakeBlockPatch>();
			var world = player.getWorld();
			var min = new BlockVector(boundingBox.getMin());
			var max = new BlockVector(boundingBox.getMax());
			for (var x = min.getBlockX(); x < max.getBlockX(); x++) {
				for (var y = min.getBlockY(); y < max.getBlockY(); y++) {
					for (var z = min.getBlockZ(); z < max.getBlockZ(); z++) {
						col.add(patch.patchPlayer(player, world.getBlockAt(x, y, z)));
					}
				}
			}
			return col;
		}
}
