package com.itzrozzadev.fo.remain.nbt;

import com.itzrozzadev.fo.MinecraftVersion;
import com.itzrozzadev.fo.exception.FoException;
import org.bukkit.Chunk;

public class NBTChunk {

	private final Chunk chunk;

	public NBTChunk(final Chunk chunk) {
		this.chunk = chunk;
	}

	/**
	 * Gets the NBTCompound used by spigots PersistentDataAPI. This method is only
	 * available for 1.16.4+!
	 *
	 * @return NBTCompound containing the data of the PersistentDataAPI
	 */
	public NBTCompound getPersistentDataContainer() {

		if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_16))
			throw new FoException("getPersistentDataContainer requires MC 1.16 or newer");

		return new NBTPersistentDataContainer(this.chunk.getPersistentDataContainer());
	}

}