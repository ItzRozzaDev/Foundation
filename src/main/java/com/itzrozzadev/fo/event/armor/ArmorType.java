package com.itzrozzadev.fo.event.armor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ArmorType {
	HELMET(5),
	CHESTPLATE(6),
	LEGGINGS(7),
	BOOTS(8);

	private final int slot;

	ArmorType(final int slot) {
		this.slot = slot;
	}

	public static ArmorType matchType(final ItemStack itemStack) {
		if (itemStack == null || itemStack.getType().equals(Material.AIR))
			return null;
		final String type = itemStack.getType().name();
		if (type.endsWith("_HELMET") || type.endsWith("_SKULL"))
			return HELMET;
		if (type.endsWith("_CHESTPLATE"))
			return CHESTPLATE;
		if (type.endsWith("_LEGGINGS"))
			return LEGGINGS;
		if (type.endsWith("_BOOTS"))
			return BOOTS;
		return null;
	}

	public int getSlot() {
		return this.slot;
	}
}
