package com.itzrozzadev.fo.remain.nbt;

import org.bukkit.block.Block;

public class NBTBlock {

	private final Block block;
	private final NBTChunk nbtChunk;

	public NBTBlock(final Block block) {
		this.block = block;
		if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R3)) {
			throw new NbtApiException("NBTBlock is only working for 1.16.4+!");
		}
		this.nbtChunk = new NBTChunk(block.getChunk());
	}

	public NBTCompound getData() {
		return this.nbtChunk.getPersistentDataContainer().getOrCreateCompound("blocks").getOrCreateCompound(this.block.getX() + "_" + this.block.getY() + "_" + this.block.getZ());
	}

}