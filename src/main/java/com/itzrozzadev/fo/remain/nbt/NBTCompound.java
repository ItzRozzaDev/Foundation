package com.itzrozzadev.fo.remain.nbt;

import org.bukkit.inventory.ItemStack;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Base class representing NMS Compounds. For a standalone implementation check
 * {@link NBTContainer}
 *
 * @author tr7zw
 */
public class NBTCompound {

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = this.readWriteLock.readLock();
	private final Lock writeLock = this.readWriteLock.writeLock();

	private final String compundName;
	private final NBTCompound parent;

	protected NBTCompound(final NBTCompound owner, final String name) {
		this.compundName = name;
		this.parent = owner;
	}

	protected Lock getReadLock() {
		return this.readLock;
	}

	protected Lock getWriteLock() {
		return this.writeLock;
	}

	protected void saveCompound() {
		if (this.parent != null)
			this.parent.saveCompound();
	}

	/**
	 * @return The Compound name
	 */
	public String getName() {
		return this.compundName;
	}

	/**
	 * @return The NMS Compound behind this Object
	 */
	public Object getCompound() {
		return this.parent.getCompound();
	}

	protected void setCompound(final Object compound) {
		this.parent.setCompound(compound);
	}

	/**
	 * @return The parent Compound
	 */
	public NBTCompound getParent() {
		return this.parent;
	}

	/**
	 * Merges all data from comp into this compound. This is done in one action, so
	 * it also works with Tiles/Entities
	 *
	 * @param comp
	 */
	public void mergeCompound(final NBTCompound comp) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.mergeOtherNBTCompound(this, comp);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setString(final String key, final String value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_STRING, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public String getString(final String key) {
		try {
			this.readLock.lock();
			return (String) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_STRING, key);
		} finally {
			this.readLock.unlock();
		}
	}

	protected String getContent(final String key) {
		return NBTReflectionUtil.getContent(this, key);
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setInteger(final String key, final Integer value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_INT, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public Integer getInteger(final String key) {
		try {
			this.readLock.lock();
			return (Integer) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_INT, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setDouble(final String key, final Double value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_DOUBLE, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public Double getDouble(final String key) {
		try {
			this.readLock.lock();
			return (Double) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_DOUBLE, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setByte(final String key, final Byte value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_BYTE, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public Byte getByte(final String key) {
		try {
			this.readLock.lock();
			return (Byte) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BYTE, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setShort(final String key, final Short value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_SHORT, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public Short getShort(final String key) {
		try {
			this.readLock.lock();
			return (Short) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_SHORT, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setLong(final String key, final Long value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_LONG, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public Long getLong(final String key) {
		try {
			this.readLock.lock();
			return (Long) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_LONG, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setFloat(final String key, final Float value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_FLOAT, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public Float getFloat(final String key) {
		try {
			this.readLock.lock();
			return (Float) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_FLOAT, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setByteArray(final String key, final byte[] value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_BYTEARRAY, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public byte[] getByteArray(final String key) {
		try {
			this.readLock.lock();
			return (byte[]) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BYTEARRAY, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setIntArray(final String key, final int[] value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_INTARRAY, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public int[] getIntArray(final String key) {
		try {
			this.readLock.lock();
			return (int[]) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_INTARRAY, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setBoolean(final String key, final Boolean value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_BOOLEAN, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	protected void set(final String key, final Object val) {
		NBTReflectionUtil.set(this, key, val);
		saveCompound();
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public Boolean getBoolean(final String key) {
		try {
			this.readLock.lock();
			return (Boolean) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BOOLEAN, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Uses Gson to store an {@link Serializable} Object
	 *
	 * @param key
	 * @param value
	 */
	public void setObject(final String key, final Object value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setObject(this, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Uses Gson to retrieve a stored Object
	 *
	 * @param key
	 * @param type Class of the Object
	 * @return The created Object or null if empty
	 */
	public <T> T getObject(final String key, final Class<T> type) {
		try {
			this.readLock.lock();
			return NBTReflectionUtil.getObject(this, key, type);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Save an ItemStack as a compound under a given key
	 *
	 * @param key
	 * @param item
	 */
	public void setItemStack(final String key, final ItemStack item) {
		try {
			this.writeLock.lock();
			removeKey(key);
			addCompound(key).mergeCompound(NBTItem.convertItemtoNBT(item));
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Get an ItemStack that was saved at the given key
	 *
	 * @param key
	 * @return
	 */
	public ItemStack getItemStack(final String key) {
		try {
			this.readLock.lock();
			final NBTCompound comp = getCompound(key);
			return NBTItem.convertNBTtoItem(comp);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Setter
	 *
	 * @param key
	 * @param value
	 */
	public void setUUID(final String key, final UUID value) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_UUID, key, value);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Getter
	 *
	 * @param key
	 * @return The stored value or NMS fallback
	 */
	public UUID getUUID(final String key) {
		try {
			this.readLock.lock();
			return (UUID) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_UUID, key);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * @param key
	 * @return True if the key is set
	 */
	public Boolean hasKey(final String key) {
		try {
			this.readLock.lock();
			final Boolean b = (Boolean) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_HAS_KEY, key);
			if (b == null)
				return false;
			return b;
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * @param key Deletes the given Key
	 */
	public void removeKey(final String key) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.remove(this, key);
			saveCompound();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * @return Set of all stored Keys
	 */
	public Set<String> getKeys() {
		try {
			this.readLock.lock();
			return NBTReflectionUtil.getKeys(this);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Creates a subCompound, or returns it if already provided
	 *
	 * @param name Key to use
	 * @return The subCompound Object
	 */
	public NBTCompound addCompound(final String name) {
		try {
			this.writeLock.lock();
			if (getType(name) == NBTType.NBTTagCompound)
				return getCompound(name);
			NBTReflectionUtil.addNBTTagCompound(this, name);
			final NBTCompound comp = getCompound(name);
			if (comp == null)
				throw new NbtApiException("Error while adding Compound, got null!");
			saveCompound();
			return comp;
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * @param name
	 * @return The Compound instance or null
	 */
	public NBTCompound getCompound(final String name) {
		try {
			this.readLock.lock();
			if (getType(name) != NBTType.NBTTagCompound)
				return null;
			final NBTCompound next = new NBTCompound(this, name);
			if (NBTReflectionUtil.valideCompound(next))
				return next;
			return null;
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * The same as addCompound, just with a name that better reflects what it does
	 *
	 * @param name
	 * @return
	 */
	public NBTCompound getOrCreateCompound(final String name) {
		return addCompound(name);
	}

	/**
	 * @param name
	 * @return The retrieved String List
	 */
	public NBTList<String> getStringList(final String name) {
		try {
			this.writeLock.lock();
			final NBTList<String> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagString, String.class);
			saveCompound();
			return list;
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * @param name
	 * @return The retrieved Integer List
	 */
	public NBTList<Integer> getIntegerList(final String name) {
		try {
			this.writeLock.lock();
			final NBTList<Integer> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagInt, Integer.class);
			saveCompound();
			return list;
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * @param name
	 * @return The retrieved Float List
	 */
	public NBTList<Float> getFloatList(final String name) {
		try {
			this.writeLock.lock();
			final NBTList<Float> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagFloat, Float.class);
			saveCompound();
			return list;
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * @param name
	 * @return The retrieved Double List
	 */
	public NBTList<Double> getDoubleList(final String name) {
		try {
			this.writeLock.lock();
			final NBTList<Double> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagDouble, Double.class);
			saveCompound();
			return list;
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * @param name
	 * @return The retrieved Long List
	 */
	public NBTList<Long> getLongList(final String name) {
		try {
			this.writeLock.lock();
			final NBTList<Long> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagLong, Long.class);
			saveCompound();
			return list;
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Returns the type of the list, null if not a list
	 *
	 * @param name
	 * @return
	 */
	public NBTType getListType(final String name) {
		try {
			this.readLock.lock();
			if (getType(name) != NBTType.NBTTagList)
				return null;
			return NBTReflectionUtil.getListType(this, name);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * @param name
	 * @return The retrieved Compound List
	 */
	public NBTCompoundList getCompoundList(final String name) {
		try {
			this.writeLock.lock();
			final NBTCompoundList list = (NBTCompoundList) NBTReflectionUtil.getList(this, name, NBTType.NBTTagCompound,
					NBTListCompound.class);
			saveCompound();
			return list;
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * @param name
	 * @return The type of the given stored key or null
	 */
	public NBTType getType(final String name) {
		try {
			this.readLock.lock();
			if (MinecraftVersion.getVersion() == MinecraftVersion.MC1_7_R4) {
				final Object nbtbase = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET, name);
				if (nbtbase == null)
					return null;
				return NBTType.valueOf((byte) ReflectionMethod.COMPOUND_OWN_TYPE.run(nbtbase));
			}
			final Object o = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_TYPE, name);
			if (o == null)
				return null;
			return NBTType.valueOf((byte) o);
		} finally {
			this.readLock.unlock();
		}
	}

	public void writeCompound(final OutputStream stream) {
		try {
			this.writeLock.lock();
			NBTReflectionUtil.writeApiNBT(this, stream);
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public String toString() {
		/*
		 * StringBuilder result = new StringBuilder(); for (String key : getKeys()) {
		 * result.append(toString(key)); } return result.toString();
		 */
		return asNBTString();
	}

	/**
	 * @param key
	 * @return A string representation of the given key
	 * @deprecated Just use toString()
	 */
	@Deprecated
	public String toString(final String key) {
		/*
		 * StringBuilder result = new StringBuilder(); NBTCompound compound = this;
		 * while (compound.getParent() != null) { result.append("   "); compound =
		 * compound.getParent(); } if (this.getType(key) == NBTType.NBTTagCompound) {
		 * return this.getCompound(key).toString(); } else { return result + "-" + key +
		 * ": " + getContent(key) + System.lineSeparator(); }
		 */
		return asNBTString();
	}

	/**
	 * @return A {@link String} representation of the NBT in Mojang JSON. This is different from normal JSON!
	 * @deprecated Just use toString()
	 */
	@Deprecated
	public String asNBTString() {
		try {
			this.readLock.lock();
			final Object comp = NBTReflectionUtil.gettoCompount(getCompound(), this);
			if (comp == null)
				return "{}";
			return comp.toString();
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Does a deep compare to check if everything is the same
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof NBTCompound) {
			final NBTCompound other = (NBTCompound) obj;
			if (getKeys().equals(other.getKeys())) {
				for (final String key : getKeys()) {
					if (!isEqual(this, other, key)) {
						return false;
					}
					return true;
				}
			}
		}
		return false;
	}

	protected static boolean isEqual(final NBTCompound compA, final NBTCompound compB, final String key) {
		if (compA.getType(key) != compB.getType(key))
			return false;
		switch (compA.getType(key)) {
			case NBTTagByte:
				return compA.getByte(key).equals(compB.getByte(key));
			case NBTTagByteArray:
				return Arrays.equals(compA.getByteArray(key), compB.getByteArray(key));
			case NBTTagCompound:
				return compA.getCompound(key).equals(compB.getCompound(key));
			case NBTTagDouble:
				return compA.getDouble(key).equals(compB.getDouble(key));
			case NBTTagEnd:
				return true; //??
			case NBTTagFloat:
				return compA.getFloat(key).equals(compB.getFloat(key));
			case NBTTagInt:
				return compA.getInteger(key).equals(compB.getInteger(key));
			case NBTTagIntArray:
				return Arrays.equals(compA.getIntArray(key), compB.getIntArray(key));
			case NBTTagList:
				return NBTReflectionUtil.getEntry(compA, key).toString().equals(NBTReflectionUtil.getEntry(compB, key).toString()); // Just string compare the 2 lists
			case NBTTagLong:
				return compA.getLong(key).equals(compB.getLong(key));
			case NBTTagShort:
				return compA.getShort(key).equals(compB.getShort(key));
			case NBTTagString:
				return compA.getString(key).equals(compB.getString(key));
		}
		return false;
	}

}
