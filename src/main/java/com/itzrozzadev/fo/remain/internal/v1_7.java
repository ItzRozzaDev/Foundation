package com.itzrozzadev.fo.remain.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;
import com.itzrozzadev.fo.ReflectionUtil;

/**
 * Represents a fake dragon entity for Minecraft 1.7.x
 */
class v1_7 extends EnderDragonEntity {
	private Object dragon;
	private int id;

	public v1_7(String name, Location loc) {
		super(name, loc);
	}

	@Override
	public Object getSpawnPacket() {
		final Class<?> Entity = ReflectionUtil.getNMSClass("Entity");
		final Class<?> EntityLiving = ReflectionUtil.getNMSClass("EntityLiving");
		final Class<?> EntityEnderDragon = ReflectionUtil.getNMSClass("EntityEnderDragon");
		Object packet = null;
		try {
			dragon = EntityEnderDragon.getConstructor(ReflectionUtil.getNMSClass("World")).newInstance(getWorld());

			final Method setLocation = ReflectionUtil.getMethod(EntityEnderDragon, "setLocation", double.class, double.class, double.class, float.class, float.class);
			setLocation.invoke(dragon, getX(), getY(), getZ(), getPitch(), getYaw());

			final Method setInvisible = ReflectionUtil.getMethod(EntityEnderDragon, "setInvisible", boolean.class);
			setInvisible.invoke(dragon, isVisible());

			final Method setCustomName = ReflectionUtil.getMethod(EntityEnderDragon, "setCustomName", String.class);
			setCustomName.invoke(dragon, name);

			final Method setHealth = ReflectionUtil.getMethod(EntityEnderDragon, "setHealth", float.class);
			setHealth.invoke(dragon, health);

			final Field motX = ReflectionUtil.getDeclaredField(Entity, "motX");
			motX.set(dragon, getXvel());

			final Field motY = ReflectionUtil.getDeclaredField(Entity, "motY");
			motY.set(dragon, getYvel());

			final Field motZ = ReflectionUtil.getDeclaredField(Entity, "motZ");
			motZ.set(dragon, getZvel());

			final Method getId = ReflectionUtil.getMethod(EntityEnderDragon, "getId");
			this.id = (Integer) getId.invoke(dragon);

			final Class<?> PacketPlayOutSpawnEntityLiving = ReflectionUtil.getNMSClass("PacketPlayOutSpawnEntityLiving");

			packet = PacketPlayOutSpawnEntityLiving.getConstructor(EntityLiving).newInstance(dragon);
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getDestroyPacket() {
		final Class<?> PacketPlayOutEntityDestroy = ReflectionUtil.getNMSClass("PacketPlayOutEntityDestroy");

		Object packet = null;
		try {
			packet = PacketPlayOutEntityDestroy.newInstance();
			final Field a = PacketPlayOutEntityDestroy.getDeclaredField("a");
			a.setAccessible(true);
			a.set(packet, new int[] { id });
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getMetaPacket(Object watcher) {
		final Class<?> DataWatcher = ReflectionUtil.getNMSClass("DataWatcher");

		final Class<?> PacketPlayOutEntityMetadata = ReflectionUtil.getNMSClass("PacketPlayOutEntityMetadata");

		Object packet = null;
		try {
			packet = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class).newInstance(id, watcher, true);
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getTeleportPacket(Location loc) {
		final Class<?> PacketPlayOutEntityTeleport = ReflectionUtil.getNMSClass("PacketPlayOutEntityTeleport");

		Object packet = null;

		try {
			packet = PacketPlayOutEntityTeleport.getConstructor(int.class, int.class, int.class, int.class, byte.class, byte.class).newInstance(this.id, loc.getBlockX() * 32, loc.getBlockY() * 32, loc.getBlockZ() * 32, (byte) ((int) loc.getYaw() * 256 / 360), (byte) ((int) loc.getPitch() * 256 / 360));
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getWatcher() {
		final Class<?> Entity = ReflectionUtil.getNMSClass("Entity");
		final Class<?> DataWatcher = ReflectionUtil.getNMSClass("DataWatcher");

		Object watcher = null;
		try {
			watcher = DataWatcher.getConstructor(Entity).newInstance(dragon);
			final Method a = ReflectionUtil.getMethod(DataWatcher, "a", int.class, Object.class);

			a.invoke(watcher, 0, isVisible() ? (byte) 0 : (byte) 0x20);
			a.invoke(watcher, 6, health);
			a.invoke(watcher, 7, 0);
			a.invoke(watcher, 8, (byte) 0);
			a.invoke(watcher, 10, name);
			a.invoke(watcher, 11, (byte) 1);
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return watcher;
	}
}