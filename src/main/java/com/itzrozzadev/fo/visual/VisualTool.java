package com.itzrozzadev.fo.visual;

import com.itzrozzadev.fo.menu.tool.BlockTool;
import com.itzrozzadev.fo.remain.CompMaterial;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

/**
 * A class that can visualize selection of blocks in the arena
 *
 * @param <T>
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class VisualTool extends BlockTool {

	/**
	 * Handle block clicking and automatically refreshes rendering of visualized blocks
	 *
	 * @param player
	 * @param click
	 * @param block
	 */
	@Override
	protected final void onBlockClick(final Player player, final ClickType click, final Block block) {
		// Remove old blocks
		stopVisualizing(player);

		// Call the block handling, probably new blocks will appear
		handleBlockClick(player, click, block);

		// Render the new blocks
		visualize(player);
	}

	/**
	 * Handles block clicking. Any changes here will be reflected automatically in the visualization
	 *
	 * @param player
	 * @param click
	 * @param block
	 */
	protected abstract void handleBlockClick(Player player, ClickType click, Block block);

	@Override
	protected final void onAirClick(final Player player, final ClickType click) {
		// Remove old blocks
		stopVisualizing(player);

		// Call the block handling, probably new blocks will appear
		handleAirClick(player, click);

		// Render the new blocks
		visualize(player);
	}

	/**
	 * Handles air clicking and updates visualization automatically
	 *
	 * @param player
	 * @param click
	 */
	protected void handleAirClick(final Player player, final ClickType click) {
	}

	/**
	 * @see com.itzrozzadev.fo.menu.tool.Tool#onHotbarFocused(org.bukkit.entity.Player)
	 */
	@Override
	protected final void onHotbarFocused(final Player player) {
		visualize(player);
	}

	/**
	 * @see com.itzrozzadev.fo.menu.tool.Tool#onHotbarDefocused(org.bukkit.entity.Player)
	 */
	@Override
	protected final void onHotbarDefocused(final Player player) {
		stopVisualizing(player);
	}

	/**
	 * Return a list of points we should render in this visualization
	 *
	 * @param player
	 * @return
	 */
	protected abstract List<Location> getVisualizedPoints(Player player);

	/**
	 * Return a region that this tool should draw particles around
	 *
	 * @param player
	 * @return
	 */
	protected VisualizedRegion getVisualizedRegion(final Player player) {
		return null;
	}

	/**
	 * Return the name above the glowing block for the given parameters
	 *
	 * @param block
	 * @param player
	 * @return
	 */
	protected abstract String getBlockName(Block block, Player player);

	/**
	 * Return the block mask for the given parameters
	 *
	 * @param block
	 * @param player
	 * @return
	 */
	protected abstract CompMaterial getBlockMask(Block block, Player player);

	/*
	 * Visualize the region and points if exist
	 */
	private void visualize(@NonNull final Player player) {
		final VisualizedRegion region = getVisualizedRegion(player);

		if (region != null && region.isWhole())
			if (!region.canSeeParticles(player))
				region.showParticles(player);

		for (final Location location : getVisualizedPoints(player)) {
			if (location == null)
				continue;

			final Block block = location.getBlock();

			if (!BlockVisualizer.isVisualized(block))
				BlockVisualizer.visualize(block, getBlockMask(block, player), getBlockName(block, player));
		}
	}

	/*
	 * Stop visualizing region and points if they were so before
	 */
	private void stopVisualizing(@NonNull final Player player) {
		final VisualizedRegion region = getVisualizedRegion(player);

		if (region != null && region.canSeeParticles(player))
			region.hideParticles(player);

		for (final Location location : getVisualizedPoints(player)) {
			if (location == null)
				continue;

			final Block block = location.getBlock();

			if (BlockVisualizer.isVisualized(block))
				BlockVisualizer.stopVisualizing(block);
		}
	}
}
