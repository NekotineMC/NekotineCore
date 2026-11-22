package fr.nekotine.core.navigation.astar;

import fr.nekotine.core.navigation.BlockView;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;

/**
 * The blocks of a box of a world, read from chunk snapshots taken on the main
 * thread: the only world access of an off-thread search. Positions outside the
 * box, or in a chunk that was not loaded, read as {@code null} and
 * {@link Cell#BLOCKED}. Immutable once built, so any thread may read it.
 */
final class SnapshotView implements BlockView, CellGrid {

	private final int minX;
	private final int minY;
	private final int minZ;
	private final int maxX;
	private final int maxY;
	private final int maxZ;
	private final int chunkX;
	private final int chunkZ;
	private final int chunksAlongX;
	private final ChunkSnapshot[] chunks;

	private SnapshotView(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, ChunkSnapshot[] chunks,
			int chunksAlongX) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		this.chunkX = minX >> 4;
		this.chunkZ = minZ >> 4;
		this.chunksAlongX = chunksAlongX;
		this.chunks = chunks;
	}

	/**
	 * Snapshots the loaded chunks over a box, bounds inclusive, clamped to the
	 * world height; never loads a chunk. Main thread only.
	 */
	static SnapshotView take(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		int lowY = Math.max(minY, world.getMinHeight());
		int highY = Math.min(maxY, world.getMaxHeight() - 1);
		int alongX = (maxX >> 4) - (minX >> 4) + 1;
		int alongZ = (maxZ >> 4) - (minZ >> 4) + 1;
		var chunks = new ChunkSnapshot[alongX * alongZ];
		for (int i = 0; i < alongX; i++) {
			for (int j = 0; j < alongZ; j++) {
				int cx = (minX >> 4) + i;
				int cz = (minZ >> 4) + j;
				if (world.isChunkLoaded(cx, cz)) {
					chunks[j * alongX + i] = world.getChunkAt(cx, cz).getChunkSnapshot(false, false, false);
				}
			}
		}
		return new SnapshotView(minX, lowY, minZ, maxX, highY, maxZ, chunks, alongX);
	}

	@Override
	public BlockData getBlockData(int x, int y, int z) {
		if (x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ) {
			return null;
		}
		var chunk = chunks[((z >> 4) - chunkZ) * chunksAlongX + (x >> 4) - chunkX];
		return chunk == null ? null : chunk.getBlockData(x & 15, y, z & 15);
	}

	@Override
	public Cell cell(int x, int y, int z) {
		return classify(getBlockData(x, y, z));
	}

	/**
	 * Wooden doors are doors; fences, walls and fence gates are not floors (the
	 * body would stand 1.5 blocks up) and block; liquids and hazards (fire,
	 * campfires, magma, cactus, cobweb, powder snow, berry bushes, wither roses,
	 * dripstone) block the body (no swimming), solid or not; carpets are open;
	 * other solid blocks are floors, partial ones (slabs, stairs, snow layers)
	 * counted as full blocks; waterlogged non-solid blocks block; the rest is open.
	 */
	static Cell classify(BlockData data) {
		if (data == null) {
			return Cell.BLOCKED;
		}
		var material = data.getMaterial();
		if (Tag.WOODEN_DOORS.isTagged(material)) {
			return Cell.DOOR;
		}
		if (Tag.FENCES.isTagged(material) || Tag.WALLS.isTagged(material) || Tag.FENCE_GATES.isTagged(material)) {
			return Cell.BLOCKED;
		}
		switch (material) {
			case WATER, LAVA, BUBBLE_COLUMN, KELP, KELP_PLANT, SEAGRASS, TALL_SEAGRASS, FIRE, SOUL_FIRE, CAMPFIRE,
					SOUL_CAMPFIRE, MAGMA_BLOCK, CACTUS, COBWEB, POWDER_SNOW, SWEET_BERRY_BUSH, WITHER_ROSE,
					POINTED_DRIPSTONE :
				return Cell.BLOCKED;
			case MOSS_CARPET, PALE_MOSS_CARPET :
				return Cell.OPEN;
			default :
				break;
		}
		if (Tag.WOOL_CARPETS.isTagged(material)) {
			return Cell.OPEN;
		}
		if (material.isSolid()) {
			return Cell.SOLID;
		}
		if (data instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) {
			return Cell.BLOCKED;
		}
		return Cell.OPEN;
	}
}
