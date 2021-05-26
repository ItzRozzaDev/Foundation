package com.itzrozzadev.fo;

import com.itzrozzadev.fo.collection.expiringmap.ExpiringMap;
import com.itzrozzadev.fo.exception.FoException;
import com.itzrozzadev.fo.model.HookManager;
import com.itzrozzadev.fo.remain.CompRunnable;
import com.itzrozzadev.fo.remain.Remain;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utility class for managing entities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityUtil {

	static {
		Common.registerEvents(new HitTracking());
	}

	/**
	 * Returns the entity target only if it is a player, or null
	 *
	 * @param entity
	 * @return
	 */
	public static Player getTargetPlayer(final Entity entity) {
		final LivingEntity target = getTarget(entity);
		return target instanceof Player && !HookManager.isNPC(target) ? (Player) target : null;
	}

	/**
	 * Return the target for the given entity
	 *
	 * @param entity
	 * @return the target, or null if does not have / unsupported
	 */
	public static LivingEntity getTarget(final Entity entity) {
		return entity instanceof Creature ? ((Creature) entity).getTarget() : null;
	}

	/**
	 * Return if this entity is creature and aggressive (not an animal)
	 *
	 * @param entity
	 * @return
	 */
	public static boolean isAggressive(final Entity entity) {
		if (entity instanceof Ghast || entity instanceof Slime)
			return true;

		if (entity instanceof Wolf && ((Wolf) entity).isAngry())
			return true;

		if (entity instanceof Animals)
			return false;

		return entity instanceof Creature;
	}


	/**
	 * Return if this entity is a {@link Creature}, {@link Slime} or {@link Wolf}
	 *
	 * @param entity
	 * @return
	 */
	public static boolean isCreature(final Entity entity) {
		return entity instanceof Slime ||
				entity instanceof Wolf ||
				entity instanceof Creature;
	}

	/**
	 * Return if this entity is suitable for removing (e.g. dropped items,
	 * falling blocks, arrows, projectiles)
	 *
	 * @param entity
	 * @return
	 */
	public static boolean canBeCleaned(final Entity entity) {
		return entity instanceof FallingBlock ||
				entity instanceof Item ||
				entity instanceof Projectile ||
				entity instanceof ExperienceOrb;
	}

	// ----------------------------------------------------------------------------------------------------
	// Dropping
	// ----------------------------------------------------------------------------------------------------

	/**
	 * Attempts to drop the item allowing space for applying properties to the item
	 * before it is spawned
	 *
	 * @param location
	 * @param item
	 * @param modifier
	 * @return the item
	 */
	public static Item dropItem(final Location location, final ItemStack item, final Consumer<Item> modifier) {
		return Remain.spawnItem(location, item, modifier);
	}

	// ----------------------------------------------------------------------------------------------------
	// Tracking
	// ----------------------------------------------------------------------------------------------------

	/**
	 * Runs a timer task with the tick period of 1 and will trigger your hit listener
	 * when the given entity isOnGround. If the entity gets removed before it hits
	 * the ground, nothing is called
	 * <p>
	 * If the entity still flies after 30 seconds, nothing is called
	 *
	 * @param entity
	 * @param hitGroundListener
	 */
	public static void trackFalling(final Entity entity, final Runnable hitGroundListener) {
		track(entity, 30 * 20, null, hitGroundListener);
	}

	/**
	 * Runs a timer task with the tick period of 1 and will trigger your fly listener
	 * each tick until entity is either removed or isOnGround
	 * <p>
	 * If the entity still flies after 30 seconds, nothing is called
	 *
	 * @param entity
	 * @param flyListener
	 */
	public static void trackFlying(final Entity entity, final Runnable flyListener) {
		track(entity, 30 * 20, flyListener, null);
	}

	/**
	 * Runs a timer task with the tick period of 1 and will trigger your hit listener
	 * when the given entity isOnGround. If the entity gets removed before it hits
	 * the ground, nothing is called
	 * <p>
	 * The fly listener is called every tick
	 *
	 * @param entity
	 * @param timeoutTicks
	 * @param flyListener
	 * @param hitGroundListener
	 */
	public static void track(final Entity entity, final int timeoutTicks, final Runnable flyListener, final Runnable hitGroundListener) {
		if (flyListener == null && hitGroundListener == null)
			throw new FoException("Cannot track entity with fly and hit listeners on null!");

		final boolean isProjectile = entity instanceof Projectile;

		if (isProjectile && hitGroundListener != null)
			HitTracking.addFlyingProjectile((Projectile) entity, event -> hitGroundListener.run());

		Common.runTimer(1, new CompRunnable() {

			private int elapsedTicks = 0;

			@Override
			public void run() {

				// Cancel after the given timeout to save performance
				if (elapsedTicks++ > timeoutTicks) {
					cancel();

					return;
				}

				// Cancel when invalid
				if (entity == null || entity.isDead() || !entity.isValid()) {
					if (entity instanceof FallingBlock && !isProjectile && hitGroundListener != null)
						hitGroundListener.run();

					cancel();

					return;
				}

				// Run the hit listener
				if (entity.isOnGround()) {
					if (!isProjectile && hitGroundListener != null)
						hitGroundListener.run();

					cancel();

				} else if (flyListener != null)
					flyListener.run();
			}
		});
	}

	/**
	 * (No timer task) Starts tracking a projectile's impact and executes the hit
	 * task when it hits something. After 30 seconds of flight we stop tracking
	 * to save performance
	 *
	 * @param projectile
	 * @param hitTask
	 */
	public static void trackHit(final Projectile projectile, final HitListener hitTask) {
		HitTracking.addFlyingProjectile(projectile, hitTask);
	}

	/**
	 * The class responsible for tracking projectile's impact
	 */
	public interface HitListener {

		/**
		 * What should happen when the projectile hits something?
		 *
		 * @param event
		 */
		void onHit(ProjectileHitEvent event);
	}
}

/**
 * Class responsible for tracking connection between projectile launch and projectile hit event
 */
class HitTracking implements Listener {

	/**
	 * List of flying projectiles with code to run on impact,
	 * stop tracking after 30 seconds to prevent overloading the map
	 */
	private static final ExpiringMap<UUID, EntityUtil.HitListener> flyingProjectiles = ExpiringMap.builder().expiration(30, TimeUnit.SECONDS).build();

	/**
	 * Invoke the hit listener when the registered projectile hits something
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHit(final ProjectileHitEvent event) {
		final EntityUtil.HitListener hitListener = flyingProjectiles.remove(event.getEntity().getUniqueId());

		if (hitListener != null)
			hitListener.onHit(event);
	}

	/**
	 * Add a new flying projectile that will be pending and execute code when collide
	 *
	 * @param projectile
	 * @param hitTask
	 */
	static void addFlyingProjectile(final Projectile projectile, final EntityUtil.HitListener hitTask) {
		flyingProjectiles.put(projectile.getUniqueId(), hitTask);
	}
}
