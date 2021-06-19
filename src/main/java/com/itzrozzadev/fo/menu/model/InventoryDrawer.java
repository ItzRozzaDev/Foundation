package com.itzrozzadev.fo.menu.model;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.MinecraftVersion;
import com.itzrozzadev.fo.PlayerUtil;
import com.itzrozzadev.fo.menu.Menu;
import com.itzrozzadev.fo.remain.CompMaterial;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Represents a way to render the inventory to the player
 * using Bukkit/Spigot native methods.
 * <p>
 * This is also handy if you simply want to show
 * a certain inventory without creating the full menu.
 */

public final class InventoryDrawer {

	/**
	 * The size of the inventory.
	 */
	@Getter
	private final int size;

	/**
	 * The inventory title
	 */
	private String title;

	/**
	 * The items in this inventory
	 */
	private final ItemStack[] content;

	/**
	 * Create a new inventory drawer, see {@link #of(int, String)}
	 *
	 * @param size  the size
	 * @param title the title
	 */
	private InventoryDrawer(final int size, final String title) {
		this.size = size;
		this.title = title;

		this.content = new ItemStack[size];
	}

	/**
	 * Adds the item at the first empty slot starting from the 0 slot
	 * <p>
	 * If the inventory is full, we add it on the last slot replacing existing item
	 *
	 * @param item the item
	 */
	public void pushItem(final ItemStack item) {
		boolean added = false;

		for (int i = 0; i < this.content.length; i++) {
			final ItemStack currentItem = this.content[i];

			if (currentItem == null) {
				this.content[i] = item;
				added = true;

				break;
			}
		}

		if (!added)
			this.content[this.size - 1] = item;
	}

	/**
	 * Is the current slot occupied by a non-null {@link ItemStack}?
	 *
	 * @param slot the slot
	 * @return true if the slot is occupied
	 */
	public boolean isSet(final int slot) {
		return getItem(slot) != null;
	}

	/**
	 * Get an item at the slot, or null if slot overflown or item not set
	 *
	 * @param slot
	 * @return
	 */
	public ItemStack getItem(final int slot) {
		return slot < this.content.length ? this.content[slot] : null;
	}

	/**
	 * Set an item at the certain slot
	 *
	 * @param slot
	 * @param item
	 */
	public void setItem(final int slot, final ItemStack item) {
		this.content[slot] = item;
	}

	/**
	 * Set the full content of this inventory
	 * <p>
	 * If the given content is shorter, all additional inventory slots are replaced with air
	 *
	 * @param newContent the new content
	 */
	public void setContent(final ItemStack[] newContent) {
		for (int i = 0; i < this.content.length; i++)
			this.content[i] = i < newContent.length ? newContent[i] : new ItemStack(CompMaterial.AIR.getMaterial());
	}

	/**
	 * Set the title of this inventory drawer, not updating the inventory if it is being viewed
	 *
	 * @param title
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Display this inventory to the player, closing older inventory if already opened
	 *
	 * @param player
	 */
	public void display(final Player player) {
		final Inventory inv = this.build(player);
		final Menu menu = Menu.getMenu(player);
		if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_17)) {
			if (player.getOpenInventory() != null && menu != null) {
				//Check for a new bigger menu - This will open a new menu instead of setting the contents of the current one
				if (menu.getSize() == getSize()) {
					player.getOpenInventory().getTopInventory().setContents(inv.getContents());
					PlayerUtil.updateInventoryTitle(player, this.title);
					player.updateInventory();
					return;
				}
			}
		}
		player.openInventory(inv);
	}

	/**
	 * Builds the inventory
	 */
	public Inventory build() {
		return this.build(null);
	}

	/**
	 * Builds the inventory for the given holder
	 *
	 * @param holder
	 * @return
	 */
	public Inventory build(@Nullable final InventoryHolder holder) {
		// Automatically append the black color in the menu, can be overriden by colors
		final Inventory inv = Bukkit.createInventory(holder, this.size, Common.colorize("&0" + this.title));
		inv.setContents(this.content);
		return inv;
	}

	/**
	 * Make a new inventory drawer
	 *
	 * @param size  the size
	 * @param title the title, colors will be replaced
	 * @return the inventory drawer
	 */
	public static InventoryDrawer of(final int size, final String title) {
		return new InventoryDrawer(size, title);
	}
}
