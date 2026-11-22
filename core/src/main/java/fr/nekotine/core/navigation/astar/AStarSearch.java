package fr.nekotine.core.navigation.astar;

import fr.nekotine.core.navigation.BlockView;
import fr.nekotine.core.navigation.PathMove;
import fr.nekotine.core.navigation.PathRule;
import java.util.Arrays;

/**
 * A grid A* for a player-sized body over a {@link CellGrid}, with no server and
 * no thread of its own: one instance runs one search.
 *
 * <h2>Walking model</h2>
 *
 * <p>
 * A body stands at a feet block with a {@link Cell#SOLID} floor below and
 * passable feet and head blocks. From there it may walk to any of the 8
 * neighbouring columns on the same level (a diagonal only when both sides are
 * clear for feet and head, and neither end is a door), step one block up on a
 * straight move when the block above its head is clear, or walk off an edge on
 * a straight move and land up to {@link #MAX_DROP} blocks lower. The rule only
 * removes positions and adds cost; it is never asked about the start.
 *
 * <h2>Determinism</h2>
 *
 * <p>
 * Neighbours are visited in a fixed order and nodes get dense ids in discovery
 * order; the open set orders by f, then h, then id. The same grid, rule and
 * inputs give the same path.
 *
 * <h2>Layout</h2>
 *
 * <p>
 * Packed {@code long} coordinate keys map to dense {@code int} ids; per-node
 * data lives in primitive arrays; the open set is an indexed binary min-heap
 * with decrease-key. No boxed nodes.
 */
final class AStarSearch {

	/** The highest drop a path may take, in blocks. */
	static final int MAX_DROP = 3;

	static final double WALK_COST = 1;

	static final double DIAGONAL_COST = Math.sqrt(2);

	static final double STEP_UP_COST = 2;

	static final double DOOR_COST = 2;

	/** A drop costs a walk plus this much per block of fall. */
	static final double DROP_COST_PER_BLOCK = 0.5;

	private static final double OCTILE = Math.sqrt(2) - 1;

	/** Straight neighbours first, then diagonals: the fixed visiting order. */
	private static final int[] DX = {0, 1, 0, -1, 1, 1, -1, -1};

	private static final int[] DZ = {-1, 0, 1, 0, -1, 1, 1, -1};

	private static final byte CLOSED = 1;

	private static final byte RULE_KNOWN = 2;

	private static final byte RULE_ALLOWED = 4;

	private static final PathMove[] MOVES = PathMove.values();

	/**
	 * The outcome of a search: feet block positions, the start first, with the move
	 * that reaches each one.
	 */
	record Result(int[] xs, int[] ys, int[] zs, PathMove[] moves, boolean reached) {

		int size() {
			return xs.length;
		}
	}

	private final CellGrid grid;

	private final BlockView view;

	private final PathRule rule;

	private final IdTable table = new IdTable();

	private int count;

	private int[] xs = new int[256];

	private int[] ys = new int[256];

	private int[] zs = new int[256];

	private double[] g = new double[256];

	private double[] f = new double[256];

	private double[] h = new double[256];

	private double[] extra = new double[256];

	private int[] parent = new int[256];

	private byte[] move = new byte[256];

	private byte[] flags = new byte[256];

	private int[] heapIndex = new int[256];

	private int[] heap = new int[256];

	private int heapSize;

	private int gx;

	private int gy;

	private int gz;

	private AStarSearch(CellGrid grid, BlockView view, PathRule rule) {
		this.grid = grid;
		this.view = view;
		this.rule = rule;
	}

	/**
	 * Searches a path from the start to the goal feet block. A start or goal inside
	 * a non-passable block whose block above is passable (e.g. standing on a slab
	 * or a dirt path) moves up one block first.
	 *
	 * @param budget
	 *            how many nodes the search may expand; running out counts as
	 *            unreachable
	 * @return the path to the goal, or, if the goal is not reached, to the expanded
	 *         node closest to it (Euclidean, ties to the lowest cost)
	 * @throws IllegalStateException
	 *             if the rule returns a negative or non-finite cost
	 */
	static Result search(CellGrid grid, BlockView view, PathRule rule, int sx, int sy, int sz, int gx, int gy, int gz,
			int budget) {
		if (budget < 1) {
			throw new IllegalArgumentException("node budget must be at least 1, got " + budget);
		}
		var search = new AStarSearch(grid, view, rule);
		return search.run(sx, liftOutOfFloor(grid, sx, sy, sz), sz, gx, liftOutOfFloor(grid, gx, gy, gz), gz, budget);
	}

	private static int liftOutOfFloor(CellGrid grid, int x, int y, int z) {
		return !grid.cell(x, y, z).passable() && grid.cell(x, y + 1, z).passable() ? y + 1 : y;
	}

	private Result run(int sx, int sy, int sz, int gx, int gy, int gz, int budget) {
		this.gx = gx;
		this.gy = gy;
		this.gz = gz;
		int start = idOf(sx, sy, sz);
		g[start] = 0;
		f[start] = h[start];
		move[start] = (byte) PathMove.START.ordinal();
		// The body is already there: the rule never removes the start.
		flags[start] |= RULE_KNOWN | RULE_ALLOWED;
		push(start);
		int best = start;
		double bestDistance = Double.POSITIVE_INFINITY;
		int expanded = 0;
		boolean reached = false;
		while (heapSize > 0 && expanded < budget) {
			int current = pop();
			flags[current] |= CLOSED;
			expanded++;
			double distance = distanceSquared(current);
			if (distance < bestDistance || (distance == bestDistance && g[current] < g[best])) {
				best = current;
				bestDistance = distance;
			}
			if (xs[current] == gx && ys[current] == gy && zs[current] == gz) {
				best = current;
				reached = true;
				break;
			}
			expand(current);
		}
		return path(best, reached);
	}

	private void expand(int current) {
		int x = xs[current];
		int y = ys[current];
		int z = zs[current];
		for (int i = 0; i < 4; i++) {
			int nx = x + DX[i];
			int nz = z + DZ[i];
			if (standable(nx, y, nz)) {
				boolean door = grid.cell(nx, y, nz) == Cell.DOOR;
				relax(current, nx, y, nz, door ? PathMove.DOOR : PathMove.WALK, door ? DOOR_COST : WALK_COST);
			} else if (standable(nx, y + 1, nz) && grid.cell(x, y + 2, z).passable()) {
				relax(current, nx, y + 1, nz, PathMove.STEP_UP, STEP_UP_COST);
			} else if (grid.cell(nx, y, nz).passable() && grid.cell(nx, y + 1, nz).passable()) {
				for (int drop = 1; drop <= MAX_DROP; drop++) {
					if (!grid.cell(nx, y - drop, nz).passable()) {
						break;
					}
					if (grid.cell(nx, y - drop - 1, nz) == Cell.SOLID) {
						relax(current, nx, y - drop, nz, PathMove.DROP, WALK_COST + drop * DROP_COST_PER_BLOCK);
						break;
					}
				}
			}
		}
		if (grid.cell(x, y, z) == Cell.DOOR) {
			return;
		}
		for (int i = 4; i < 8; i++) {
			int nx = x + DX[i];
			int nz = z + DZ[i];
			if (standable(nx, y, nz) && grid.cell(nx, y, nz) == Cell.OPEN && clear(nx, y, z) && clear(x, y, nz)
					&& allowed(nx, y, z) && allowed(x, y, nz)) {
				relax(current, nx, y, nz, PathMove.WALK, DIAGONAL_COST);
			}
		}
	}

	/** @return whether a body fits there, feet and head, with no door. */
	private boolean clear(int x, int y, int z) {
		return grid.cell(x, y, z) == Cell.OPEN && grid.cell(x, y + 1, z) == Cell.OPEN;
	}

	private boolean standable(int x, int y, int z) {
		return grid.cell(x, y - 1, z) == Cell.SOLID && grid.cell(x, y, z).passable()
				&& grid.cell(x, y + 1, z).passable();
	}

	private boolean allowed(int x, int y, int z) {
		return allowed(idOf(x, y, z));
	}

	private boolean allowed(int id) {
		if ((flags[id] & RULE_KNOWN) == 0) {
			flags[id] |= RULE_KNOWN;
			if (rule.allows(view, xs[id], ys[id], zs[id])) {
				double cost = rule.cost(view, xs[id], ys[id], zs[id]);
				if (!(cost >= 0) || Double.isInfinite(cost)) {
					throw new IllegalStateException("the path rule returned the cost " + cost + " at " + xs[id] + " "
							+ ys[id] + " " + zs[id] + "; it must be finite and >= 0");
				}
				extra[id] = cost;
				flags[id] |= RULE_ALLOWED;
			}
		}
		return (flags[id] & RULE_ALLOWED) != 0;
	}

	private void relax(int current, int x, int y, int z, PathMove how, double cost) {
		int id = idOf(x, y, z);
		if ((flags[id] & CLOSED) != 0 || !allowed(id)) {
			return;
		}
		double candidate = g[current] + cost + extra[id];
		if (candidate < g[id]) {
			g[id] = candidate;
			f[id] = candidate + h[id];
			parent[id] = current;
			move[id] = (byte) how.ordinal();
			if (heapIndex[id] < 0) {
				push(id);
			} else {
				siftUp(heapIndex[id]);
			}
		}
	}

	private double distanceSquared(int id) {
		double dx = xs[id] - gx;
		double dy = ys[id] - gy;
		double dz = zs[id] - gz;
		return dx * dx + dy * dy + dz * dz;
	}

	/** Octile distance on the horizontal plane: no move costs less. */
	private double heuristic(int x, int z) {
		int dx = Math.abs(x - gx);
		int dz = Math.abs(z - gz);
		return Math.max(dx, dz) + OCTILE * Math.min(dx, dz);
	}

	private Result path(int last, boolean reached) {
		int length = 0;
		for (int id = last; id >= 0; id = parent[id]) {
			length++;
		}
		var px = new int[length];
		var py = new int[length];
		var pz = new int[length];
		var moves = new PathMove[length];
		int i = length;
		for (int id = last; id >= 0; id = parent[id]) {
			i--;
			px[i] = xs[id];
			py[i] = ys[id];
			pz[i] = zs[id];
			moves[i] = MOVES[move[id]];
		}
		return new Result(px, py, pz, moves, reached);
	}

	// Nodes

	private int idOf(int x, int y, int z) {
		long key = key(x, y, z);
		int id = table.get(key);
		if (id >= 0) {
			return id;
		}
		id = count++;
		if (id == xs.length) {
			grow();
		}
		table.put(key, id);
		xs[id] = x;
		ys[id] = y;
		zs[id] = z;
		g[id] = Double.POSITIVE_INFINITY;
		f[id] = Double.POSITIVE_INFINITY;
		h[id] = heuristic(x, z);
		parent[id] = -1;
		heapIndex[id] = -1;
		return id;
	}

	private void grow() {
		int size = xs.length * 2;
		xs = Arrays.copyOf(xs, size);
		ys = Arrays.copyOf(ys, size);
		zs = Arrays.copyOf(zs, size);
		g = Arrays.copyOf(g, size);
		f = Arrays.copyOf(f, size);
		h = Arrays.copyOf(h, size);
		extra = Arrays.copyOf(extra, size);
		parent = Arrays.copyOf(parent, size);
		move = Arrays.copyOf(move, size);
		flags = Arrays.copyOf(flags, size);
		heapIndex = Arrays.copyOf(heapIndex, size);
	}

	/** Packs a block position: 26 bits of x, 26 bits of z, 12 bits of y. */
	static long key(int x, int y, int z) {
		return ((long) x << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
	}

	// Indexed min-heap

	private boolean before(int a, int b) {
		if (f[a] != f[b]) {
			return f[a] < f[b];
		}
		if (h[a] != h[b]) {
			return h[a] < h[b];
		}
		return a < b;
	}

	private void push(int id) {
		if (heapSize == heap.length) {
			heap = Arrays.copyOf(heap, heapSize * 2);
		}
		heap[heapSize] = id;
		heapIndex[id] = heapSize;
		heapSize++;
		siftUp(heapSize - 1);
	}

	private int pop() {
		int top = heap[0];
		heapIndex[top] = -1;
		heapSize--;
		if (heapSize > 0) {
			heap[0] = heap[heapSize];
			heapIndex[heap[0]] = 0;
			siftDown(0);
		}
		return top;
	}

	private void siftUp(int index) {
		int id = heap[index];
		while (index > 0) {
			int up = (index - 1) >>> 1;
			int other = heap[up];
			if (!before(id, other)) {
				break;
			}
			heap[index] = other;
			heapIndex[other] = index;
			index = up;
		}
		heap[index] = id;
		heapIndex[id] = index;
	}

	private void siftDown(int index) {
		int id = heap[index];
		while (true) {
			int child = 2 * index + 1;
			if (child >= heapSize) {
				break;
			}
			if (child + 1 < heapSize && before(heap[child + 1], heap[child])) {
				child++;
			}
			int other = heap[child];
			if (!before(other, id)) {
				break;
			}
			heap[index] = other;
			heapIndex[other] = index;
			index = child;
		}
		heap[index] = id;
		heapIndex[id] = index;
	}

	/** An open-addressing {@code long -> int} map, ids only (never removes). */
	private static final class IdTable {
		private long[] keys = new long[512];
		private int[] values = filled(512);
		private int size;

		private static int[] filled(int length) {
			var values = new int[length];
			Arrays.fill(values, -1);
			return values;
		}

		private static int slot(long key, int mask) {
			long hash = key * 0x9E3779B97F4A7C15L;
			return (int) (hash ^ (hash >>> 32)) & mask;
		}

		int get(long key) {
			int mask = keys.length - 1;
			for (int i = slot(key, mask);; i = (i + 1) & mask) {
				int value = values[i];
				if (value < 0 || keys[i] == key) {
					return value;
				}
			}
		}

		void put(long key, int value) {
			if (2 * (size + 1) > keys.length) {
				rehash();
			}
			int mask = keys.length - 1;
			int i = slot(key, mask);
			while (values[i] >= 0) {
				i = (i + 1) & mask;
			}
			keys[i] = key;
			values[i] = value;
			size++;
		}

		private void rehash() {
			var oldKeys = keys;
			var oldValues = values;
			keys = new long[oldKeys.length * 2];
			values = filled(oldKeys.length * 2);
			int mask = keys.length - 1;
			for (int j = 0; j < oldKeys.length; j++) {
				if (oldValues[j] >= 0) {
					int i = slot(oldKeys[j], mask);
					while (values[i] >= 0) {
						i = (i + 1) & mask;
					}
					keys[i] = oldKeys[j];
					values[i] = oldValues[j];
				}
			}
		}
	}
}
