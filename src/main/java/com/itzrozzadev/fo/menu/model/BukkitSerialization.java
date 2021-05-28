package com.itzrozzadev.fo.menu.model;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BukkitSerialization {
	/**
	 * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
	 *
	 * @param playerInventory to turn into an array of strings.
	 * @return Array of strings: [ main content, armor content ]
	 * @throws IllegalStateException
	 */
	public static String[] playerInventoryToBase64(final PlayerInventory playerInventory) throws IllegalStateException {
		//get the main content part, this doesn't return the armor
		final String content = inventoryTo64(playerInventory);
		final String armor = armourTo64(playerInventory.getArmorContents());

		return new String[]{content, armor};
	}

	public static String armourTo64(final ItemStack[] armorContents) {
		return itemStackArrayToBase64(armorContents);
	}

	public static String inventoryTo64(final PlayerInventory inventory) {
		return toBase64(inventory);
	}

	public static void toInventory(final Player player, final String contents, final String armour) throws IOException {
		player.getInventory().setContents(itemStackArrayFromBase64(contents));
		player.getInventory().setArmorContents(itemStackArrayFromBase64(armour));
	}


	/**
	 * A method to serialize an {@link ItemStack} array to Base64 String.
	 * Based off of {@link #toBase64(Inventory)}.
	 *
	 * @param items to turn into a Base64 String.
	 * @return Base64 string of the items.
	 * @throws IllegalStateException
	 */
	public static String itemStackArrayToBase64(final ItemStack[] items) throws IllegalStateException {
		try {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			// Write the size of the inventory
			dataOutput.writeInt(items.length);

			// Save every element in the list
			for (final ItemStack item : items) {
				dataOutput.writeObject(item);
			}

			// Serialize that array
			dataOutput.close();
			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch (final Exception e) {
			throw new IllegalStateException("Unable to save item stacks.", e);
		}
	}

	/**
	 * A method to serialize an inventory to Base64 string.
	 * Special thanks to Comphenix in the Bukkit forums or also known
	 * as aadnk on GitHub.
	 *
	 * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
	 *
	 * @param inventory to serialize
	 * @return Base64 string of the provided inventory
	 * @throws IllegalStateException
	 */
	public static String toBase64(final Inventory inventory) throws IllegalStateException {
		try {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			// Write the size of the inventory
			dataOutput.writeInt(inventory.getSize());

			// Save every element in the list
			for (int i = 0; i < inventory.getSize(); i++) {
				dataOutput.writeObject(inventory.getItem(i));
			}

			// Serialize that array
			dataOutput.close();
			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch (final Exception e) {
			throw new IllegalStateException("Unable to save item stacks.", e);
		}
	}

	/**
	 * A method to get an {@link Inventory} from an encoded, Base64, string.
	 * Special thanks to Comphenix in the Bukkit forums or also known
	 * as aadnk on GitHub.
	 *
	 * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
	 *
	 * @param data Base64 string of data containing an inventory.
	 * @return Inventory created from the Base64 string.
	 * @throws IOException
	 */
	public static Inventory fromBase64(final String data) throws IOException {
		try {
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			final Inventory inventory = Bukkit.getServer().createInventory(null, getInventorySize(dataInput.readInt()));

			// Read the serialized inventory
			for (int i = 0; i < inventory.getSize(); i++) {
				inventory.setItem(i, (ItemStack) dataInput.readObject());
			}

			dataInput.close();
			return inventory;
		} catch (final ClassNotFoundException e) {
			throw new IOException("Unable to decode class type.", e);
		}
	}

	/**
	 * Gets an array of ItemStacks from Base64 string.
	 * Base off of {@link #fromBase64(String)}.
	 *
	 * @param data Base64 string to convert to ItemStack array.
	 * @return ItemStack array created from the Base64 string.
	 * @throws IOException
	 */
	public static ItemStack[] itemStackArrayFromBase64(final String data) throws IOException {
		try {
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			final ItemStack[] items = new ItemStack[dataInput.readInt()];

			// Read the serialized inventory
			for (int i = 0; i < items.length; i++) {
				items[i] = (ItemStack) dataInput.readObject();
			}

			dataInput.close();
			return items;
		} catch (final ClassNotFoundException e) {
			throw new IOException("Unable to decode class type.", e);
		}
	}

	private static int getInventorySize(final int max) {
		if (max <= 9) {
			return 9;
		} else if (max <= 18) {
			return 18;
		} else if (max <= 27) {
			return 27;
		} else if (max <= 36) {
			return 36;
		} else if (max <= 45) {
			return 45;
		} else if (max <= 54) {
			return 54;
		} else {
			return 54;
		}
	}
}