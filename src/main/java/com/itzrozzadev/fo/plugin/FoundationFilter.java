package com.itzrozzadev.fo.plugin;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.MinecraftVersion;
import com.itzrozzadev.fo.settings.SimpleSettings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

/**
 * Represents the console filtering module
 */
final class FoundationFilter {

	/**
	 * The messages we should filter, plugin authors can customize this in {@link SimplePlugin}
	 */
	@Setter(value = AccessLevel.PACKAGE)
	private static List<String> MESSAGES_TO_FILTER = new ArrayList<>();

	/**
	 * Start filtering the console
	 */
	public static void inject() {

		// Set filter for System out
		System.setOut(new FilterSystem());

		// Set filter for Bukkit
		final FilterLegacy filter = new FilterLegacy();

		for (final Plugin plugin : Bukkit.getPluginManager().getPlugins())
			plugin.getLogger().setFilter(filter);

		Bukkit.getLogger().setFilter(filter);

		// Set Log4j filter
		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_7))
			FilterLog4j.inject();
	}

	/*
	 * Return true if the message is filtered
	 */
	static boolean isFiltered(String message) {
		if (message == null || message.isEmpty())
			return false;

		message = Common.stripColors(message);

		// Filter a warning since we've already patched this with NashornPlus extension
		if (message.equals("Warning: Nashorn engine is planned to be removed from a future JDK release"))
			return true;

		// Filter spam
		if (message.equals("Initializing Legacy Material Support. Unless you have legacy plugins and/or data this is a bug!"))
			return true;

		// One less spammy message for server owners
		if (message.endsWith("which is not a depend, softdepend or loadbefore of this plugin."))
			return true;

		message = message.toLowerCase();

		// Only filter this after plugin has been fully enabled
		if (!SimpleSettings.MAIN_COMMAND_ALIASES.isEmpty()) {

			// Filter inbuilt Foundation or ChatControl commands
			if (message.contains("issued server command: /" + SimpleSettings.MAIN_COMMAND_ALIASES.get(0) + " internal") || message.contains("issued server command: /#flp"))
				return true;

			// Filter user-defined commands
			if (SimplePlugin.hasInstance())
				for (String filter : SimplePlugin.getInstance().getConsoleFilter()) {
					filter = filter.toLowerCase();

					if (message.startsWith(filter) || message.contains(filter))
						return true;
				}
		}

		return false;
	}
}

/**
 * The old Bukkit filter
 */
class FilterLegacy implements java.util.logging.Filter {

	@Override
	public boolean isLoggable(final LogRecord record) {
		final String message = record.getMessage();

		return !FoundationFilter.isFiltered(message);
	}
}

/**
 * The System out filter
 */
class FilterSystem extends PrintStream {

	FilterSystem() {
		super(System.out);
	}

	@Override
	public void println(final Object x) {
		if (x != null && !FoundationFilter.isFiltered(x.toString()))
			super.println(x);
	}

	@Override
	public void println(final String x) {
		if (x != null && !FoundationFilter.isFiltered(x))
			super.println(x);
	}
}

/**
 * The new Log4j filter
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class FilterLog4j implements org.apache.logging.log4j.core.Filter {

	/*
	 * Starts logging for this filter
	 */
	static void inject() {
		try {
			((Logger) LogManager.getRootLogger()).addFilter(new FilterLog4j());

		} catch (final Throwable ex) {
			// Unsupported
		}
	}

	@Override
	public Result filter(final LogEvent record) {
		return checkMessage(record.getMessage().getFormattedMessage());
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String message, final Object... arg4) {
		return checkMessage(message);
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final Object message, final Throwable arg4) {
		return checkMessage(message.toString());
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final Message message, final Throwable arg4) {
		return checkMessage(message.getFormattedMessage());
	}

	/*
	 * Return if the message should be filtered
	 */
	private final Result checkMessage(final String message) {
		return FoundationFilter.isFiltered(message) ? Result.DENY : Result.NEUTRAL;
	}

	/* ------------------------------------------------------------ */
	/* Implementation required methods */
	/* ------------------------------------------------------------ */

	@Override
	public Result getOnMatch() {
		return Result.NEUTRAL;
	}

	@Override
	public Result getOnMismatch() {
		return Result.NEUTRAL;
	}

	@Override
	public State getState() {
		try {
			return State.STARTED;
		} catch (final Throwable t) {
			return null;
		}
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean isStarted() {
		return true;
	}

	@Override
	public boolean isStopped() {
		return false;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4) {
		return null;
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4, final Object arg5) {
		return null;
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4, final Object arg5, final Object arg6) {
		return null;
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7) {
		return null;
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7, final Object arg8) {
		return null;
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7, final Object arg8, final Object arg9) {
		return null;
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7, final Object arg8, final Object arg9, final Object arg10) {
		return null;
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7, final Object arg8, final Object arg9, final Object arg10, final Object arg11) {
		return null;
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7, final Object arg8, final Object arg9, final Object arg10, final Object arg11, final Object arg12) {
		return null;
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7, final Object arg8, final Object arg9, final Object arg10, final Object arg11, final Object arg12, final Object arg13) {
		return null;
	}
}