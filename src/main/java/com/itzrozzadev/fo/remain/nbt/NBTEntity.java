package com.itzrozzadev.fo.remain.nbt;

import com.itzrozzadev.fo.MinecraftVersion;
import com.itzrozzadev.fo.exception.FoException;
import org.bukkit.entity.Entity;

/**
 * NBT class to access vanilla tags from Entities. Entities don't support custom
 * tags. Use the NBTInjector for custom tags. Changes will be instantly applied
 * to the Entity, use the merge method to do many things at once.
 *
 * @author tr7zw
 */
public class NBTEntity extends NBTCompound {

	private final Entity ent;

	/**
	 * @param entity Any valid Bukkit Entity
	 */
	public NBTEntity(final Entity entity) {
		super(null, null);
		if (entity == null) {
			throw new NullPointerException("Entity can't be null!");
		}
		this.ent = entity;
	}

	@Override
	public Object getCompound() {
		return NBTReflectionUtil.getEntityNBTTagCompound(NBTReflectionUtil.getNMSEntity(this.ent));
	}

	@Override
	protected void setCompound(final Object compound) {
		NBTReflectionUtil.setEntityNBTTag(compound, NBTReflectionUtil.getNMSEntity(this.ent));
	}

	/**
	 * Gets the NBTCompound used by spigots PersistentDataAPI. This method is only
	 * available for 1.14+!
	 *
	 * @return NBTCompound containing the data of the PersistentDataAPI
	 */
	public NBTCompound getPersistentDataContainer() {

		if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_14))
			throw new FoException("getPersistentDataContainer requires MC 1.14 or newer");

		return new NBTPersistentDataContainer(this.ent.getPersistentDataContainer());
	}

}
