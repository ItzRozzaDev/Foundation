package com.itzrozzadev.fo.constants;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.TimeUtil;
import com.itzrozzadev.fo.plugin.SimplePlugin;

import java.util.UUID;

/**
 * Stores constants for this plugin
 */
public final class FoConstants {

	/**
	 * Represents a UUID consisting of 0's only
	 */
	public static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	public static final class File {

		/**
		 * The name of our settings file
		 */
		public static final String SETTINGS = "config.yml";

		/**
		 * The error.log file created automatically to log errors to
		 */
		public static final String ERRORS = "error.log";

		/**
		 * The debug.log file to log debug messages to
		 */
		public static final String DEBUG = "debug.log";

		/**
		 * The FoConstants.DATA file (uses YAML) for saving various data
		 */
		public static final String DATA = "data.db";
	}

	public static final class Header {

		/**
		 * The header for FoConstants.DATA file
		 */
		public static final String[] DATA_FILE = new String[]{
				"",
				"This file stores metadata for the plugin.",
				"",
				" ** THE FILE IS MACHINE GENERATED. PLEASE DO NOT EDIT **",
				""
		};

		/**
		 * The header that is put into the file that has been automatically
		 * updated and comments were lost
		 */
		public static final String[] UPDATED_FILE = new String[]{
				Common.configLine(),
				"",
				" Your file has been automatically updated at " + TimeUtil.getFormattedDate(),
				" to " + SimplePlugin.getNamed() + " " + SimplePlugin.getVersion(),
				Common.configLine(),
				""
		};
	}

	public static final class NBT {

		/**
		 * Represents our NBT tag
		 */
		public static final String TAG = SimplePlugin.getNamed() + "_NbtTag";

		/**
		 * An internal metadata tag the player gets when he opens the menu
		 */
		public static final String TAG_MENU_CURRENT = SimplePlugin.getNamed() + "_Menu";

		/**
		 * An internal metadata tag the player gets when he opens another menu
		 */
		public static final String TAG_MENU_PREVIOUS = SimplePlugin.getNamed() + "_Previous_Menu";
	}
}
