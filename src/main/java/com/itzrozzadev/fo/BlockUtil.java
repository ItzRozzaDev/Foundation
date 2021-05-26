package com.itzrozzadev.fo;

import com.google.common.collect.Sets;
import com.itzrozzadev.fo.MinecraftVersion.V;
import com.itzrozzadev.fo.remain.CompMaterial;
import com.itzrozzadev.fo.remain.Remain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Utility class for block manipulation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockUtil {

	/**
	 * Matches all DOUBLE or STEP block names
	 */
	private static final Pattern SLAB_PATTERN = Pattern.compile("(?!DOUBLE).*STEP");

	/**
	 * The block faces used while searching for all parts of the given
	 * tree upwards
	 */
	private static final BlockFace[] TREE_TRUNK_FACES = {BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH};

	/**
	 * A list of safe blocks upon which a tree naturally grows
	 */
	private final static Set<String> TREE_GROUND_BLOCKS = Sets.newHashSet("GRASS_BLOCK", "COARSE_DIRT", "DIRT", "MYCELIUM", "PODZOL");

	/**
	 * The vertical gaps when creating locations for a bounding box,
	 * see {@link #getBoundingBox(Location, Location)}
	 */
	public static double BOUNDING_VERTICAL_GAP = 1;

	/**
	 * The horizontal gaps when creating locations for a bounding box,
	 * see {@link #getBoundingBox(Location, Location)}
	 */
	public static double BOUNDING_HORIZONTAL_GAP = 1;

	// ------------------------------------------------------------------------------------------------------------
	// Cuboid region manipulation
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Returns true if the given location is within the two vector cuboid bounds
	 *
	 * @param location  - Location being
	 * @param primary   - Primary location of the cuboid
	 * @param secondary - Secondary location of the cuboid
	 * @return Is the given location is within cuboid of the primary and secondary locations
	 */
	public static boolean isWithinCuboid(final Location location, final Location primary, final Location secondary) {
		final double locX = location.getX();
		final double locY = location.getY();
		final double locZ = location.getZ();

		final int x = primary.getBlockX();
		final int y = primary.getBlockY();
		final int z = primary.getBlockZ();

		final int x1 = secondary.getBlockX();
		final int y1 = secondary.getBlockY();
		final int z1 = secondary.getBlockZ();

		if (locX >= x && locX <= x1 || locX <= x && locX >= x1)
			if (locZ >= z && locZ <= z1 || locZ <= z && locZ >= z1)
				return locY >= y && locY <= y1 || locY <= y && locY >= y1;

		return false;
	}

	/**
	 * Returns locations representing the bounding box of a cuboid region
	 *
	 * @param primary   - Primary location of bounding box
	 * @param secondary - Secondary location of bounding box
	 * @return - Locations of bounding box with in the specified points
	 */
	public static Set<Location> getBoundingBox(final Location primary, final Location secondary) {
		final List<VectorHelper> shape = new ArrayList<>();

		final VectorHelper min = getMinimumPoint(primary, secondary);
		final VectorHelper max = getMaximumPoint(primary, secondary).add(1, 0, 1);

		final int height = getHeight(primary, secondary);

		final List<VectorHelper> bottomCorners = new ArrayList<>();

		bottomCorners.add(new VectorHelper(min.getX(), min.getY(), min.getZ()));
		bottomCorners.add(new VectorHelper(max.getX(), min.getY(), min.getZ()));
		bottomCorners.add(new VectorHelper(max.getX(), min.getY(), max.getZ()));
		bottomCorners.add(new VectorHelper(min.getX(), min.getY(), max.getZ()));

		for (int i = 0; i < bottomCorners.size(); i++) {
			final VectorHelper p1 = bottomCorners.get(i);
			final VectorHelper p2 = i + 1 < bottomCorners.size() ? bottomCorners.get(i + 1) : bottomCorners.get(0);
			final VectorHelper p3 = p1.add(0, height, 0);
			final VectorHelper p4 = p2.add(0, height, 0);
			shape.addAll(plotLine(p1, p2));
			shape.addAll(plotLine(p3, p4));
			shape.addAll(plotLine(p1, p3));

			for (double offset = BOUNDING_VERTICAL_GAP; offset < height; offset += BOUNDING_VERTICAL_GAP) {
				final VectorHelper p5 = p1.add(0.0D, offset, 0.0D);
				final VectorHelper p6 = p2.add(0.0D, offset, 0.0D);
				shape.addAll(plotLine(p5, p6));
			}
		}

		final Set<Location> locations = new HashSet<>();

		for (final VectorHelper vector : shape)
			locations.add(new Location(primary.getWorld(), vector.getX(), vector.getY(), vector.getZ()));

		return locations;
	}

	private static List<VectorHelper> plotLine(final VectorHelper p1, final VectorHelper p2) {
		final List<VectorHelper> ShapeVectors = new ArrayList<>();

		final int points = (int) (p1.distance(p2) / BOUNDING_HORIZONTAL_GAP) + 1;
		final double length = p1.distance(p2);
		final double gap = length / (points - 1);

		final VectorHelper gapShapeVector = p2.subtract(p1).normalize().multiply(gap);

		for (int i = 0; i < points; i++) {
			final VectorHelper currentPoint = p1.add(gapShapeVector.multiply(i));

			ShapeVectors.add(currentPoint);
		}

		return ShapeVectors;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Spherical manipulation
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Get all locations within the given 3D spherical radius, hollow or not
	 *
	 * @param location - Location of center of sphere
	 * @param radius   - Radius of sphere
	 * @param hollow   - Is sphere hollow
	 * @return - Returns the sphere
	 */
	public static Set<Location> getSphere(final Location location, final int radius, final boolean hollow) {
		final Set<Location> blocks = new HashSet<>();
		final World world = location.getWorld();
		final int X = location.getBlockX();
		final int Y = location.getBlockY();
		final int Z = location.getBlockZ();
		final int radiusSquared = radius * radius;

		if (hollow) {
			for (int x = X - radius; x <= X + radius; x++)
				for (int y = Y - radius; y <= Y + radius; y++)
					for (int z = Z - radius; z <= Z + radius; z++)
						if ((X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z) <= radiusSquared)
							blocks.add(new Location(world, x, y, z));

			return makeHollow(blocks, true);
		}

		for (int x = X - radius; x <= X + radius; x++)
			for (int y = Y - radius; y <= Y + radius; y++)
				for (int z = Z - radius; z <= Z + radius; z++)
					if ((X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z) <= radiusSquared)
						blocks.add(new Location(world, x, y, z));

		return blocks;
	}

	/**
	 * Get all locations within the given 2D circle radius, hollow or full circle
	 *
	 * @param location - Location of center of circle
	 * @param radius   - Radius of circle
	 * @param hollow   - Is circle hollow
	 * @return - Returns the circle
	 */
	public static Set<Location> getCircle(final Location location, final int radius, final boolean hollow) {
		final Set<Location> blocks = new HashSet<>();
		final World world = location.getWorld();

		final int initialX = location.getBlockX();
		final int initialY = location.getBlockY();
		final int initialZ = location.getBlockZ();
		final int radiusSquared = radius * radius;

		if (hollow) {
			for (int x = initialX - radius; x <= initialX + radius; x++)
				for (int z = initialZ - radius; z <= initialZ + radius; z++)
					if ((initialX - x) * (initialX - x) + (initialZ - z) * (initialZ - z) <= radiusSquared)
						blocks.add(new Location(world, x, initialY, z));

			return makeHollow(blocks, false);
		}

		for (int x = initialX - radius; x <= initialX + radius; x++)
			for (int z = initialZ - radius; z <= initialZ + radius; z++)
				if ((initialX - x) * (initialX - x) + (initialZ - z) * (initialZ - z) <= radiusSquared)
					blocks.add(new Location(world, x, initialY, z));

		return blocks;
	}

	/**
	 * Creates a new list of outer location points from all given points - making it hollow
	 *
	 * @param locations - Locations of blocks being hollowed
	 * @param sphere    - If its hollow
	 * @return - Hollowed locations
	 */
	public static Set<Location> makeHollow(final Set<Location> locations, final boolean sphere) {
		final Set<Location> edge = new HashSet<>();

		if (!sphere) {
			for (final Location location : locations) {
				final World world = location.getWorld();
				final int x = location.getBlockX();
				final int y = location.getBlockY();
				final int z = location.getBlockZ();

				final Location front = new Location(world, x + 1, y, z);
				final Location back = new Location(world, x - 1, y, z);
				final Location left = new Location(world, x, y, z + 1);
				final Location right = new Location(world, x, y, z - 1);

				if (!(locations.contains(front) && locations.contains(back) && locations.contains(left) && locations.contains(right)))
					edge.add(location);

			}
			return edge;
		}

		for (final Location location : locations) {
			final World world = location.getWorld();

			final int x = location.getBlockX();
			final int y = location.getBlockY();
			final int z = location.getBlockZ();

			final Location front = new Location(world, x + 1, y, z);
			final Location back = new Location(world, x - 1, y, z);
			final Location left = new Location(world, x, y, z + 1);
			final Location right = new Location(world, x, y, z - 1);
			final Location top = new Location(world, x, y + 1, z);
			final Location bottom = new Location(world, x, y - 1, z);

			if (!(locations.contains(front) && locations.contains(back) && locations.contains(left) && locations.contains(right) && locations.contains(top) && locations.contains(bottom)))
				edge.add(location);
		}

		return edge;

	}

	// ------------------------------------------------------------------------------------------------------------
	// Getting blocks within a cuboid
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Returns all blocks within the two cuboid bounds
	 *
	 * @param primary   - Primary point
	 * @param secondary - Secondary point
	 * @return - All blocks between the given points
	 */
	public static List<Block> getBlocks(final Location primary, final Location secondary) {
		Valid.checkNotNull(primary, "Primary region point must be set!");
		Valid.checkNotNull(secondary, "Secondary region point must be set!");

		final List<Block> blocks = new ArrayList<>();

		final int topBlockX = Math.max(primary.getBlockX(), secondary.getBlockX());
		final int bottomBlockX = Math.min(primary.getBlockX(), secondary.getBlockX());

		final int topBlockY = Math.max(primary.getBlockY(), secondary.getBlockY());
		final int bottomBlockY = Math.min(primary.getBlockY(), secondary.getBlockY());

		final int topBlockZ = Math.max(primary.getBlockZ(), secondary.getBlockZ());
		final int bottomBlockZ = Math.min(primary.getBlockZ(), secondary.getBlockZ());

		for (int x = bottomBlockX; x <= topBlockX; x++)
			for (int z = bottomBlockZ; z <= topBlockZ; z++)
				for (int y = bottomBlockY; y <= topBlockY; y++) {
					final Block block = primary.getWorld().getBlockAt(x, y, z);

					if (block != null)
						blocks.add(block);
				}

		return blocks;
	}

	/**
	 * Get all the blocks in a specific area centered around the Location passed in
	 *
	 * @param location - Center of the search area
	 * @param height   - how many blocks up to check
	 * @param radius   - of the search (cubic search radius)
	 * @return - all the Block with the given Type in the specified radius
	 */
	public static List<Block> getBlocks(final Location location, final int height, final int radius) {
		final List<Block> blocks = new ArrayList<>();

		for (int y = 0; y < height; y++)
			for (int x = -radius; x <= radius; x++)
				for (int z = -radius; z <= radius; z++) {
					final Block checkBlock = location.getBlock().getRelative(x, y, z);

					if (checkBlock != null && checkBlock.getType() != Material.AIR)
						blocks.add(checkBlock);
				}
		return blocks;
	}

	/**
	 * Return chunks around the given location
	 *
	 * @param location - Center location
	 * @param radius   - Radius of the search area for chunk
	 * @return - Chunks within the given radius
	 */
	public static List<Chunk> getChunks(final Location location, final int radius) {
		final HashSet<Chunk> addedChunks = new HashSet<>();
		final World world = location.getWorld();

		final int chunkX = location.getBlockX() >> 4;
		final int chunkZ = location.getBlockZ() >> 4;

		for (int x = chunkX - radius; x <= chunkX + radius; ++x)
			for (int z = chunkZ - radius; z <= chunkZ + radius; ++z)
				if (world.isChunkLoaded(x, z))
					addedChunks.add(world.getChunkAt(x, z));

		return new ArrayList<>(addedChunks);
	}

	/**
	 * Return all leaves/logs upwards connected to that given tree block
	 * Parts are sorted according to their Y coordinate from lowest to highest
	 *
	 * @param treeBase - Base of the tree
	 * @return - Parts of the tree
	 */
	public static List<Block> getTreePartsUp(final Block treeBase) {
		final Material baseMaterial = treeBase.getState().getType();

		final String logType = MinecraftVersion.atLeast(V.v1_13) ? baseMaterial.toString() : "LOG";
		final String leaveType = MinecraftVersion.atLeast(V.v1_13) ? logType.replace("_LOG", "") + "_LEAVES" : "LEAVES";

		final Set<Block> treeParts = new HashSet<>();
		final Set<Block> toSearch = new HashSet<>();
		final Set<Block> searched = new HashSet<>();

		toSearch.add(treeBase.getRelative(BlockFace.UP));
		searched.add(treeBase);

		int cycle;

		for (cycle = 0; cycle < 1000 && !toSearch.isEmpty(); cycle++) {
			final Block block = toSearch.iterator().next();

			toSearch.remove(block);
			searched.add(block);

			if (block.getType().toString().equals(logType) || block.getType().toString().equals(leaveType)) {
				treeParts.add(block);

				for (final BlockFace face : TREE_TRUNK_FACES) {
					final Block relative = block.getRelative(face);

					if (!searched.contains(relative))
						toSearch.add(relative);

				}

			} else if (!block.getType().isTransparent())
				return new ArrayList<>();
		}

		return new ArrayList<>(treeParts);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Block type checkers
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Returns true whether the given block is a "LOG" type and then perform a search
	 * down to the bottom most connected block to find if that stands onto {@link #TREE_GROUND_BLOCKS}
	 *
	 * @param treeBaseBlock - Block being check if its a treeBase
	 * @return if the bottom most connected block to the given block stays on {@link #TREE_GROUND_BLOCKS}
	 */
	public static boolean isLogOnGround(Block treeBaseBlock) {

		// Reach for the bottom most tree-like block
		while (CompMaterial.isLog(treeBaseBlock.getType()))
			treeBaseBlock = treeBaseBlock.getRelative(BlockFace.DOWN);

		return TREE_GROUND_BLOCKS.contains(CompMaterial.fromMaterial(treeBaseBlock.getType()).toString());
	}

	/**
	 * Will a falling block which lands on this Material break and drop to the
	 * ground?
	 *
	 * @param material - Being checked
	 * @return - If a falling block break
	 */
	public static boolean isBreakingFallingBlock(final Material material) {
		return material.isTransparent() &&
				material != CompMaterial.NETHER_PORTAL.getMaterial() &&
				material != CompMaterial.END_PORTAL.getMaterial() ||
				material == CompMaterial.COBWEB.getMaterial() ||
				material == Material.DAYLIGHT_DETECTOR ||
				CompMaterial.isTrapDoor(material) ||
				material == CompMaterial.SIGN.getMaterial() ||
				CompMaterial.isWallSign(material) ||
				// Match all slabs besides double slab
				SLAB_PATTERN.matcher(material.name()).matches();
	}

	/**
	 * Return true when the given material is a tool
	 *
	 * @param material - Being checked
	 * @return - If the is a tool
	 */
	public static boolean isTool(final Material material) {
		return material.name().endsWith("AXE") // axe & pickaxe
				|| material.name().endsWith("SPADE")
				|| material.name().endsWith("SWORD")
				|| material.name().endsWith("HOE")
				|| material.name().endsWith("BUCKET") // water, milk, lava,..
				|| material == CompMaterial.BOW.getMaterial()
				|| material == CompMaterial.FISHING_ROD.getMaterial()
				|| material == CompMaterial.CLOCK.getMaterial()
				|| material == CompMaterial.COMPASS.getMaterial()
				|| material == CompMaterial.FLINT_AND_STEEL.getMaterial();
	}

	/**
	 * Return true if the material is an armor
	 *
	 * @param material - Being checked
	 * @return - If the is an armour
	 */
	public static boolean isArmor(final Material material) {
		return material.name().endsWith("HELMET")
				|| material.name().endsWith("CHESTPLATE")
				|| material.name().endsWith("LEGGINGS")
				|| material.name().endsWith("BOOTS");
	}

	/**
	 * Returns true if block is safe to select
	 *
	 * @param material - The material being checked
	 * @return - If block is safe to select
	 */
	public static boolean isForBlockSelection(final Material material) {
		if (!material.isBlock() || material == Material.AIR)
			return false;

		try {
			if (material.isInteractable()) // Ignore chests etc.
				return false;
		} catch (final Throwable ignored) {
		}

		try {
			if (material.hasGravity()) // Ignore falling blocks
				return false;
		} catch (final Throwable ignored) {
		}

		return material.isSolid();
	}

	// ------------------------------------------------------------------------------------------------------------
	// Finding blocks and locations
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Scans the location from top to bottom to find the highest Y coordinate that is not air and not snow.
	 * This will return the free coordinate above the snow layer.
	 *
	 * @param location - Location being checked for highest block with no snow
	 * @return - The y coordinate, or -1 if not found
	 */
	public static int findHighestBlockNoSnow(final Location location) {
		return findHighestBlockNoSnow(location.getWorld(), location.getBlockX(), location.getBlockZ());
	}

	/**
	 * Scan the location from top to bottom to find the highest Y coordinate that is not air and not snow.
	 * This will return the free coordinate above the snow layer.
	 *
	 * @param world
	 * @param x
	 * @param z
	 * @return the y coordinate, or -1 if not found
	 */
	public static int findHighestBlockNoSnow(final World world, final int x, final int z) {
		for (int y = world.getMaxHeight(); y > 0; y--) {
			final Block block = world.getBlockAt(x, y, z);

			if (block != null && !CompMaterial.isAir(block) && block.getType() != CompMaterial.SNOW.getMaterial())
				return y + 1;
		}

		return -1;
	}

	/**
	 * Scans the location from top to bottom to find the highest Y non-air coordinate that matches
	 * the given predicate.
	 *
	 * @param location  - Location being checked
	 * @param predicate - Predicate involved
	 * @return the y coordinate, or -1 if not found
	 */
	public static int findHighestBlock(final Location location, final Predicate<Material> predicate) {
		return findHighestBlock(location.getWorld(), location.getBlockX(), location.getBlockZ(), predicate);
	}

	/**
	 * Scans the location from top to bottom to find the highest Y non-air coordinate that matches
	 * the given predicate.
	 *
	 * @param world     - World involved
	 * @param x         - x involved
	 * @param z         - z involved
	 * @param predicate - Predicate involved
	 * @return the y coordinate, or -1 if not found
	 */
	public static int findHighestBlock(final World world, final int x, final int z, final Predicate<Material> predicate) {
		for (int y = world.getMaxHeight(); y > 0; y--) {
			final Block block = world.getBlockAt(x, y, z);

			if (block != null && !CompMaterial.isAir(block) && predicate.test(block.getType()))
				return y + 1;
		}

		return -1;

	}

	/**
	 * Returns the closest location to the given locations
	 *
	 * @param location  - Location involved
	 * @param locations - Locations being checked to see which one is closest to the given locatiom
	 * @return - Closest location
	 */
	public static Location findClosestLocation(final Location location, List<Location> locations) {
		locations = new ArrayList<>(locations);

		locations.sort(Comparator.comparingDouble(f -> f.distance(location)));
		return locations.get(0);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Shooting blocks
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Shoots the given block to the sky with the given velocity. The shot block is set then to air.
	 *
	 * @param block    - Being shot
	 * @param velocity - Velocity of block
	 * @return Falling block
	 */
	public static FallingBlock shootBlock(final Block block, final Vector velocity) {
		return shootBlock(block, velocity, 0D);
	}

	/**
	 * Shoots the given block to the sky with the given velocity
	 * but also has a chance to set the shot block on fire. The shot block is set then to air.
	 *
	 * @param block            - Being shot
	 * @param velocity         - Velocity of block
	 * @param burnOnFallChance - Chance (0.0-1.0f) of setting the shot block on fire
	 * @return Falling block
	 */
	public static FallingBlock shootBlock(final Block block, final Vector velocity, final double burnOnFallChance) {
		if (!canShootBlock(block))
			return null;

		final FallingBlock falling = Remain.spawnFallingBlock(block.getLocation(), block.getType());

		{ // Set velocity to reflect the given velocity but change a bit for more realism
			final double x = MathUtil.range(velocity.getX(), -2, 2) * 0.5D;
			final double y = Math.random();
			final double z = MathUtil.range(velocity.getZ(), -2, 2) * 0.5D;

			falling.setVelocity(new Vector(x, y, z));
		}

		if (RandomUtil.chanceD(burnOnFallChance) && block.getType().isBurnable())
			scheduleBurnOnFall(falling);

		// Prevent drop
		falling.setDropItem(false);

		// Remove the block
		block.setType(Material.AIR);

		return falling;
	}

	/**
	 * Return the allowed material types to shoot this block
	 *
	 * @param block - Block being checked
	 * @return - If the block can be shot
	 */
	private static boolean canShootBlock(final Block block) {
		final Material material = block.getType();

		return !CompMaterial.isAir(material) && (material.toString().contains("STEP") || material.toString().contains("SLAB") || BlockUtil.isForBlockSelection(material));
	}

	/**
	 * Schedule to set the flying block on fire upon impact
	 *
	 * @param block - Block being set to fire on impact
	 */
	private static void scheduleBurnOnFall(final FallingBlock block) {
		EntityUtil.trackFalling(block, () -> {
			final Block upperBlock = block.getLocation().getBlock().getRelative(BlockFace.UP);

			if (upperBlock.getType() == Material.AIR)
				upperBlock.setType(Material.FIRE);
		});
	}

	// ------------------------------------------------------------------------------------------------------------
	// Helper classes
	// ------------------------------------------------------------------------------------------------------------

	private static VectorHelper getMinimumPoint(final Location pos1, final Location pos2) {
		return new VectorHelper(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
	}

	private static VectorHelper getMaximumPoint(final Location pos1, final Location pos2) {
		return new VectorHelper(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
	}

	private static int getHeight(final Location pos1, final Location pos2) {
		final VectorHelper min = getMinimumPoint(pos1, pos2);
		final VectorHelper max = getMaximumPoint(pos1, pos2);

		return (int) (max.getY() - min.getY() + 1.0D);
	}

	@RequiredArgsConstructor
	private final static class VectorHelper {

		@Getter
		protected final double x, y, z;

		public VectorHelper add(final VectorHelper other) {
			return add(other.x, other.y, other.z);
		}

		public VectorHelper add(final double x, final double y, final double z) {
			return new VectorHelper(this.x + x, this.y + y, this.z + z);
		}

		public VectorHelper subtract(final VectorHelper other) {
			return subtract(other.x, other.y, other.z);
		}

		public VectorHelper subtract(final double x, final double y, final double z) {
			return new VectorHelper(this.x - x, this.y - y, this.z - z);
		}

		public VectorHelper multiply(final double n) {
			return new VectorHelper(this.x * n, this.y * n, this.z * n);
		}

		public VectorHelper divide(final double n) {
			return new VectorHelper(x / n, y / n, z / n);
		}

		public double length() {
			return Math.sqrt(x * x + y * y + z * z);
		}

		public double distance(final VectorHelper other) {
			return Math.sqrt(Math.pow(other.x - x, 2) +
					Math.pow(other.y - y, 2) +
					Math.pow(other.z - z, 2));
		}

		public VectorHelper normalize() {
			return divide(length());
		}

		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof VectorHelper))
				return false;

			final VectorHelper other = (VectorHelper) obj;
			return other.x == this.x && other.y == this.y && other.z == this.z;
		}

		@Override
		public String toString() {
			return "(" + x + ", " + y + ", " + z + ")";
		}
	}
}