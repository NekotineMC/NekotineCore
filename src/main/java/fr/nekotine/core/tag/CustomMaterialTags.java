package fr.nekotine.core.tag;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;

public class CustomMaterialTags{
	private static NamespacedKey keyFor(String key) {
        //noinspection deprecation
        return new NamespacedKey("nekotine", key + "_settag");
    }
	
	//
	
	public static MaterialSetTag BUTTONS =
		new MaterialSetTag(keyFor("buttons"))
		.endsWith("BUTTON")
		.ensureSize("BUTTONS", 13).lock();
	
	public static MaterialSetTag ANVILS =
		new MaterialSetTag(keyFor("anvils"))
		.endsWith("ANVIL")
		.ensureSize("ANVILS", 3).lock();
	
	public static MaterialSetTag COMMAND_BLOCKS = 
		new MaterialSetTag(keyFor("command_blocks"))
		.endsWith("COMMAND_BLOCK")
		.ensureSize("COMMAND_BLOCKS", 3).lock();
	
	public static MaterialSetTag CHESTS = 
		new MaterialSetTag(keyFor("chests"))
		.endsWith("CHEST")
		.ensureSize("CHESTS", 3).lock();
		 
	//
	
	public static CustomMaterialSetTag<BlockFace> EQUIP_BLOCKFACE_BLOCKED = new CustomMaterialSetTag<BlockFace>()
			.add(face -> face == BlockFace.NORTH || face == BlockFace.SOUTH, 
				Material.BELL)
			.add(face -> face.equals(BlockFace.NORTH), Material.CHISELED_BOOKSHELF);

	public static CustomMaterialSetTag<BlockFace> ADVENTURE_EQUIP_BLOCKED = 
		new CustomMaterialSetTag<BlockFace>()
		.add(MaterialTags.SIGNS)
		.add(ANVILS)
		.add(MaterialTags.BEDS)
		.add(BUTTONS)
		.add(MaterialTags.WOODEN_DOORS)
		.add(MaterialTags.FENCE_GATES)
		.add(MaterialTags.SHULKER_BOXES)
		.add(MaterialTags.WOODEN_TRAPDOORS)
		.add(Material.BARREL, Material.BEACON, Material.BLAST_FURNACE, Material.BREWING_STAND)
		.add(CHESTS)
		.add(Material.DISPENSER, Material.DROPPER, Material.FURNACE, Material.HOPPER)
		.add(Material.SMOKER, Material.ENCHANTING_TABLE, Material.CRAFTING_TABLE, Material.LEVER)
		.add(Material.GRINDSTONE, Material.LECTERN, Material.LOOM, Material.STONECUTTER)
		.add(Material.CARTOGRAPHY_TABLE, Material.SMITHING_TABLE, Material.NOTE_BLOCK)
		.add(EQUIP_BLOCKFACE_BLOCKED);
	
	public static CustomMaterialSetTag<BlockFace> SURVIVAL_EQUIP_BLOCKED = 
		new CustomMaterialSetTag<BlockFace>()
		.add(ADVENTURE_EQUIP_BLOCKED)
		.add(Material.REPEATER, Material.COMPARATOR, Material.DAYLIGHT_DETECTOR);
	
	
	public static CustomMaterialSetTag<BlockFace> CREATIVE_EQUIP_BLOCKED = 
		new CustomMaterialSetTag<BlockFace>()
		.add(SURVIVAL_EQUIP_BLOCKED)
		.add(COMMAND_BLOCKS)
		.add(Material.STRUCTURE_BLOCK);
}
