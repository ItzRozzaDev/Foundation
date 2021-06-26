package com.itzrozzadev.fo.command;

import com.itzrozzadev.fo.*;
import com.itzrozzadev.fo.MinecraftVersion.V;
import com.itzrozzadev.fo.collection.StrictList;
import com.itzrozzadev.fo.exception.FoException;
import com.itzrozzadev.fo.model.ChatPaginator;
import com.itzrozzadev.fo.model.Replacer;
import com.itzrozzadev.fo.model.SimpleComponent;
import com.itzrozzadev.fo.plugin.SimplePlugin;
import com.itzrozzadev.fo.settings.SimpleLocalization;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A command group contains a set of different subcommands
 * associated with the main command, for example: /arena join, /arena leave etc.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SimpleCommandGroup {

	/**
	 * The list of sub-commands belonging to this command tree, for example
	 * the /boss command has subcommands /boss region, /boss menu etc.
	 */
	private final StrictList<SimpleSubCommand> subcommands = new StrictList<>();

	/**
	 * The registered main command, if any
	 */
	private SimpleCommand mainCommand;

	// Colors
	@Getter(value = AccessLevel.PROTECTED)
	private final ChatColor themeColor = setThemeColor();

	protected ChatColor setThemeColor() {
		return ChatColor.DARK_GRAY;
	}

	@Getter
	private final ChatColor arrowColor = setArrowColor();

	protected ChatColor setArrowColor() {
		return ChatColor.WHITE;
	}

	private final ChatColor mainArgument = setMainArgumentColor();

	protected ChatColor setMainArgumentColor() {
		return ChatColor.WHITE;
	}

	private final ChatColor firstArgument = setFirstArgumentColor();

	protected ChatColor setFirstArgumentColor() {
		return ChatColor.WHITE;
	}

	private final ChatColor requiredArgsColor = setRequiredArgumentsColor();

	protected ChatColor setRequiredArgumentsColor() {
		return ChatColor.WHITE;
	}


	private final ChatColor optionalArgsColor = setOptionalArgumentsColor();

	protected ChatColor setOptionalArgumentsColor() {
		return ChatColor.WHITE;
	}


	/**
	 * How many commands shall we display per page by default?
	 * <p>
	 * Defaults to 12
	 */

	private final int commandsPerPage = setCommandsPerPage();


	protected int setCommandsPerPage() {
		return 12;
	}

	// ----------------------------------------------------------------------
	// Main functions
	// ----------------------------------------------------------------------

	/**
	 * Register this command group into Bukkit and start using it
	 *
	 * @param labelAndAliases
	 */
	public final void register(final StrictList<String> labelAndAliases) {
		register(labelAndAliases.get(0), (labelAndAliases.size() > 1 ? labelAndAliases.range(1) : new StrictList<String>()).getSource());
	}

	/**
	 * Register this command group into Bukkit and start using it
	 *
	 * @param label
	 * @param aliases
	 */
	public final void register(final String label, final List<String> aliases) {
		Valid.checkBoolean(!isRegistered(), "Main command already registered as: " + this.mainCommand);

		this.mainCommand = new MainCommand(label);

		if (aliases != null)
			this.mainCommand.setAliases(aliases);

		this.mainCommand.register();
		registerSubcommands();

		// Sort A-Z
		this.subcommands.getSource().sort(Comparator.comparing(SimpleSubCommand::getSubLabel));

		// Check for collision
		checkSubCommandAliasesCollision();
	}

	/*
	 * Enforce non-overlapping aliases for subcommands
	 */
	private void checkSubCommandAliasesCollision() {
		final List<String> aliases = new ArrayList<>();

		for (final SimpleSubCommand subCommand : this.subcommands)
			for (final String alias : subCommand.getSublabels()) {
				Valid.checkBoolean(!aliases.contains(alias), "Subcommand '/" + getLabel() + " " + subCommand.getSubLabel() + "' has alias '" + alias + "' that is already in use by another subcommand!");

				aliases.add(alias);
			}
	}

	/**
	 * Remove this command group from Bukkit. Takes immediate changes in the game.
	 */
	public final void unregister() {
		Valid.checkBoolean(isRegistered(), "Main command not registered!");

		this.mainCommand.unregister();
		this.mainCommand = null;

		this.subcommands.clear();
	}

	/**
	 * Has the command group been registered yet?
	 *
	 * @return
	 */
	public final boolean isRegistered() {
		return this.mainCommand != null;
	}

	/**
	 * Scans all of your plugin's classes and registers commands extending the given class
	 * automatically.
	 *
	 * @param <T>
	 * @param ofClass
	 * @deprecated produces unexpected results if called more than once from your code, deal with caution!
	 */
	@Deprecated
	protected final <T extends SimpleSubCommand> void autoRegisterSubcommands(final Class<T> ofClass) {
		for (final Class<? extends SimpleSubCommand> clazz : ReflectionUtil.getClasses(SimplePlugin.getInstance(), ofClass))
			if (!Modifier.isAbstract(clazz.getModifiers()))
				registerSubcommand(ReflectionUtil.instantiate(clazz));
	}

	/**
	 * Extending method to register subcommands, call
	 * {@link #registerSubcommand(SimpleSubCommand)} and {@link #registerHelpLine(String...)}
	 * there for your command group.
	 */
	protected abstract void registerSubcommands();

	/**
	 * Registers a new subcommand for this group
	 *
	 * @param command
	 */
	protected final void registerSubcommand(final SimpleSubCommand command) {
		Valid.checkNotNull(this.mainCommand, "Cannot add subcommands when main command is missing! Call register()");
		Valid.checkBoolean(!this.subcommands.contains(command), "Subcommand /" + this.mainCommand.getLabel() + " " + command.getSubLabel() + " already registered when trying to add " + command.getClass());

		this.subcommands.add(command);
	}

	/**
	 * Registers a simple help message for this group, used in /{label} help|?
	 * since we add help for all subcommands automatically
	 *
	 * @param menuHelp
	 */
	protected final void registerHelpLine(final String... menuHelp) {
		Valid.checkNotNull(this.mainCommand, "Cannot add subcommands when main command is missing! Call register()");

		this.subcommands.add(new FillerSubCommand(this, menuHelp));
	}

	// ----------------------------------------------------------------------
	// Shortcuts
	// ----------------------------------------------------------------------

	/**
	 * Get the label for this command group, failing if not yet registered
	 *
	 * @return
	 */
	public final String getLabel() {
		Valid.checkBoolean(isRegistered(), "Main command has not yet been set!");

		return this.mainCommand.getMainLabel();
	}

	/**
	 * Return aliases for the main command
	 *
	 * @return
	 */
	public final List<String> getAliases() {
		return this.mainCommand.getAliases();
	}

	// ----------------------------------------------------------------------
	// Functions
	// ----------------------------------------------------------------------

	/**
	 * Return the message displayed when no parameter is given, by
	 * default we give credits
	 * <p>
	 * If you specify "author" in your plugin.yml we display author information
	 * If you override {@link SimplePlugin#getFoundedYear()} we display copyright
	 *
	 * @param sender the command sender that requested this to be shown to him
	 *               may be null
	 * @return
	 */
	protected List<SimpleComponent> getNoParamsHeader(final CommandSender sender) {
		final int foundedYear = SimplePlugin.getInstance().getFoundedYear();
		final int yearNow = Calendar.getInstance().get(Calendar.YEAR);

		final List<String> messages = new ArrayList<>();

		messages.add(getThemeColor() + Common.chatLineSmooth());
		messages.add(getHeaderPrefix() + "  " + SimplePlugin.getNamed() + getTrademark() + " &7" + SimplePlugin.getVersion());
		messages.add(" ");

		{
			final String authors = String.join(", ", SimplePlugin.getInstance().getDescription().getAuthors());

			if (!authors.isEmpty())
				messages.add("   &7" + SimpleLocalization.Commands.LABEL_AUTHORS + " &f" + authors + (foundedYear != -1 ? " &7\u00A9 " + foundedYear + (yearNow != foundedYear ? " - " + yearNow : "") : ""));
		}

		{
			final String credits = getCredits();

			if (credits != null && !credits.isEmpty())
				messages.add("   " + credits);
		}

		messages.add(getThemeColor() + Common.chatLineSmooth());

		return Common.convert(messages, SimpleComponent::of);
	}

	/**
	 * Should we send command helps instead of no-param header?
	 *
	 * @return
	 */

	protected boolean sendHelpIfNoArgs() {
		return false;
	}

	public String getTrademark() {
		return SimplePlugin.getInstance().getDescription().getAuthors().contains("ItzRozzaDev") ? getHeaderPrefix() + "&8\u2122" : "";
	}

	/**
	 * Get a part of the getNoParamsHeader() typically showing
	 * your website where the user can find more information about this command
	 * or your plugin in general
	 *
	 * @return
	 */
	protected String getCredits() {
		return "&7Visit &fitzrozzadev.com &7for more information.";
	}

	/**
	 * Return which subcommands should trigger the automatic help
	 * menu that shows all subcommands sender has permission for.
	 * <p>
	 * Also see {@link #getHelpHeader()}
	 * <p>
	 * Default: help and ?
	 *
	 * @return
	 */
	protected List<String> getHelpLabel() {
		return Arrays.asList("help", "?");
	}

	/**
	 * Return the header messages used in /{label} help|? typically
	 * used to tell all available subcommands from this command group
	 *
	 * @return
	 */
	protected String[] getHelpHeader() {
		return new String[]{
				"&8",
				this.themeColor + Common.chatLineSmooth(),
				getHeaderPrefix() + "  " + SimplePlugin.getNamed() + getTrademark() + " &7" + SimplePlugin.getVersion(),
				" ",
				this.requiredArgsColor + "  <> &f= " + this.requiredArgsColor + SimpleLocalization.Commands.LABEL_REQUIRED_ARGS,
				this.optionalArgsColor + "  [] &f= " + this.optionalArgsColor + SimpleLocalization.Commands.LABEL_OPTIONAL_ARGS,
				" "
		};
	}

	/**
	 * Return the subcommand description when listing all commands using the "help" or "?" subcommand
	 *
	 * @return
	 */
	protected String getSubcommandDescription() {
		return SimpleLocalization.Commands.LABEL_SUBCOMMAND_DESCRIPTION;
	}

	/**
	 * Return the default color in the {@link #getHelpHeader()},
	 * GOLD + BOLD colors by default
	 *
	 * @return
	 */
	protected String getHeaderPrefix() {
		return "" + getThemeColor() + ChatColor.BOLD;
	}

	// ----------------------------------------------------------------------
	// Execution
	// ----------------------------------------------------------------------

	/**
	 * The main command handling this command group
	 */
	public final class MainCommand extends SimpleCommand {

		/**
		 * Create new main command with the given label
		 *
		 * @param label
		 */
		private MainCommand(final String label) {
			super(label);

			// Let everyone view credits of this command when they run it without any sublabels
			setPermission(null);

			// We handle help ourselves
			setAutoHandleHelp(false);
		}

		/**
		 * Handle this command group, print a special message when no arguments are given,
		 * execute subcommands, handle help or ? argument and more.
		 */
		@Override
		protected void onCommand() {

			// Print a special message on no arguments
			if (this.args.length == 0) {
				if (sendHelpIfNoArgs())
					tellSubcommandsHelp();
				else
					tell(getNoParamsHeader(this.sender));

				return;
			}

			final String argument = this.args[0];
			final SimpleSubCommand command = findSubcommand(argument);

			// Handle subcommands
			if (command != null) {
				final String oldSubLabel = command.getSubLabel();

				try {
					// Simulate our main label
					command.setSubLabel(this.args[0]);

					// Run the command
					command.execute(this.sender, getLabel(), this.args.length == 1 ? new String[]{} : Arrays.copyOfRange(this.args, 1, this.args.length));

				} finally {
					// Restore old sublabel after the command has been run
					command.setSubLabel(oldSubLabel);
				}
			}

			// Handle help argument
			else if (!getHelpLabel().isEmpty() && Valid.isInList(argument, getHelpLabel()))
				tellSubcommandsHelp();

				// Handle unknown argument
			else
				returnInvalidArgs();
		}

		protected void onNoArgsHelp(final CommandSender sender) {

		}

		/**
		 * Automatically tells all help for all subcommands
		 */
		protected void tellSubcommandsHelp() {

			// Building help can be heavy so do it off of the main thread
			Common.runAsync(() -> {
				if (SimpleCommandGroup.this.subcommands.isEmpty()) {
					tell(SimpleLocalization.Commands.HEADER_NO_SUBCOMMANDS);
					return;
				}

				final List<SimpleComponent> lines = new ArrayList<>();
				final boolean atLeast17 = MinecraftVersion.atLeast(V.v1_7);

				for (final SimpleSubCommand subcommand : SimpleCommandGroup.this.subcommands)
					if (subcommand.showInHelp() && hasPerm(subcommand.getPermission())) {
						if (subcommand instanceof FillerSubCommand) {
							tellNoPrefix(((FillerSubCommand) subcommand).getHelpMessages());
							continue;
						}
						final String usage = colorizeUsage(subcommand.getUsage());
						final String desc = Common.getOrEmpty(subcommand.getDescription());
						final String plainMessage = Replacer.replaceArray(getSubcommandDescription(),
								"label", SimpleCommandGroup.this.mainArgument + "/" + getLabel(),
								"sublabel", SimpleCommandGroup.this.firstArgument + subcommand.getSubLabel(),
								"usage", usage,
								"sublabel", (atLeast17 ? "&n" : "") + subcommand.getSubLabel() + (atLeast17 ? "&r" : ""),
								"description", !desc.isEmpty() && !atLeast17 ? desc : "",
								"dash", !desc.isEmpty() && !atLeast17 ? "&e-" : "");

						final SimpleComponent line = SimpleComponent.of(plainMessage);

						if (!desc.isEmpty() && atLeast17) {
							final String command = SimpleCommandGroup.this.mainArgument + "/" + getLabel() + SimpleCommandGroup.this.firstArgument + subcommand.getSubLabel() + " ";
							final List<String> hover = new ArrayList<>();

							hover.add(SimpleLocalization.Commands.HELP_TOOLTIP_DESCRIPTION.replace("{description}", desc));

							if (subcommand.getPermission() != null && isOp())
								hover.add(SimpleLocalization.Commands.HELP_TOOLTIP_PERMISSION.replace("{permission}", subcommand.getPermission()));

							if (subcommand.getMultilineUsageMessage() != null && subcommand.getMultilineUsageMessage().length > 0) {
								hover.add(SimpleLocalization.Commands.HELP_TOOLTIP_USAGE);

								for (final String usageLine : subcommand.getMultilineUsageMessage())
									hover.add("&f" + replacePlaceholders(colorizeUsage(usageLine.replace("{sublabel}", subcommand.getSubLabel()))));

							} else
								hover.add(SimpleLocalization.Commands.HELP_TOOLTIP_USAGE + " " + (usage.isEmpty() ? command : command + usage));

							line.onHover(hover);
							line.onClickSuggestCmd("/" + getLabel() + " " + subcommand.getSubLabel());
						}

						lines.add(line);
					}

				if (!lines.isEmpty()) {
					final ChatPaginator pages = new ChatPaginator(MathUtil.range(0, lines.size(), SimpleCommandGroup.this.commandsPerPage), SimpleCommandGroup.this.themeColor);

					if (getHelpHeader() != null)
						if (SimplePlugin.getInstance().getMainCommand().getLabel().equals(this.getMainLabel()))

							pages.setHeader(getHelpHeader());
						else {
							pages.setFoundationHeader(getHelpHeader()[0]);
						}

					pages.setPages(lines);
					pages.setArrowColor(getArrowColor());

					// Send the component on the main thread
					Common.runLater(() -> pages.send(this.sender));

				} else
					tell(SimpleLocalization.Commands.HEADER_NO_SUBCOMMANDS_PERMISSION);
			});
		}

		/**
		 * Replaces some usage parameters such as <> or [] with colorized brackets
		 *
		 * @param message
		 * @return
		 */
		private String colorizeUsage(final String message) {
			return message == null ? "" : message.replace("<", SimpleCommandGroup.this.requiredArgsColor + "<").replace(">", SimpleCommandGroup.this.requiredArgsColor + ">&f")
					.replace("[", SimpleCommandGroup.this.optionalArgsColor + "[").replace("]", SimpleCommandGroup.this.optionalArgsColor + "]&f").replaceAll(" -([a-zA-Z])", " &3-$1");
		}

		/**
		 * Finds a subcommand by label
		 *
		 * @param label
		 * @return
		 */
		private SimpleSubCommand findSubcommand(final String label) {
			for (final SimpleSubCommand command : SimpleCommandGroup.this.subcommands) {
				if (command instanceof FillerSubCommand)
					continue;

				for (final String alias : command.getSublabels())
					if (alias.equalsIgnoreCase(label))
						return command;
			}

			return null;
		}

		/**
		 * Handle tabcomplete for subcommands and their tabcomplete
		 */
		@Override
		public List<String> tabComplete() {
			if (this.args.length == 1)
				return tabCompleteSubcommands(this.sender, this.args[0]);

			if (this.args.length > 1) {
				final SimpleSubCommand cmd = findSubcommand(this.args[0]);

				if (cmd != null)
					return cmd.tabComplete(this.sender, getLabel(), Arrays.copyOfRange(this.args, 1, this.args.length));
			}

			return null;
		}

		/**
		 * Automatically tab-complete subcommands
		 *
		 * @param sender
		 * @param param
		 * @return
		 */
		private List<String> tabCompleteSubcommands(final CommandSender sender, String param) {
			param = param.toLowerCase();

			final List<String> tab = new ArrayList<>();

			for (final SimpleSubCommand subcommand : SimpleCommandGroup.this.subcommands)
				if (subcommand.showInHelp() && !(subcommand instanceof FillerSubCommand) && hasPerm(subcommand.getPermission()))
					for (final String label : subcommand.getSublabels())
						if (!label.trim().isEmpty() && label.startsWith(param))
							tab.add(label);

			return tab;
		}
	}

	// ----------------------------------------------------------------------
	// Helper
	// ----------------------------------------------------------------------

	/**
	 * A helper class for showing plain messages in /{label} help|?
	 */
	private final class FillerSubCommand extends SimpleSubCommand {

		@Getter
		private final String[] helpMessages;

		private FillerSubCommand(final SimpleCommandGroup parent, final String... menuHelp) {
			super(parent, "_" + RandomUtil.nextBetween(1, Short.MAX_VALUE));

			this.helpMessages = menuHelp;
		}

		@Override
		protected void onCommand() {
			throw new FoException("Filler space command cannot be run!");
		}
	}

}