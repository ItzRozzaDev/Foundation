package com.itzrozzadev.fo.settings;

import com.itzrozzadev.fo.Valid;

import java.util.Objects;

/**
 * An extension of {@link YamlConfig} useful when you want to separate
 * your settings classes but they all use the same file.
 * <p>
 * Example: CoreArena plugin uses classes and arenas, both save data in the FoConstants.DATA
 * file but have different section in that file.
 */
public abstract class YamlSectionConfig extends YamlConfig {

	/**
	 * The local path prefix, works just the same as the path prefix in
	 * {@link YamlConfig} however the path prefix there now works as the section prefix
	 * that does not change.
	 */
	private String localPathPrefix = "";

	/**
	 * Create a new section config with a section prefix,
	 * for example Players for storing player data.
	 *
	 * @param sectionPrefix, the section prefix, or null if not used
	 */
	protected YamlSectionConfig(final String sectionPrefix) {
		super.pathPrefix(sectionPrefix);
	}

	/**
	 * Return true if this section of the config file exists
	 *
	 * @return
	 */
	public final boolean isSectionValid() {
		return getObject("") != null;
	}

	/**
	 * Deletes the section from the file. This will keep the file,
	 * but only remove all data within {@link #getSection()}
	 */
	public final void deleteSection() {
		save("", null);
	}

	/**
	 * Return the {@link YamlConfig} path prefix, which here works
	 * as the permanent section name
	 *
	 * @return
	 */
	public final String getSection() {
		return getPathPrefix();
	}

	// ----------------------------------------------------------------
	// Path prefix
	// ----------------------------------------------------------------

	/**
	 * Set the path prefix for this section. This path prefix will be applied
	 * AFTER the {@link #getSection()} prefix.
	 */
	@Override
	protected final void pathPrefix(final String localPathPrefix) {
		if (localPathPrefix != null)
			Valid.checkBoolean(!localPathPrefix.endsWith("."), "Path prefix must not end with a dot: " + localPathPrefix);

		this.localPathPrefix = localPathPrefix == null || localPathPrefix.isEmpty() ? null : localPathPrefix;
	}

	/**
	 * A modified version of the super method allowing to add our section prefix and
	 * the temporary path prefix all together.
	 */
	@Override
	protected final String formPathPrefix(final String myPath) {
		String path = "";

		if (getPathPrefix() != null && !getPathPrefix().isEmpty())
			path += getPathPrefix() + ".";

		if (this.localPathPrefix != null && !this.localPathPrefix.isEmpty())
			path += this.localPathPrefix + ".";

		path = path + myPath;

		return path.endsWith(".") ? path.substring(0, path.length() - 1) : path;
	}

	// ----------------------------------------------------------------
	// Misc
	// ----------------------------------------------------------------

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "YamlSection{file=" + getFileName() + ", section=" + super.getPathPrefix() + ", local path=" + this.localPathPrefix + "}";
	}

	/**
	 * @see com.itzrozzadev.fo.settings.YamlConfig#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof YamlSectionConfig) {
			final YamlSectionConfig config = (YamlSectionConfig) obj;

			return config.getFileName().equals(this.getFileName()) && config.getPathPrefix().equals(this.getPathPrefix()) && Objects.deepEquals(config.localPathPrefix, this.localPathPrefix);
		}

		return false;
	}
}
