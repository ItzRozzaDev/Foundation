package com.itzrozzadev.fo.visual;

import com.itzrozzadev.fo.BlockUtil;
import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.Valid;
import com.itzrozzadev.fo.collection.SerializedMap;
import com.itzrozzadev.fo.region.Region;
import com.itzrozzadev.fo.remain.CompParticle;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A simply way to visualize two locations in the world
 */
public final class VisualizedRegion extends Region {

	/**
	 * A list of players who can see the particles
	 */
	private final List<Player> viewers = new ArrayList<>();

	/**
	 * The task responsible for sending particles
	 */
	private BukkitTask task;

	/**
	 * The particle that is being sent out
	 */
	@Setter
	private CompParticle particle = CompParticle.VILLAGER_HAPPY;

	/**
	 * Create a new visualizable region
	 *
	 * @param primary
	 * @param secondary
	 */
	public VisualizedRegion(@Nullable final Location primary, @Nullable final Location secondary) {
		super(primary, secondary);
	}

	/**
	 * Create a visualizable region
	 *
	 * @param name
	 * @param primary
	 * @param secondary
	 */
	public VisualizedRegion(@Nullable final String name, final Location primary, @Nullable final Location secondary) {
		super(name, primary, secondary);
	}

	// ------–------–------–------–------–------–------–------–------–------–------–------–
	// Rendering
	// ------–------–------–------–------–------–------–------–------–------–------–------–

	/**
	 * Shows the region to the given player for the given duration,
	 * the hides it
	 *
	 * @param player
	 * @param durationTicks
	 */
	public void showParticles(final Player player, final int durationTicks) {
		showParticles(player);

		Common.runLater(durationTicks, () -> {
			if (canSeeParticles(player))
				hideParticles(player);
		});
	}

	/**
	 * Shows the region to the given player
	 *
	 * @param player
	 */
	public void showParticles(final Player player) {
		Valid.checkBoolean(!canSeeParticles(player), "Player " + player.getName() + " already sees region " + this);
		Valid.checkBoolean(isWhole(), "Cannot show particles of an incomplete region " + this);

		this.viewers.add(player);

		if (this.task == null)
			startVisualizing();
	}

	/**
	 * Hides the region from the given player
	 *
	 * @param player
	 */
	public void hideParticles(final Player player) {
		Valid.checkBoolean(canSeeParticles(player), "Player " + player.getName() + " is not seeing region " + this);

		this.viewers.remove(player);

		if (this.viewers.isEmpty() && this.task != null)
			stopVisualizing();
	}

	/**
	 * Return true if the given player can see the region particles
	 *
	 * @param player
	 * @return
	 */
	public boolean canSeeParticles(final Player player) {
		return this.viewers.contains(player);
	}

	/*
	 * Starts visualizing this region if it is whole
	 */
	private void startVisualizing() {
		Valid.checkBoolean(this.task == null, "Already visualizing region " + this + "!");
		Valid.checkBoolean(isWhole(), "Cannot visualize incomplete region " + this + "!");

		this.task = Common.runTimer(23, new BukkitRunnable() {
			@Override
			public void run() {
				if (VisualizedRegion.this.viewers.isEmpty()) {
					stopVisualizing();

					return;
				}

				final Set<Location> blocks = BlockUtil.getBoundingBox(getPrimary(), getSecondary());

				for (final Location location : blocks)
					for (final Player viewer : VisualizedRegion.this.viewers) {
						final Location viewerLocation = viewer.getLocation();

						if (viewerLocation.getWorld().equals(location.getWorld()) && viewerLocation.distance(location) < 100)
							VisualizedRegion.this.particle.spawnFor(viewer, location);
					}

			}
		});
	}

	/*
	 * Stops the region from being visualized
	 */
	private void stopVisualizing() {
		Valid.checkNotNull(this.task, "Region " + this + " not visualized");

		this.task.cancel();
		this.task = null;

		this.viewers.clear();
	}

	/**
	 * Converts a saved map from your yaml/json file into a region if it contains Primary and Secondary keys
	 *
	 * @param map
	 * @return
	 */
	public static VisualizedRegion deserialize(final SerializedMap map) {
		Valid.checkBoolean(map.containsKey("Primary") && map.containsKey("Secondary"), "The region must have Primary and a Secondary location");

		final String name = map.getString("Name");
		final Location prim = map.getLocation("Primary");
		final Location sec = map.getLocation("Secondary");

		return new VisualizedRegion(name, prim, sec);
	}
}
