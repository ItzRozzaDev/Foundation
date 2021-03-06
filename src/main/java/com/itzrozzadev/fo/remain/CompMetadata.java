package com.itzrozzadev.fo.remain;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.MinecraftVersion;
import com.itzrozzadev.fo.MinecraftVersion.V;
import com.itzrozzadev.fo.SerializeUtil;
import com.itzrozzadev.fo.Valid;
import com.itzrozzadev.fo.collection.SerializedMap;
import com.itzrozzadev.fo.collection.StrictMap;
import com.itzrozzadev.fo.constants.FoConstants;
import com.itzrozzadev.fo.model.ConfigSerializable;
import com.itzrozzadev.fo.plugin.SimplePlugin;
import com.itzrozzadev.fo.remain.nbt.NBTCompound;
import com.itzrozzadev.fo.remain.nbt.NBTItem;
import com.itzrozzadev.fo.settings.YamlSectionConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for persistent metadata manipulation
 * <p>
 * We apply scoreboard tags to ensure permanent metadata storage
 * if supported, otherwise it is lost on reload
 */
public final class CompMetadata {

	/**
	 * The tag delimiter
	 */
	private final static String DELIMITER = "%-%";
	private final static String METADATA_MENU = "MENU";

	// Static access
	private CompMetadata() {
	}

	// ----------------------------------------------------------------------------------------
	// Setting metadata
	// ----------------------------------------------------------------------------------------

	/**
	 * A shortcut for setting a tag with key-value pair on an item
	 *
	 * @param item
	 * @param key
	 * @param value
	 * @return
	 */
	public static ItemStack setMetadataItem(final ItemStack item, final String key, final String value) {
		Valid.checkNotNull(item, "Setting NBT tag got null item");

		final NBTItem nbt = new NBTItem(item);
		final NBTCompound tag = nbt.addCompound(FoConstants.NBT.TAG);

		tag.setString(key, value);
		return nbt.getItem();
	}

	public static void setMetadataUpdate(final ItemStack item, final String key, final String value) {
		Valid.checkNotNull(item, "Setting NBT tag got null item");

		final NBTItem nbt = new NBTItem(item);
		final NBTCompound tag = nbt.addCompound(FoConstants.NBT.TAG);

		tag.setString(key, value);
		item.setItemMeta(nbt.getItem().getItemMeta());
	}

	private static void removeMetadataUpdate(final ItemStack item, final String key) {
		Valid.checkNotNull(item, "Removing NBT tag got null item");

		if (hasMetadata(item, key)) {
			final NBTItem nbt = new NBTItem(item);
			final NBTCompound tag = nbt.addCompound(FoConstants.NBT.TAG);
			tag.removeKey(key);
			item.setItemMeta(nbt.getItem().getItemMeta());
		}
	}

	public static ItemStack makeMenuItem(final ItemStack item) {
		return setMetadataItem(item, METADATA_MENU, METADATA_MENU);
	}

	public static boolean isItemMenu(final ItemStack itemStack) {
		return hasMetadata(itemStack, METADATA_MENU);
	}


	/**
	 * Attempts to set a persistent metadata for entity
	 *
	 * @param entity
	 * @param tag
	 */
	public static void setMetadata(final Entity entity, final String tag) {
		setMetadata(entity, tag, tag);
	}

	/**
	 * Attempts to set a persistent metadata tag with value for entity
	 *
	 * @param entity
	 * @param key
	 * @param value
	 */
	public static void setMetadata(final Entity entity, final String key, final String value) {
		Valid.checkNotNull(entity);

		final String tag = format(key, value);

		if (Remain.hasScoreboardTags()) {
			if (!entity.getScoreboardTags().contains(tag))
				entity.addScoreboardTag(tag);

		} else {
			entity.setMetadata(key, new FixedMetadataValue(SimplePlugin.getInstance(), value));

			MetadataFile.getInstance().addMetadata(entity, key, value);
		}
	}

	// Format the syntax of stored tags
	private static String format(final String key, final String value) {
		return SimplePlugin.getNamed() + DELIMITER + key + DELIMITER + value;
	}

	/**
	 * Sets persistent tile entity metadata
	 *
	 * @param tileEntity
	 * @param key
	 * @param value
	 */
	public static void setMetadata(final BlockState tileEntity, final String key, final String value) {
		Valid.checkNotNull(tileEntity);
		Valid.checkNotNull(key);
		Valid.checkNotNull(value);

		if (MinecraftVersion.atLeast(V.v1_14)) {
			Valid.checkBoolean(tileEntity instanceof TileState, "BlockState must be instance of a TileState not " + tileEntity);

			setNamedspaced((TileState) tileEntity, key, value);
			tileEntity.update();

		} else {
			tileEntity.setMetadata(key, new FixedMetadataValue(SimplePlugin.getInstance(), value));
			tileEntity.update();
			MetadataFile.getInstance().addMetadata(tileEntity, key, value);
		}
	}

	private static void setNamedspaced(final TileState tile, final String key, final String value) {
		tile.getPersistentDataContainer().set(new NamespacedKey(SimplePlugin.getInstance(), key), PersistentDataType.STRING, value);
	}

	// ----------------------------------------------------------------------------------------
	// Getting metadata
	// ----------------------------------------------------------------------------------------

	/**
	 * A shortcut from reading a certain key from an item's given compound tag
	 *
	 * @param item
	 * @param key
	 * @return
	 */
	public static String getMetadata(final ItemStack item, final String key) {
		Valid.checkNotNull(item, "Reading NBT tag got null item");

		if (item == null || CompMaterial.isAir(item.getType()))
			return null;

		final String compoundTag = FoConstants.NBT.TAG;
		final NBTItem nbt = new NBTItem(item);

		final String value = nbt.hasKey(compoundTag) ? nbt.getCompound(compoundTag).getString(key) : null;

		return Common.getOrNull(value);
	}

	/**
	 * Attempts to get the entity's metadata, first from scoreboard tag,
	 * second from Bukkit metadata
	 *
	 * @param entity
	 * @param key
	 * @return the tag, or null
	 */
	public static String getMetadata(final Entity entity, final String key) {
		Valid.checkNotNull(entity);

		if (Remain.hasScoreboardTags())
			for (final String line : entity.getScoreboardTags()) {
				final String tag = getTag(line, key);

				if (tag != null && !tag.isEmpty())
					return tag;
			}

		final String value = entity.hasMetadata(key) ? entity.getMetadata(key).get(0).asString() : null;

		return Common.getOrNull(value);
	}

	// Parses the tag and gets its value
	private static String getTag(final String raw, final String key) {
		final String[] parts = raw.split(DELIMITER);

		return parts.length == 3 && parts[0].equals(SimplePlugin.getNamed()) && parts[1].equals(key) ? parts[2] : null;
	}

	/**
	 * Return saved tile entity metadata, or null if none
	 *
	 * @param tileEntity
	 * @param key,       or null if none
	 * @return
	 */
	public static String getMetadata(final BlockState tileEntity, final String key) {
		Valid.checkNotNull(tileEntity);
		Valid.checkNotNull(key);

		if (MinecraftVersion.atLeast(V.v1_14)) {
			Valid.checkBoolean(tileEntity instanceof TileState, "BlockState must be instance of a TileState not " + tileEntity);

			return getNamedspaced((TileState) tileEntity, key);
		}

		final String value = tileEntity.hasMetadata(key) ? tileEntity.getMetadata(key).get(0).asString() : null;

		return Common.getOrNull(value);
	}

	private static String getNamedspaced(final TileState tile, final String key) {
		final String value = tile.getPersistentDataContainer().get(new NamespacedKey(SimplePlugin.getInstance(), key), PersistentDataType.STRING);

		return Common.getOrNull(value);
	}

	// ----------------------------------------------------------------------------------------
	// Checking for metadata
	// ----------------------------------------------------------------------------------------

	/**
	 * Return true if the given itemstack has the given key stored at its compound
	 * tag {@link FoConstants.NBT#TAG}
	 *
	 * @param item
	 * @param key
	 * @return
	 */
	public static boolean hasMetadata(final ItemStack item, final String key) {
		Valid.checkBoolean(MinecraftVersion.atLeast(V.v1_7), "NBT ItemStack tags only support MC 1.7.10+");
		Valid.checkNotNull(item);

		if (CompMaterial.isAir(item.getType()))
			return false;

		final NBTItem nbt = new NBTItem(item);
		final NBTCompound tag = nbt.getCompound(FoConstants.NBT.TAG);

		return tag != null && tag.hasKey(key);
	}

	/**
	 * Returns if the entity has the given tag by key, first checks scoreboard tags,
	 * and then bukkit metadata
	 *
	 * @param entity
	 * @param key
	 * @return
	 */
	public static boolean hasMetadata(final Entity entity, final String key) {
		Valid.checkNotNull(entity);

		if (Remain.hasScoreboardTags())
			for (final String line : entity.getScoreboardTags())
				if (hasTag(line, key))
					return true;

		return entity.hasMetadata(key);
	}

	/**
	 * Return true if the given tile entity block such as {@link CreatureSpawner} has
	 * the given key
	 *
	 * @param tileEntity
	 * @param key
	 * @return
	 */
	public static boolean hasMetadata(final BlockState tileEntity, final String key) {
		Valid.checkNotNull(tileEntity);
		Valid.checkNotNull(key);

		if (MinecraftVersion.atLeast(V.v1_14)) {
			Valid.checkBoolean(tileEntity instanceof TileState, "BlockState must be instance of a TileState not " + tileEntity);

			return hasNamedspaced((TileState) tileEntity, key);
		}

		return tileEntity.hasMetadata(key);
	}

	private static boolean hasNamedspaced(final TileState tile, final String key) {
		return tile.getPersistentDataContainer().has(new NamespacedKey(SimplePlugin.getInstance(), key), PersistentDataType.STRING);
	}

	// Parses the tag and gets its value
	private static boolean hasTag(final String raw, final String tag) {
		final String[] parts = raw.split(DELIMITER);

		return parts.length == 3 && parts[0].equals(SimplePlugin.getNamed()) && parts[1].equals(tag);
	}

	/**
	 * Sets a temporary metadata to entity. This metadata is NOT persistent
	 * and is removed on server stop, restart or reload.
	 * <p>
	 * Use {@link #setMetadata(Entity, String)} to set persistent custom tags for entities.
	 *
	 * @param entity
	 * @param tag
	 */
	public static void setTempMetadata(final Entity entity, final String tag) {
		entity.setMetadata(createTempMetadataKey(tag), new FixedMetadataValue(SimplePlugin.getInstance(), tag));
	}

	/**
	 * Sets a temporary metadata to entity. This metadata is NOT persistent
	 * and is removed on server stop, restart or reload.
	 * <p>
	 * Use {@link #setMetadata(Entity, String)} to set persistent custom tags for entities.
	 *
	 * @param entity
	 * @param tag
	 * @param key
	 */
	public static void setTempMetadata(final Entity entity, final String tag, final Object key) {
		entity.setMetadata(createTempMetadataKey(tag), new FixedMetadataValue(SimplePlugin.getInstance(), key));
	}

	/**
	 * Return entity metadata value or null if has none
	 * <p>
	 * Only usable if you set it using the {@link #setTempMetadata(Entity, String, Object)} with the key parameter
	 * because otherwise the tag is the same as the value we return
	 *
	 * @param entity
	 * @param tag
	 * @return
	 */
	public static MetadataValue getTempMetadata(final Entity entity, final String tag) {
		final String key = createTempMetadataKey(tag);

		return entity.hasMetadata(key) ? entity.getMetadata(key).get(0) : null;
	}

	public static boolean hasTempMetadata(final Entity player, final String tag) {
		return player.hasMetadata(createTempMetadataKey(tag));
	}

	/**
	 * Add temporary metadata to the entity
	 *
	 * @param entity - Entity the metadata is being added to
	 * @param tag    - Metadata tag being added to the entity
	 */
	public static void addTempMetadata(final Entity entity, final String tag) {
		entity.setMetadata(tag, new FixedMetadataValue(SimplePlugin.getInstance(), tag));
	}

	/**
	 * Add temporary metadata to the entity
	 *
	 * @param entity - Entity the metadata is being added to
	 * @param tag    - Metadata tag being added to the entity
	 * @param value  - Value of the metadata
	 */

	public static void addTempMetadata(final Entity entity, final String tag, final String value) {
		entity.setMetadata(tag, new FixedMetadataValue(SimplePlugin.getInstance(), value));
	}

	/**
	 * Remove temporary metadata from the entity
	 *
	 * @param entity - Entity the metadata is being removed from
	 * @param tag    - - Metadata tag being removed from the entity
	 */
	public static void removeMetadata(final Entity entity, final String tag) {
		final String key = createTempMetadataKey(tag);

		if (entity.hasMetadata(key))
			entity.removeMetadata(key, SimplePlugin.getInstance());
	}


	/*
	 * Create a new temporary metadata key
	 */
	private static String createTempMetadataKey(final String tag) {
		return SimplePlugin.getNamed() + "_" + tag;
	}

	/**
	 * Due to lack of persistent metadata implementation until Minecraft 1.14.x,
	 * we simply store them in a file during server restart and then apply
	 * as a temporary metadata for the Bukkit entities.
	 * <p>
	 * internal use only
	 */
	public static final class MetadataFile extends YamlSectionConfig {

		private static final Object LOCK = new Object();

		@Getter
		private static volatile MetadataFile instance = new MetadataFile();

		private final StrictMap<UUID, List<String>> entityMetadataMap = new StrictMap<>();
		private final StrictMap<Location, BlockCache> blockMetadataMap = new StrictMap<>();

		private MetadataFile() {
			super("Metadata");

			loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
		}

		@Override
		protected void onLoadFinish() {
			synchronized (LOCK) {
				loadEntities();
				loadBlockStates();

				save();
			}
		}

		private void loadEntities() {
			synchronized (LOCK) {
				this.entityMetadataMap.clear();

				for (final String uuidName : getMap("Entity").keySet()) {
					final UUID uuid = UUID.fromString(uuidName);

					// Remove broken key
					if (!(getObject("Entity." + uuidName) instanceof List)) {
						setNoSave("Entity." + uuidName, null);

						continue;
					}

					final List<String> metadata = getStringList("Entity." + uuidName);
					final Entity entity = Remain.getEntity(uuid);

					// Check if the entity is still real
					if (!metadata.isEmpty() && entity != null && entity.isValid() && !entity.isDead()) {
						this.entityMetadataMap.put(uuid, metadata);

						applySavedMetadata(metadata, entity);
					}
				}

				save("Entity", this.entityMetadataMap);
			}
		}

		private void loadBlockStates() {
			synchronized (LOCK) {
				this.blockMetadataMap.clear();

				for (final String locationRaw : getMap("Block").keySet()) {
					final Location location = SerializeUtil.deserializeLocation(locationRaw);
					final BlockCache blockCache = get("Block." + locationRaw, BlockCache.class);

					final Block block = location.getBlock();

					// Check if the block remained the same
					if (!CompMaterial.isAir(block) && CompMaterial.fromBlock(block) == blockCache.getType()) {
						this.blockMetadataMap.put(location, blockCache);

						applySavedMetadata(blockCache.getMetadata(), block);
					}
				}

				save("Block", this.blockMetadataMap);
			}
		}

		private void applySavedMetadata(final List<String> metadata, final Metadatable entity) {
			synchronized (LOCK) {
				for (final String metadataLine : metadata) {
					if (metadataLine.isEmpty())
						continue;

					final String[] lines = metadataLine.split(DELIMITER);
					Valid.checkBoolean(lines.length == 3, "Malformed metadata line for " + entity + ". Length 3 != " + lines.length + ". Data: " + metadataLine);

					final String key = lines[1];
					final String value = lines[2];

					entity.setMetadata(key, new FixedMetadataValue(SimplePlugin.getInstance(), value));
				}
			}
		}

		protected void addMetadata(final Entity entity, @NonNull final String key, final String value) {
			synchronized (LOCK) {
				final List<String> metadata = this.entityMetadataMap.getOrPut(entity.getUniqueId(), new ArrayList<>());

				metadata.removeIf(meta -> getTag(meta, key) != null);

				if (value != null && !value.isEmpty()) {
					final String formatted = format(key, value);

					metadata.add(formatted);
				}

				save("Entity", this.entityMetadataMap);
			}
		}

		protected void addMetadata(final BlockState blockState, final String key, final String value) {
			synchronized (LOCK) {
				final BlockCache blockCache = this.blockMetadataMap.getOrPut(blockState.getLocation(), new BlockCache(CompMaterial.fromBlock(blockState.getBlock()), new ArrayList<>()));

				blockCache.getMetadata().removeIf(meta -> getTag(meta, key) != null);

				if (value != null && !value.isEmpty()) {
					final String formatted = format(key, value);

					blockCache.getMetadata().add(formatted);
				}

				{ // Save
					for (final Map.Entry<Location, BlockCache> entry : this.blockMetadataMap.entrySet())
						setNoSave("Block." + SerializeUtil.serializeLoc(entry.getKey()), entry.getValue().serialize());

					save();
				}
			}
		}

		@Getter
		@RequiredArgsConstructor
		public static final class BlockCache implements ConfigSerializable {
			private final CompMaterial type;
			private final List<String> metadata;

			public static BlockCache deserialize(final SerializedMap map) {
				final CompMaterial type = map.getMaterial("Type");
				final List<String> metadata = map.getStringList("Metadata");

				return new BlockCache(type, metadata);
			}

			@Override
			public SerializedMap serialize() {
				final SerializedMap map = new SerializedMap();

				map.put("Type", this.type.toString());
				map.put("Metadata", this.metadata);

				return map;
			}
		}

		public static void onReload() {
			instance = new MetadataFile();
		}
	}
}
