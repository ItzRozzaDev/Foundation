package com.itzrozzadev.fo.command;

import com.itzrozzadev.fo.Valid;
import com.itzrozzadev.fo.plugin.SimplePlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * A simple subcommand belonging to a {@link SimpleCommandGroup}
 */
public abstract class SimpleSubCommand extends SimpleCommand {

	/**
	 * All registered sublabels this subcommand can have
	 */
	@Getter
	private final String[] sublabels;

	/**
	 * The latest sublabel used when the subcommand was run,
	 * always updated on executing
	 */
	@Setter(value = AccessLevel.PROTECTED)
	@Getter(value = AccessLevel.PROTECTED)
	private String sublabel;

	/**
	 * Create a new subcommand given the main plugin instance defines a main command group
	 *
	 * @param sublabel
	 */
	protected SimpleSubCommand(final String sublabel) {
		this(getMainCommandGroup0(), sublabel);
	}

	/*
	 * Attempts to get the main command group, failing with an error if not defined
	 */
	private static SimpleCommandGroup getMainCommandGroup0() {
		final SimpleCommandGroup main = SimplePlugin.getInstance().getMainCommand();
		Valid.checkNotNull(main, SimplePlugin.getNamed() + " does not define a main command group!");

		return main;
	}

	/**
	 * Creates a new subcommand belonging to a command group
	 *
	 * @param parent
	 * @param sublabel
	 */
	protected SimpleSubCommand(final SimpleCommandGroup parent, final String sublabel) {
		super(parent.getLabel());

		this.sublabels = sublabel.split("([|/])");
		Valid.checkBoolean(sublabels.length > 0, "Please set at least 1 sublabel");

		this.sublabel = sublabels[0];

		// If the default perm was not changed, improve it
		if (getRawPermission().equals(getDefaultPermission())) {
			final SimplePlugin instance = SimplePlugin.getInstance();

			if (instance.getMainCommand() != null && instance.getMainCommand().getLabel().equals(this.getMainLabel()))
				setPermission(getRawPermission().replace("{label}", "{sublabel}")); // simply replace label with sublabel

			else
				setPermission(getRawPermission() + ".{sublabel}"); // append the sublabel at the end since this is not our main command
		}
	}

	/**
	 * The command group automatically displays all subcommands in the /{label} help|? menu.
	 * Shall we display the subcommand in this menu?
	 *
	 * @return
	 */
	protected boolean showInHelp() {
		return true;
	}

	/**
	 * Replace additional {sublabel} placeholder for this subcommand.
	 * See {@link SimpleCommand#replacePlaceholders(String)}
	 */
	@Override
	protected String replacePlaceholders(final String message) {
		return super.replacePlaceholders(message).replace("{sublabel}", getSublabel());
	}

	@Override
	public final boolean equals(final Object obj) {
		return obj instanceof SimpleSubCommand && Arrays.equals(((SimpleSubCommand) obj).sublabels, this.sublabels);
	}
}
