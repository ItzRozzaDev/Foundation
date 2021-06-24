package com.itzrozzadev.fo.bungee;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.Valid;
import com.itzrozzadev.fo.bungee.message.IncomingMessage;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

/**
 * A unified way of combining Bungee channel, listener and action
 */
@Getter
public final class SimpleBungee {

	/**
	 * The channel
	 */
	private final String channel;

	/**
	 * The listener
	 */
	private final BungeeListener listener;

	/**
	 * The actions
	 */
	private final BungeeAction[] actions;

	/**
	 * Create a new simple bungee suite with the given channel, the given listener class and the given action as enum
	 *
	 * @param channel
	 * @param listenerClass
	 * @param actionEnum
	 */
	public SimpleBungee(final String channel, final Class<? extends BungeeListener> listenerClass, final Class<? extends BungeeAction> actionEnum) {
		this(channel, toListener(listenerClass), toAction(actionEnum));
	}

	private static BungeeListener toListener(final Class<? extends BungeeListener> listenerClass) {
		Valid.checkNotNull(listenerClass);

		try {
			final Constructor<?> con = listenerClass.getConstructor();
			con.setAccessible(true);

			return (BungeeListener) con.newInstance();
		} catch (final ReflectiveOperationException ex) {
			Common.log("Unable to create new instance of " + listenerClass + ", ensure constructor is public without parameters!");
			ex.printStackTrace();

			return null;
		}
	}

	private static BungeeAction[] toAction(final Class<? extends BungeeAction> actionEnum) {
		Valid.checkNotNull(actionEnum);
		Valid.checkBoolean(actionEnum.isEnum(), "Enum expected, given: " + actionEnum);

		try {
			return (BungeeAction[]) actionEnum.getMethod("values").invoke(null);

		} catch (final ReflectiveOperationException ex) {
			Common.log("Unable to get values() of " + actionEnum + ", ensure it is an enum!");
			ex.printStackTrace();

			return null;
		}
	}

	/**
	 * Create a new bungee suite with the given params
	 *
	 * @param channel
	 * @param listener
	 * @param actions
	 */
	public SimpleBungee(final String channel, final BungeeListener listener, final BungeeAction... actions) {
		Valid.checkNotNull(channel, "Channel cannot be null!");

		this.channel = channel;
		this.listener = listener;

		Valid.checkNotNull(actions, "Actions cannot be null!");
		this.actions = actions;
	}

	public SimpleBungee(final String channel) {
		this(channel, new BungeeListener() {
			@Override
			public void onMessageReceived(final Player player, final IncomingMessage incomingMessage) {

			}
		});
	}

}
