package com.itzrozzadev.fo.constants;

import com.itzrozzadev.fo.command.annotation.Permission;
import com.itzrozzadev.fo.plugin.SimplePlugin;

/**
 * Used to store basic library permissions
 */
public class FoPermissions {
	@Permission("Receive plugin update notifications on join.")
	public static final String NOTIFY_UPDATE = SimplePlugin.getNamed().toLowerCase() + ".notify.update";
}