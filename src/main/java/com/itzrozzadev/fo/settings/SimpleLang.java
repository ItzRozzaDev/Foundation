package com.itzrozzadev.fo.settings;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.SerializeUtil;
import com.itzrozzadev.fo.Valid;
import com.itzrozzadev.fo.collection.SerializedMap;
import com.itzrozzadev.fo.exception.FoException;
import com.itzrozzadev.fo.model.JavaScriptExecutor;
import com.itzrozzadev.fo.model.SimpleComponent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the new way of plugin localization, with the greatest
 * upside of saving development time.
 * <p>
 * The downside is that keys are not checked during load so any
 * malformed or missing key will fail later and may be unnoticed.
 * <p>
 * Using the classic SimpleLocalization is still recommended to ensure
 * your users get notified when they malformed their localization file early
 * on startup.
 */
public final class SimpleLang extends YamlConfig {

	/**
	 * The instance of this class
	 */
	private static volatile SimpleLang instance;

	/**
	 * Set the instance in your plugin's onStart method.
	 *
	 * @param filePath
	 */
	public static void setInstance(final String filePath) {
		instance = new SimpleLang(filePath);
	}

	/**
	 * Set the instance in your plugin's onStart method.
	 * <p>
	 * In this method we pull the locale file from localization/messages_{SimplePrefix.LOCALE_PREFIX}.yml file
	 *
	 * @param filePath
	 */
	public static void setInstance() {
		instance = new SimpleLang("localization/messages_" + SimpleSettings.LOCALE_PREFIX + ".yml");
	}

	/**
	 * Creates a new instance
	 *
	 * @param path
	 */
	private SimpleLang(final String path) {
		this.loadConfiguration(path);
	}

	/**
	 * @see com.itzrozzadev.fo.settings.YamlConfig#saveComments()
	 */
	@Override
	protected boolean saveComments() {
		return true;
	}

	/*
	 * Return a key from our localization, failing if not exists
	 */
	private String getStringStrict(final String path) {
		final String key = getString(path);
		Valid.checkNotNull(key, "Missing localization key '" + path + "' from " + getFileName());

		return key;
	}

	/**
	 * Reload this file
	 */
	public static void reloadFile() {
		synchronized (instance) {
			instance.reload();
		}
	}

	/**
	 * Return a boolean at path
	 *
	 * @param path
	 * @return
	 */
	public static boolean getOption(final String path) {
		return instance.getBoolean(path);
	}

	/**
	 * Return a component list from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param variables
	 * @return
	 */
	public static List<SimpleComponent> ofComponentList(final String path, @Nullable final Object... variables) {
		return Common.convert(ofList(path, variables), SimpleComponent::of);
	}

	/**
	 * Return a list from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param variables
	 * @return
	 */
	public static List<String> ofList(final String path, @Nullable final Object... variables) {
		return Arrays.asList(ofArray(path, variables));
	}

	/**
	 * Return an array from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param variables
	 * @return
	 */
	public static String[] ofArray(final String path, @Nullable final Object... variables) {
		return of(path, variables).split("\n");
	}

	/**
	 * Return a component from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param variables
	 * @return
	 */
	public static SimpleComponent ofComponent(final String path, @Nullable final Object... variables) {
		return SimpleComponent.of(of(path, variables));
	}

	/**
	 * Return the given key for the given amount automatically
	 * singular or plural form including the amount
	 *
	 * @param amount
	 * @param path
	 * @return
	 */
	public static String ofCase(final long amount, final String path) {
		return amount + " " + ofCaseNoAmount(amount, path);
	}

	/**
	 * Return the given key for the given amount automatically
	 * singular or plural form excluding the amount
	 *
	 * @param amount
	 * @param path
	 * @return
	 */
	public static String ofCaseNoAmount(final long amount, final String path) {
		final String key = of(path);
		final String[] split = key.split(", ");

		Valid.checkBoolean(split.length == 1 || split.length == 2, "Invalid syntax of key at '" + path + "', this key is a special one and "
				+ "it needs singular and plural form separated with , such as: second, seconds");

		final String singular = split[0];
		final String plural = split[split.length == 2 ? 1 : 0];

		return amount == 0 || amount > 1 ? plural : singular;
	}

	/**
	 * Return an array from the localization file with {0} {1} etc. variables replaced.
	 * and script variables parsed. We treat the locale key as a valid JavaScript
	 *
	 * @param path
	 * @param scriptVariables
	 * @param variables
	 * @return
	 */
	public static String ofScript(final String path, final SerializedMap scriptVariables, @Nullable final Object... variables) {
		String script = of(path, variables);
		final Object result;

		// Our best guess is that the user has removed the script completely but forgot to put the entire message in '',
		// so we attempt to do so
		if (!script.contains("?") && !script.contains(":") && !script.contains("+") && !script.startsWith("'") && !script.endsWith("'"))
			script = "'" + script + "'";

		try {
			result = JavaScriptExecutor.run(script, scriptVariables.asMap());

		} catch (final Throwable t) {
			throw new FoException(t, "Failed to compile localization key '" + path + "' with script: " + script + " (this must be a valid JavaScript code)");
		}

		return result.toString();
	}

	/**
	 * Return a key from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param variables
	 * @return
	 */
	public static String of(final String path, @Nullable final Object... variables) {
		synchronized (instance) {
			final String key = instance.getStringStrict(path);

			return translate(key, variables);
		}
	}

	/*
	 * Replace placeholders in the message
	 */
	private static String translate(String key, @Nullable final Object... variables) {
		if (variables != null)
			for (int i = 0; i < variables.length; i++) {
				Object variable = variables[i];

				variable = Common.getOrDefaultStrict(SerializeUtil.serialize(variable), "");
				Valid.checkNotNull("Failed to replace {" + i + "} as " + variable + "(raw = " + variables[i] + ")");

				key = key.replace("{" + i + "}", variable.toString());
			}

		return key;
	}
}