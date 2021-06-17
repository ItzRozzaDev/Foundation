package com.itzrozzadev.fo.remain.nbt;

import com.itzrozzadev.fo.Common;
import org.bukkit.Bukkit;

enum ClassWrapper {
	CRAFT_ITEMSTACK(PackageWrapper.CRAFTBUKKIT, "inventory.CraftItemStack", null, null),
	CRAFT_METAITEM(PackageWrapper.CRAFTBUKKIT, "inventory.CraftMetaItem", null, null),
	CRAFT_ENTITY(PackageWrapper.CRAFTBUKKIT, "entity.CraftEntity", null, null),
	CRAFT_WORLD(PackageWrapper.CRAFTBUKKIT, "CraftWorld", null, null),
	CRAFT_PERSISTENTDATACONTAINER(PackageWrapper.CRAFTBUKKIT, "persistence.CraftPersistentDataContainer",
			MinecraftVersion.MC1_14_R1, null),
	NMS_NBTBASE(PackageWrapper.NMS, "NBTBase", null, null, "net.minecraft.nbt"),
	NMS_NBTTAGSTRING(PackageWrapper.NMS, "NBTTagString", null, null, "net.minecraft.nbt"),
	NMS_NBTTAGINT(PackageWrapper.NMS, "NBTTagInt", null, null, "net.minecraft.nbt"),
	NMS_NBTTAGFLOAT(PackageWrapper.NMS, "NBTTagFloat", null, null, "net.minecraft.nbt"),
	NMS_NBTTAGDOUBLE(PackageWrapper.NMS, "NBTTagDouble", null, null, "net.minecraft.nbt"),
	NMS_NBTTAGLONG(PackageWrapper.NMS, "NBTTagLong", null, null, "net.minecraft.nbt"),
	NMS_ITEMSTACK(PackageWrapper.NMS, "ItemStack", null, null, "net.minecraft.world.item"),
	NMS_NBTTAGCOMPOUND(PackageWrapper.NMS, "NBTTagCompound", null, null, "net.minecraft.nbt"),
	NMS_NBTTAGLIST(PackageWrapper.NMS, "NBTTagList", null, null, "net.minecraft.nbt"),
	NMS_NBTCOMPRESSEDSTREAMTOOLS(PackageWrapper.NMS, "NBTCompressedStreamTools", null, null, "net.minecraft.nbt"),
	NMS_MOJANGSONPARSER(PackageWrapper.NMS, "MojangsonParser", null, null, "net.minecraft.nbt"),
	NMS_TILEENTITY(PackageWrapper.NMS, "TileEntity", null, null, "net.minecraft.world.level.block.entity"),
	NMS_BLOCKPOSITION(PackageWrapper.NMS, "BlockPosition", MinecraftVersion.MC1_8_R3, null, "net.minecraft.core"),
	NMS_WORLDSERVER(PackageWrapper.NMS, "WorldServer", null, null, "net.minecraft.server.level"),
	NMS_MINECRAFTSERVER(PackageWrapper.NMS, "MinecraftServer", null, null, "net.minecraft.server"),
	NMS_WORLD(PackageWrapper.NMS, "World", null, null, "net.minecraft.world.level"),
	NMS_ENTITY(PackageWrapper.NMS, "Entity", null, null, "net.minecraft.world.entity"),
	NMS_ENTITYTYPES(PackageWrapper.NMS, "EntityTypes", null, null, "net.minecraft.world.entity"),
	NMS_REGISTRYSIMPLE(PackageWrapper.NMS, "RegistrySimple", MinecraftVersion.MC1_11_R1, MinecraftVersion.MC1_12_R1),
	NMS_REGISTRYMATERIALS(PackageWrapper.NMS, "RegistryMaterials", null, null, "net.minecraft.core"),
	NMS_IREGISTRY(PackageWrapper.NMS, "IRegistry", null, null, "net.minecraft.core"),
	NMS_MINECRAFTKEY(PackageWrapper.NMS, "MinecraftKey", MinecraftVersion.MC1_8_R3, null, "net.minecraft.resources"),
	NMS_GAMEPROFILESERIALIZER(PackageWrapper.NMS, "GameProfileSerializer", null, null, "net.minecraft.nbt"),
	NMS_IBLOCKDATA(PackageWrapper.NMS, "IBlockData", MinecraftVersion.MC1_8_R3, null,
			"net.minecraft.world.level.block.state"),
	GAMEPROFILE(PackageWrapper.NONE, "com.mojang.authlib.GameProfile", MinecraftVersion.MC1_8_R3, null);

	private Class<?> clazz;
	private boolean enabled = false;

	ClassWrapper(final PackageWrapper packageId, final String clazzName, final MinecraftVersion from, final MinecraftVersion to) {
		this(packageId, clazzName, from, to, null);
	}

	ClassWrapper(final PackageWrapper packageId, final String clazzName, final MinecraftVersion from, final MinecraftVersion to,
				 final String mojangMap) {
		if (from != null && MinecraftVersion.getVersion().getVersionId() < from.getVersionId()) {
			return;
		}
		if (to != null && MinecraftVersion.getVersion().getVersionId() > to.getVersionId()) {
			return;
		}
		this.enabled = true;
		try {
			if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_17_R1) && mojangMap != null) {
				this.clazz = Class.forName(mojangMap + "." + clazzName);
			} else if (packageId == PackageWrapper.NONE) {
				this.clazz = Class.forName(clazzName);
			} else {
				final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
				this.clazz = Class.forName(packageId.getUri() + "." + version + "." + clazzName);
			}
		} catch (final Exception ex) {
			Common.error(ex, "[NBTAPI] Error while trying to resolve the class '" + clazzName + "'!");
		}
	}

	/**
	 * @return The wrapped class
	 */
	public Class<?> getClazz() {
		return this.clazz;
	}

	/**
	 * @return Is this class available in this Version
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

}