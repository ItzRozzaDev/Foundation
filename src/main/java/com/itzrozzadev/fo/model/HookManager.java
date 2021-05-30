package com.itzrozzadev.fo.model;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.server.TemporaryPlayer;
import com.earth2me.essentials.*;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.itzrozzadev.fo.*;
import com.itzrozzadev.fo.debug.Debugger;
import com.itzrozzadev.fo.exception.FoException;
import com.itzrozzadev.fo.plugin.SimplePlugin;
import com.itzrozzadev.fo.region.Region;
import com.itzrozzadev.fo.remain.Remain;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.xephi.authme.api.v3.AuthMeApi;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Our main class hooking into different plugins, providing you
 * convenience access to their methods
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HookManager {

	// ------------------------------------------------------------------------------------------------------------
	// Store hook classes separately for below, avoiding no such method/field errors
	// ------------------------------------------------------------------------------------------------------------

	private static AuthMeHook authMeHook;
	private static BanManagerHook banManagerHook;
	private static BossHook bossHook;
	private static CitizensHook citizensHook;
	private static DiscordSRVHook discordSRVHook;
	private static EssentialsHook essentialsHook;
	private static ItemsAdderHook itemsAdderHook;
	private static LiteBansHook liteBansHook;
	private static LocketteProHook locketteProHook;
	private static LWCHook lwcHook;
	private static MultiverseHook multiverseHook;
	private static MVdWPlaceholderHook MVdWPlaceholderHook;
	private static MythicMobsHook mythicMobsHook;
	private static NickyHook nickyHook;
	private static PlaceholderAPIHook placeholderAPIHook;
	private static PlotSquaredHook plotSquaredHook;
	private static ProtocolLibHook protocolLibHook;
	private static TownyHook townyHook;
	private static VaultHook vaultHook;
	private static WorldEditHook worldeditHook;
	private static WorldGuardHook worldguardHook;

	private static boolean nbtAPIDummyHook = false;
	private static boolean nuVotifierDummyHook = false;
	private static boolean townyChatDummyHook = false;

	// ------------------------------------------------------------------------------------------------------------
	// Main loading method
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Detect various plugins and load their methods into this library so you can use it later
	 */
	public static void loadDependencies() {
		if (Common.doesPluginExist("AuthMe"))
			authMeHook = new AuthMeHook();

		if (Common.doesPluginExist("BanManager"))
			banManagerHook = new BanManagerHook();

		if (Common.doesPluginExist("Boss"))
			bossHook = new BossHook();

		if (Common.doesPluginExist("Citizens"))
			citizensHook = new CitizensHook();

		if (Common.doesPluginExist("DiscordSRV"))
			try {
				Class.forName("github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel");

				discordSRVHook = new DiscordSRVHook();

			} catch (final ClassNotFoundException ex) {
				Common.error(ex, "&c" + SimplePlugin.getNamed() + " failed to hook DiscordSRV because the plugin is outdated (1.18.x is supported)!");
			}

		if (Common.doesPluginExist("Essentials"))
			essentialsHook = new EssentialsHook();

		if (Common.doesPluginExist("ItemsAdder"))
			itemsAdderHook = new ItemsAdderHook();

		if (Common.doesPluginExist("LiteBans"))
			liteBansHook = new LiteBansHook();

		if (Common.doesPluginExist("Lockette"))
			locketteProHook = new LocketteProHook();

		if (Common.doesPluginExist("LWC"))
			lwcHook = new LWCHook();

		if (Common.doesPluginExist("Multiverse-Core"))
			multiverseHook = new MultiverseHook();

		if (Common.doesPluginExist("MVdWPlaceholderAPI"))
			MVdWPlaceholderHook = new MVdWPlaceholderHook();

		if (Common.doesPluginExist("MythicMobs"))
			mythicMobsHook = new MythicMobsHook();

		if (Common.doesPluginExist("Nicky"))
			nickyHook = new NickyHook();

		if (Common.doesPluginExist("PlaceholderAPI"))
			placeholderAPIHook = new PlaceholderAPIHook();

		if (Common.doesPluginExist("PlotSquared")) {
			final String ver = Bukkit.getPluginManager().getPlugin("PlotSquared").getDescription().getVersion();

			if (ver.startsWith("5.") || ver.startsWith("3."))
				plotSquaredHook = new PlotSquaredHook();
			else
				Common.log("&cWarning: &fCould not hook into PlotSquared, version 3.x or 5.x required, you have " + ver);
		}

		if (Common.doesPluginExist("ProtocolLib")) {
			protocolLibHook = new ProtocolLibHook();

			// Also check if the library is loaded properly
			try {
				if (MinecraftVersion.newerThan(MinecraftVersion.V.v1_6))
					Class.forName("com.comphenix.protocol.wrappers.WrappedChatComponent");
			} catch (final Throwable t) {
				protocolLibHook = null;

				Common.throwError(t, "You are running an old and unsupported version of ProtocolLib, please update it.");
			}
		}


		if (Common.doesPluginExist("Towny"))
			townyHook = new TownyHook();

		if (Common.doesPluginExist("Vault"))
			vaultHook = new VaultHook();

		if (Common.doesPluginExist("WorldEdit") || Common.doesPluginExist("FastAsyncWorldEdit"))
			worldeditHook = new WorldEditHook();

		if (Common.doesPluginExist("WorldGuard"))
			worldguardHook = new WorldGuardHook(worldeditHook);

		// Dummy hooks

		if (Common.doesPluginExist("NBTAPI"))
			nbtAPIDummyHook = true;

		if (Common.doesPluginExist("Votifier"))
			nuVotifierDummyHook = true;

		if (Common.doesPluginExist("TownyChat"))
			townyChatDummyHook = true;
	}

	/**
	 * Removes packet listeners from ProtocolLib for a plugin
	 *
	 * @param plugin
	 * @deprecated internal use only, please do not call
	 */
	@Deprecated
	public static void unloadDependencies(final Plugin plugin) {
		if (isProtocolLibLoaded())
			protocolLibHook.removePacketListeners(plugin);

		if (isPlaceholderAPILoaded())
			placeholderAPIHook.unregister();
	}

	// ------------------------------------------------------------------------------------------------------------
	// Methods for determining which plugins were loaded after you call the load method
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Is AuthMe Reloaded loaded? We only support the latest version
	 *
	 * @return
	 */
	public static boolean isAuthMeLoaded() {
		return authMeHook != null;
	}

	/**
	 * Return if BanManager plugin is detected
	 *
	 * @return
	 */
	public static boolean isBanManagerLoaded() {
		return banManagerHook != null;
	}

	/**
	 * Return if Boss plugin is detected
	 *
	 * @return
	 */
	public static boolean isBossLoaded() {
		return bossHook != null;
	}

	/**
	 * Is Citizens loaded?
	 *
	 * @return
	 */
	public static boolean isCitizensLoaded() {
		return citizensHook != null;
	}

	/**
	 * Is DiscordSRV loaded?
	 *
	 * @return
	 */
	public static boolean isDiscordSRVLoaded() {
		return discordSRVHook != null;
	}

	/**
	 * Is EssentialsX loaded?
	 *
	 * @return
	 */
	public static boolean isEssentialsLoaded() {
		return essentialsHook != null;
	}

	/**
	 * Is FastAsyncWorldEdit loaded?
	 *
	 * @return
	 */
	public static boolean isFAWELoaded() {

		// Check for FAWE directly
		final Plugin fawe = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");

		if (fawe != null && fawe.isEnabled())
			return true;

		// Check for legacy FAWE installations
		final Plugin worldEdit = Bukkit.getPluginManager().getPlugin("WorldEdit");

		if (worldEdit != null && worldEdit.isEnabled() && "Fast Async WorldEdit plugin".equals(worldEdit.getDescription().getDescription()))
			return true;

		return false;
	}

	/**
	 * Is ItemsAdder loaded as a plugin?
	 *
	 * @return
	 */
	public static boolean isItemsAdderLoaded() {
		return itemsAdderHook != null;
	}

	/**
	 * Is LiteBans loaded?
	 *
	 * @return
	 */
	public static boolean isLiteBansLoaded() {
		return liteBansHook != null;
	}

	/**
	 * Is Lockette Pro loaded
	 *
	 * @return
	 */
	public static boolean isLocketteProLoaded() {
		return locketteProHook != null;
	}

	/**
	 * Is LWC loaded?
	 *
	 * @return
	 */
	public static boolean isLWCLoaded() {
		return lwcHook != null;
	}

	/**
	 * Is Multiverse-Core loaded?
	 *
	 * @return
	 */
	public static boolean isMultiverseCoreLoaded() {
		return multiverseHook != null;
	}

	/**
	 * Is MVdWPlaceholderAPI loaded?
	 *
	 * @return
	 */
	public static boolean isMVdWPlaceholderAPILoaded() {
		return MVdWPlaceholderHook != null;
	}

	/**
	 * Is MythicMobs loaded?
	 *
	 * @return
	 */
	public static boolean isMythicMobsLoaded() {
		return mythicMobsHook != null;
	}

	/**
	 * Is NBTAPI loaded as a plugin?
	 *
	 * @return
	 */
	public static boolean isNbtAPILoaded() {
		return nbtAPIDummyHook;
	}

	/**
	 * Is Nicky loaded?
	 *
	 * @return
	 */
	public static boolean isNickyLoaded() {
		return nickyHook != null;
	}

	/**
	 * Is nuVotifier loaded as a plugin?
	 *
	 * @return
	 */
	public static boolean isNuVotifierLoaded() {
		return nuVotifierDummyHook;
	}

	/**
	 * Is PlaceholderAPI loaded?
	 *
	 * @return
	 */
	public static boolean isPlaceholderAPILoaded() {
		return placeholderAPIHook != null;
	}

	/**
	 * Is PlotSquared loaded?
	 *
	 * @return
	 */
	public static boolean isPlotSquaredLoaded() {
		return plotSquaredHook != null;
	}

	/**
	 * Is ProtocolLib loaded?¡
	 * <p>
	 * This will not only check if the plugin is in plugins folder, but also if it's
	 * correctly loaded and working. (*Should* detect plugin's malfunction when
	 * out-dated.)
	 *
	 * @return
	 */
	public static boolean isProtocolLibLoaded() {
		return protocolLibHook != null;
	}


	/**
	 * Is Towny loaded?
	 *
	 * @return
	 */
	public static boolean isTownyLoaded() {
		return townyHook != null;
	}

	/**
	 * Is TownyChat loaded?
	 *
	 * @return
	 */
	public static boolean isTownyChatLoaded() {
		return townyHook != null && townyChatDummyHook;
	}

	/**
	 * Is Vault loaded?
	 *
	 * @return
	 */
	public static boolean isVaultLoaded() {
		return vaultHook != null;
	}

	/**
	 * Is WorldEdit loaded?
	 *
	 * @return
	 */
	public static boolean isWorldEditLoaded() {
		return worldeditHook != null || isFAWELoaded();
	}

	/**
	 * Is WorldGuard loaded?
	 *
	 * @return
	 */
	public static boolean isWorldGuardLoaded() {
		return worldguardHook != null;
	}

	// ------------------------------------------------------------------------------------------------------------
	//
	//
	// Delegate methods for use from other plugins
	//
	//
	// ------------------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------------------------
	// AuthMe
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return true if player is logged via AuthMe, or true if AuthMe is not installed
	 *
	 * @param player
	 * @return
	 */
	public static boolean isLogged(final Player player) {
		return !isAuthMeLoaded() || authMeHook.isLogged(player);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Boss-related plugins
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the Boss name from the given entity, if Boss plugin is installed and
	 * the given entity is a Boss, otherwise returns null.
	 *
	 * @param entity
	 * @return
	 */
	public static String getBossName(final Entity entity) {
		return isBossLoaded() ? bossHook.getBossName(entity) : null;
	}

	/**
	 * Returns the name from the given entity, if MythicMobs plugin is installed and
	 * the given entity is a mythic mob, otherwise returns null.
	 *
	 * @param entity
	 * @return
	 */
	public static String getMythicMobName(final Entity entity) {
		return isMythicMobsLoaded() ? mythicMobsHook.getBossName(entity) : null;
	}


	// ------------------------------------------------------------------------------------------------------------
	// EssentialsX
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return true if the given player is afk in EssentialsX or CMI, or false if neither plugin is present
	 *
	 * @param player
	 * @return
	 */
	public static boolean isAfk(final Player player) {

		return isEssentialsLoaded() && essentialsHook.isAfk(player.getName());
	}

	/**
	 * Return true if the given player is vanished in EssentialsX or CMI, or false if neither plugin is present
	 *
	 * @param player
	 * @return
	 * @deprecated this does not call metadata check in most plugins, see {@link PlayerUtil#isVanished(Player)}
	 */
	@Deprecated
	public static boolean isVanished(final Player player) {
		return isEssentialsLoaded() && essentialsHook.isVanished(player.getName());
	}

	/**
	 * Return true if the player is muted in EssentialsX or false if plugin is not present
	 *
	 * @param player
	 * @return
	 */
	public static boolean isMuted(final Player player) {

		if (isEssentialsLoaded() && essentialsHook.isMuted(player.getName()))
			return true;


		if (isBanManagerLoaded() && banManagerHook.isMuted(player))
			return true;

		return isLiteBansLoaded() && liteBansHook.isMuted(player);
	}

	/**
	 * If litebans is loaded, mute player - this expects you having /lmute command installed!
	 *
	 * @param player
	 * @param durationTokenized
	 * @param reason
	 */
	public static void setLiteBansMute(final Player player, final String durationTokenized, final String reason) {
		if (isLiteBansLoaded())
			Common.dispatchCommand(player, "lmute {player} " + durationTokenized + (reason == null || reason.isEmpty() ? "" : " " + reason));
	}

	/**
	 * If litebans is loaded, unmute player - this expects you having /lunmute command installed!
	 *
	 * @param player
	 */
	public static void setLiteBansUnmute(final Player player) {
		if (isLiteBansLoaded())
			Common.dispatchCommand(player, "lunmute {player}");
	}

	/**
	 * Toggles a god mode for player from EssentialsX
	 *
	 * @param player
	 * @param godMode
	 */
	public static void setGodMode(final Player player, final boolean godMode) {
		if (isEssentialsLoaded())
			essentialsHook.setGodMode(player, godMode);

	}

	/**
	 * Sets the last /back location for  EssentialsX
	 *
	 * @param player
	 * @param location
	 */
	public static void setBackLocation(final Player player, final Location location) {
		if (isEssentialsLoaded())
			essentialsHook.setBackLocation(player.getName(), location);


	}

	/**
	 * Set EssentialsX ignored player
	 *
	 * @param player
	 * @param who
	 * @param ignore
	 */
	public static void setIgnore(final UUID player, final UUID who, final boolean ignore) {
		if (isEssentialsLoaded())
			essentialsHook.setIgnore(player, who, ignore);

	}

	/**
	 * Return true if the player is ignoring another player in EssentialsX
	 *
	 * @param player
	 * @param who
	 * @return
	 */
	public static boolean isIgnoring(final UUID player, final UUID who) {
		Valid.checkBoolean(player != null, "Player to check ignore from cannot be null/empty");
		Valid.checkBoolean(who != null, "Player to check ignore to cannot be null/empty");

		return isEssentialsLoaded() && essentialsHook.isIgnoring(player, who);
	}

	/**
	 * Returns the nick for the given recipient from Essentials or Nicky, or if it's a console, their name
	 *
	 * @param sender
	 * @return
	 */
	public static String getNickColored(final CommandSender sender) {
		return getNick(sender, false);
	}

	/**
	 * Returns the nick for the given recipient from Essentials or Nicky, or if it's a console, their name
	 *
	 * @param sender
	 * @return
	 */
	public static String getNickColorless(final CommandSender sender) {
		return getNick(sender, true);
	}

	/**
	 * Returns the nick for the given recipient from Essentials or Nicky, or if it's a console, their name
	 *
	 * @param sender
	 * @param stripColors
	 * @return
	 */
	private static String getNick(final CommandSender sender, final boolean stripColors) {
		final Player player = sender instanceof Player ? (Player) sender : null;

		if (player != null && isNPC(player)) {
			Common.log("&eWarn: Called getNick for NPC " + player.getName() + "! Notify the developers to add an ignore check at " + Debugger.traceRoute(true));

			return player.getName();
		}

		if (player == null)
			return sender.getName();

		final String nickyNick = isNickyLoaded() ? nickyHook.getNick(player) : null;
		final String essNick = isEssentialsLoaded() ? essentialsHook.getNick(player.getName()) : null;

		final String nick = nickyNick != null ? nickyNick : essNick != null ? essNick : sender.getName();

		return stripColors ? Common.stripColors(Common.revertColorizing(nick).replace(ChatColor.COLOR_CHAR + "x", "")) : nick;
	}

	/**
	 * Sets the nickname for Essentials if installed for the given target player
	 *
	 * @param playerId
	 * @param nick
	 */
	public static void setNick(@NonNull final UUID playerId, @Nullable final String nick) {
		if (isEssentialsLoaded())
			essentialsHook.setNick(playerId, nick);

	}

	/**
	 * Attempts to reverse lookup player name from his nick
	 * Only Essentials
	 *
	 * @param nick
	 * @return
	 */
	public static String getNameFromNick(@NonNull final String nick) {
		final String essNick = isEssentialsLoaded() ? essentialsHook.getNameFromNick(nick) : nick;

		return !essNick.equals(nick) && !"".equals(essNick) ? essNick : nick;
	}

	// ------------------------------------------------------------------------------------------------------------
	// EssentialsX
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return the reply recipient for the given player, or null if not exist
	 *
	 * @param player
	 * @return
	 */
	public static Player getReplyTo(final Player player) {
		return isEssentialsLoaded() ? essentialsHook.getReplyTo(player.getName()) : null;
	}

	// ------------------------------------------------------------------------------------------------------------
	// ItemsAdder
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Use ItemsAdder to replace font images in the message
	 *
	 * @param message
	 * @return
	 */
	public static String replaceFontImages(final String message) {
		return replaceFontImages(null, message);
	}

	/**
	 * Use ItemsAdder to replace font images in the message based on the player's permission
	 *
	 * @param player
	 * @param message
	 * @return
	 */
	public static String replaceFontImages(@Nullable final Player player, final String message) {
		return isItemsAdderLoaded() ? itemsAdderHook.replaceFontImages(player, message) : message;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Multiverse-Core
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the world name alias from Multiverse-Core
	 *
	 * @param world
	 * @return
	 */
	public static String getWorldAlias(final World world) {
		return isMultiverseCoreLoaded() ? multiverseHook.getWorldAlias(world.getName()) : world.getName();
	}

	// ------------------------------------------------------------------------------------------------------------
	// Towny
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return players nation from Towny, or null if not loaded
	 *
	 * @param player
	 * @return
	 */
	public static String getNation(final Player player) {
		return isTownyLoaded() ? townyHook.getNationName(player) : null;
	}

	/**
	 * Return players town name from Towny, or null if none
	 *
	 * @param player
	 * @return
	 */
	public static String getTownName(final Player player) {
		return isTownyLoaded() ? townyHook.getTownName(player) : null;
	}

	/**
	 * Return the online residents in players town, or an empty list
	 *
	 * @param player
	 * @return
	 */
	public static Collection<? extends Player> getTownResidentsOnline(final Player player) {
		return isTownyLoaded() ? townyHook.getTownResidentsOnline(player) : new ArrayList<>();
	}

	/**
	 * Return the online nation players in players nation (Towny), or an empty list
	 *
	 * @param player
	 * @return
	 */
	public static Collection<? extends Player> getNationPlayersOnline(final Player player) {
		return isTownyLoaded() ? townyHook.getNationPlayersOnline(player) : new ArrayList<>();
	}

	/**
	 * Return the online nation players in players ally (Towny), or an empty list
	 *
	 * @param player
	 * @return
	 */
	public static Collection<? extends Player> getAllyPlayersOnline(final Player player) {
		return isTownyLoaded() ? townyHook.getAllyPlayersOnline(player) : new ArrayList<>();
	}

	/**
	 * Return the town owner name at the given location or null if none
	 *
	 * @param location
	 * @return
	 */
	public static String getTownOwner(final Location location) {
		return isTownyLoaded() ? townyHook.getTownName(location) : null;
	}


	/**
	 * Return the town name at the given location or null if none
	 *
	 * @param location
	 * @return
	 */
	public static String getTown(final Location location) {
		return isTownyLoaded() ? String.valueOf(townyHook.getTown(location)) : null;
	}

	/**
	 * Return a list of all loaded towns, or an empty list if none
	 *
	 * @return
	 */
	public static List<String> getTowns() {
		return isTownyLoaded() ? townyHook.getTowns() : new ArrayList<>();
	}

	// ------------------------------------------------------------------------------------------------------------
	// Vault
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return the Vault player prefix or empty if none
	 *
	 * @param player
	 * @return
	 */
	public static String getPlayerPrefix(final Player player) {
		return isVaultLoaded() ? vaultHook.getPlayerPrefix(player) : "";
	}

	/**
	 * Return the Vault player suffix or empty if none
	 *
	 * @param player
	 * @return
	 */
	public static String getPlayerSuffix(final Player player) {
		return isVaultLoaded() ? vaultHook.getPlayerSuffix(player) : "";
	}

	/**
	 * Return the Vault player permission group or empty if none
	 *
	 * @param player
	 * @return
	 */
	public static String getPlayerPermissionGroup(final Player player) {
		return isVaultLoaded() ? vaultHook.getPlayerGroup(player) : "";
	}

	/**
	 * Return the players balance from Vault (hooks into your economy plugin)
	 *
	 * @param player
	 * @return
	 */
	public static double getBalance(final Player player) {
		return isVaultLoaded() ? vaultHook.getBalance(player) : 0;
	}

	/**
	 * Return the singular currency name, or null if not loaded
	 *
	 * @return
	 */
	public static String getCurrencySingular() {
		return isVaultLoaded() ? vaultHook.getCurrencyNameSG() : null;
	}

	/**
	 * Return the plural currency name, or null if not loaded
	 *
	 * @return
	 */
	public static String getCurrencyPlural() {
		return isVaultLoaded() ? vaultHook.getCurrencyNamePL() : null;
	}

	/**
	 * Takes money from the player if Vault is installed
	 *
	 * @param player
	 * @param amount
	 */
	public static void withdraw(final Player player, final double amount) {
		if (isVaultLoaded())
			vaultHook.withdraw(player, amount);
	}

	/**
	 * Gives money to the player if Vault is installed
	 *
	 * @param player
	 * @param amount
	 */
	public static void deposit(final Player player, final double amount) {
		if (isVaultLoaded())
			vaultHook.deposit(player, amount);
	}

	/**
	 * Checks if the given player has the given permission, safe to use
	 * for instances where the player may be a {@link TemporaryPlayer} from
	 * ProtocolLib where then we use Vault to check the players perm
	 *
	 * @param player
	 * @param perm
	 * @return
	 */
	public static boolean hasProtocolLibPermission(final Player player, final String perm) {
		if (isProtocolLibLoaded() && protocolLibHook.isTemporaryPlayer(player))
			return hasVaultPermission(player, perm);

		return PlayerUtil.hasPerm(player, perm);
	}

	/**
	 * Checks if the given player name has a certain permission using vault
	 * Or throws an error if Vault is not present
	 *
	 * @param offlinePlayer
	 * @param perm
	 * @return
	 */
	public static boolean hasVaultPermission(final OfflinePlayer offlinePlayer, final String perm) {
		Valid.checkBoolean(isVaultLoaded(), "hasVaultPermission called - Please install Vault to enable this functionality!");

		return vaultHook.hasPerm(offlinePlayer, perm);
	}

	/**
	 * Returns the players primary permission group using Vault, or empty if none
	 *
	 * @param player
	 * @return
	 */
	public static String getPlayerPrimaryGroup(final Player player) {
		return isVaultLoaded() ? vaultHook.getPrimaryGroup(player) : "";
	}

	/**
	 * Return true if Vault could find a suitable chat plugin to hook to
	 *
	 * @return
	 */
	public static boolean isChatIntegrated() {
		return isVaultLoaded() ? vaultHook.isChatIntegrated() : false;
	}

	/**
	 * Return true if Vault could find a suitable economy plugin to hook to
	 *
	 * @return
	 */
	public static boolean isEconomyIntegrated() {
		return isVaultLoaded() ? vaultHook.isEconomyIntegrated() : false;
	}

	/**
	 * Updates Vault service providers
	 *
	 * @deprecated internal use only
	 */
	@Deprecated
	public static void updateVaultIntegration() {
		if (isVaultLoaded())
			vaultHook.setIntegration();
	}

	// ------------------------------------------------------------------------------------------------------------
	// PlaceholderAPI / MVdWPlaceholderAPI
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Uses PlaceholderAPI and MVdWPlaceholderAPI to replace placeholders in a message
	 *
	 * @param player
	 * @param message
	 * @return
	 */
	public static String replacePlaceholders(final Player player, String message) {
		if (message == null || "".equals(message.trim()))
			return message;

		message = isPlaceholderAPILoaded() ? placeholderAPIHook.replacePlaceholders(player, message) : message;
		message = isMVdWPlaceholderAPILoaded() ? MVdWPlaceholderHook.replacePlaceholders(player, message) : message;

		return message;
	}

	/**
	 * Uses PlaceholderAPI to replace relation placeholders in a message
	 *
	 * @param one
	 * @param two
	 * @param message
	 * @return
	 */
	public static String replaceRelationPlaceholders(final Player one, final Player two, final String message) {
		if (message == null || "".equals(message.trim()))
			return message;

		return isPlaceholderAPILoaded() ? placeholderAPIHook.replaceRelationPlaceholders(one, two, message) : message;
	}

	/**
	 * If PlaceholderAPI is loaded, registers a new placeholder within it
	 * with the given variable and value.
	 * <p>
	 * The variable is automatically prepended with your plugin name, lowercased + _,
	 * such as chatcontrol_ or boss_ + your variable.
	 * <p>
	 * Example if the variable is player health in ChatControl plugin: "chatcontrol_health"
	 * <p>
	 * The value will be called against the given player
	 * <p>
	 * <p>
	 * * ATTENTION: We now have a new system where you register variables through {@link Variables#addExpansion(SimpleExpansion)}
	 * instead. It gives you better flexibility and, like PlaceholderAPI, you can replace different variables on the fly.
	 *
	 * @param variable
	 * @param value
	 */
	public static void addPlaceholder(final String variable, final Function<Player, String> value) {
		Variables.addExpansion(new SimpleExpansion() {

			@Override
			protected String onReplace(@NonNull final CommandSender sender, final String identifier) {
				return variable.equalsIgnoreCase(identifier) && sender instanceof Player ? value.apply((Player) sender) : null;
			}
		});
	}

	// ------------------------------------------------------------------------------------------------------------
	// ProtocolLib
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Adds a {@link PacketAdapter} packet listener to ProtocolLib.
	 * <p>
	 * If the plugin is missing, an error will be thrown
	 *
	 * @param adapter
	 */
	public static void addPacketListener(/*Uses object to prevent errors if plugin is not installed*/final Object adapter) {
		Valid.checkBoolean(isProtocolLibLoaded(), "Cannot add packet listeners if ProtocolLib isn't installed");

		protocolLibHook.addPacketListener(adapter);
	}

	/**
	 * Send a {@link PacketContainer} to the given player
	 *
	 * @param player
	 * @param packetContainer
	 */
	public static void sendPacket(final Player player, final Object packetContainer) {
		Valid.checkBoolean(isProtocolLibLoaded(), "Sending packets requires ProtocolLib installed and loaded");

		protocolLibHook.sendPacket(player, packetContainer);
	}

	// ------------------------------------------------------------------------------------------------------------
	// LWC
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return the LWC owner of the block, or null
	 *
	 * @param block
	 * @return
	 */
	public static String getLWCOwner(final Block block) {
		return isLWCLoaded() ? lwcHook.getOwner(block) : null;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Lockette Pro
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return if the given player owns the given block from Lockette Pro
	 *
	 * @param block
	 * @param player
	 * @return
	 */
	public static boolean isLocketteOwner(final Block block, final Player player) {
		return isLocketteProLoaded() && locketteProHook.isOwner(block, player);
	}

	// ------------------------------------------------------------------------------------------------------------
	// WorldGuard
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return WorldGuard list of regions at the given location or an empty list
	 *
	 * @param loc
	 * @return
	 */
	public static List<String> getRegions(final Location loc) {
		return isWorldGuardLoaded() ? worldguardHook.getRegionsAt(loc) : new ArrayList<>();
	}

	/**
	 * Return WorldGuard list of loaded regions or an empty list
	 *
	 * @return
	 */
	public static List<String> getRegions() {
		return isWorldGuardLoaded() ? worldguardHook.getAllRegions() : new ArrayList<>();
	}

	/**
	 * Get our representation of a worldguard region by its name or null
	 *
	 * @param name
	 * @return
	 */
	public static Region getRegion(final String name) {
		return isWorldGuardLoaded() ? worldguardHook.getRegion(name) : null;
	}

	// ------------------------------------------------------------------------------------------------------------
	// PlotSquared
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Get a list of players inside a PlotSquared plot, or empty if not loaded
	 *
	 * @param players
	 * @return
	 */
	public static Collection<? extends Player> getPlotPlayers(final Player players) {
		return isPlotSquaredLoaded() ? plotSquaredHook.getPlotPlayers(players) : new ArrayList<>();
	}


	// ------------------------------------------------------------------------------------------------------------
	// Citizens
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return true if the entity is a Citizens NPC
	 *
	 * @param entity
	 * @return
	 */
	public static boolean isNPC(final Entity entity) {
		return isCitizensLoaded() ? citizensHook.isNPC(entity) : false;
	}

	// ------------------------------------------------------------------------------------------------------------
	// DiscordSRV
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return all linked Discord channels. You can link those in DiscordSRV config.yml file
	 *
	 * @return the linked channels or an empty set when DiscordSRV is not loaded
	 */
	public static Set<String> getDiscordChannels() {
		return isDiscordSRVLoaded() ? discordSRVHook.getChannels() : new HashSet<>();
	}

	/**
	 * Sends a message from the given sender to a certain channel on DiscordSRV
	 *
	 * @param senderName
	 * @param channel
	 * @param message
	 */
	/*public static void sendDiscordMessage(final String senderName, final String channel, final String message) {
		if (isDiscordSRVLoaded())
			discordSRVHook.sendMessage(senderName, channel, message);
	}*/

	/**
	 * Sends a message from the given sender to a certain channel on Discord using DiscordSRV
	 * <p>
	 * Enhanced functionality is available if the sender is a player
	 *
	 * @param sender
	 * @param channel
	 * @param message
	 */
	public static void sendDiscordMessage(final CommandSender sender, final String channel, @NonNull final String message) {
		if (isDiscordSRVLoaded() && !Common.stripColors(message).isEmpty())
			discordSRVHook.sendMessage(sender, channel, message);
	}

	/**
	 * Send a message to a Discord channel if DiscordSRV is installed
	 *
	 * @param channel
	 * @param message
	 */
	public static void sendDiscordMessage(final String channel, @NonNull final String message) {
		if (isDiscordSRVLoaded() && !Common.stripColors(message).isEmpty())
			discordSRVHook.sendMessage(channel, message);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Class helpers
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Represents a PlaceholderAPI placeholder replacer with the given
	 * variable (will be prepended with the name of your plugin, such as
	 * <p>
	 * chatcontrol_ + this variable
	 * <p>
	 * and the value that is callable so that you can return updated value each time.
	 */
	/*@Data
	static class PAPIPlaceholder {

		private final String variable;
		private final BiFunction<Player, String, String> value;
	}*/
}

// ------------------------------------------------------------------------------------------------------------
//
// Below are the individual classes responsible for hooking into third party plugins
// and getting data from them. Due to often changes we do not keep those documented.
//
// ------------------------------------------------------------------------------------------------------------

class AuthMeHook {

	boolean isLogged(final Player player) {
		try {
			final AuthMeApi instance = AuthMeApi.getInstance();

			return instance.isAuthenticated(player);
		} catch (final Throwable t) {
			return false;
		}
	}
}

class EssentialsHook {

	private final Essentials ess;

	EssentialsHook() {
		this.ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
	}

	void setGodMode(final Player player, final boolean godMode) {
		final User user = getUser(player.getName());

		if (user != null)
			user.setGodModeEnabled(godMode);
	}

	void setIgnore(final UUID player, final UUID toIgnore, final boolean ignore) {
		try {
			final com.earth2me.essentials.User user = this.ess.getUser(player);
			final com.earth2me.essentials.User toIgnoreUser = this.ess.getUser(toIgnore);

			if (toIgnoreUser != null)
				user.setIgnoredPlayer(toIgnoreUser, ignore);

		} catch (final Throwable t) {
		}
	}

	boolean isIgnoring(final UUID player, final UUID ignoringPlayer) {
		try {
			final com.earth2me.essentials.User user = this.ess.getUser(player);
			final com.earth2me.essentials.User ignored = this.ess.getUser(ignoringPlayer);

			return user != null && ignored != null && user.isIgnoredPlayer(ignored);

		} catch (final Throwable t) {
			return false;
		}
	}

	boolean isAfk(final String pl) {
		final IUser user = getUser(pl);

		return user != null ? user.isAfk() : false;
	}

	boolean isVanished(final String pl) {
		final IUser user = getUser(pl);

		return user != null ? user.isVanished() : false;
	}

	boolean isMuted(final String pl) {
		final com.earth2me.essentials.User user = getUser(pl);

		return user != null ? user.isMuted() : false;
	}

	Player getReplyTo(final String recipient) {
		final User user = getUser(recipient);

		if (user == null)
			return null;

		String replyPlayer = null;

		try {
			replyPlayer = user.getReplyRecipient().getName();

		} catch (final Throwable ex) {
			try {
				final Method getReplyTo = ReflectionUtil.getMethod(user.getClass(), "getReplyTo");

				if (getReplyTo != null) {
					final CommandSource commandSource = ReflectionUtil.invoke(getReplyTo, user);

					replyPlayer = commandSource == null ? null : commandSource.getPlayer().getName();
				}

			} catch (final Throwable t) {
				replyPlayer = null;
			}
		}

		final Player bukkitPlayer = replyPlayer == null ? null : Bukkit.getPlayer(replyPlayer);

		if (bukkitPlayer != null && bukkitPlayer.isOnline())
			return bukkitPlayer;

		return null;
	}

	String getNick(final String player) {
		final User user = getUser(player);

		if (user == null) {
			Common.log("&cMalfunction getting Essentials user. Have you reloaded?");

			return player;
		}

		final String essNick = Common.getOrEmpty(user.getNickname());

		return "".equals(essNick) ? null : essNick;
	}

	void setNick(final UUID uniqueId, final String nick) {
		final User user = getUser(uniqueId);

		if (user != null) {
			final boolean isEmpty = nick == null || Common.stripColors(nick).replace(" ", "").isEmpty();

			user.setNickname(isEmpty ? null : Common.colorize(nick));
		}
	}

	String getNameFromNick(final String maybeNick) {
		final UserMap users = this.ess.getUserMap();

		if (users != null)
			for (final UUID userId : users.getAllUniqueUsers()) {
				final User user = users.getUser(userId);

				if (user != null && user.getNickname() != null && Valid.colorlessEquals(user.getNickname(), maybeNick))
					return Common.getOrDefault(user.getName(), maybeNick);
			}

		return maybeNick;
	}

	void setBackLocation(final String player, final Location loc) {
		final User user = getUser(player);

		if (user != null)
			try {
				user.setLastLocation(loc);

			} catch (final Throwable t) {
			}
	}

	private User getUser(final String name) {
		if (this.ess.getUserMap() == null)
			return null;

		User user = null;

		try {
			user = this.ess.getUserMap().getUser(name);
		} catch (final Throwable t) {
		}

		if (user == null)
			try {
				user = this.ess.getUserMap().getUserFromBukkit(name);

			} catch (final Throwable ex) {
				user = this.ess.getUser(name);
			}
		return user;
	}

	private User getUser(final UUID uniqueId) {
		if (this.ess.getUserMap() == null)
			return null;

		User user = null;

		try {
			user = this.ess.getUserMap().getUser(uniqueId);
		} catch (final Throwable t) {
		}

		if (user == null)
			try {
				user = this.ess.getUser(uniqueId);
			} catch (final Throwable ex) {
			}

		return user;
	}

}

class MultiverseHook {

	private final MultiverseCore multiVerse;

	MultiverseHook() {
		this.multiVerse = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
	}

	String getWorldAlias(final String world) {
		final MultiverseWorld mvWorld = this.multiVerse.getMVWorldManager().getMVWorld(world);

		if (mvWorld != null)
			return mvWorld.getColoredWorldString();

		return world;
	}
}

class TownyHook {

	Collection<? extends Player> getTownResidentsOnline(final Player pl) {
		final List<Player> recipients = new ArrayList<>();
		final String playersTown = getTownName(pl);

		if (!playersTown.isEmpty())
			for (final Player online : Remain.getOnlinePlayers())
				if (playersTown.equals(getTownName(online)))
					recipients.add(online);

		return recipients;
	}

	Collection<? extends Player> getAllyPlayersOnline(final Player pl) {
		final List<Player> recipients = new ArrayList<>();
		final Resident resident = getResident(pl);

		if (resident != null)
			for (final Player online : Remain.getOnlinePlayers()) {
				final Resident otherResident = getResident(online);

				if (otherResident != null && otherResident.isAlliedWith(resident))
					recipients.add(online);
			}

		return recipients;
	}

	Collection<? extends Player> getNationPlayersOnline(final Player pl) {
		final List<Player> recipients = new ArrayList<>();
		final String playerNation = getNationName(pl);

		if (!playerNation.isEmpty())
			for (final Player online : Remain.getOnlinePlayers())
				if (playerNation.equals(getNationName(online)))
					recipients.add(online);

		return recipients;
	}

	String getTownName(final Player pl) {
		final Town t = getTown(pl);

		return t != null ? t.getName() : "";
	}

	String getNationName(final Player pl) {
		final Nation n = getNation(pl);

		return n != null ? n.getName() : "";
	}

	List<String> getTowns() {
		try {
			return Common.convert(com.palmergames.bukkit.towny.TownyUniverse.getInstance().getTowns(), Town::getName);

		} catch (final Throwable e) {
			return new ArrayList<>();
		}
	}

	String getTownName(final Location loc) {
		final Town town = getTown(loc);

		return town != null ? town.getName() : null;
	}

	Town getTown(final Location loc) {
		try {
			final WorldCoord worldCoord = WorldCoord.parseWorldCoord(loc);
			final TownBlock townBlock = com.palmergames.bukkit.towny.TownyUniverse.getInstance().getTownBlock(worldCoord);

			return townBlock != null ? townBlock.getTown() : null;

		} catch (final Throwable e) {
			return null;
		}
	}

	String getTownOwner(final Location loc) {
		try {
			final Town town = getTown(loc);

			return town != null ? town.getMayor().getName() : null;

		} catch (final Throwable e) {
			return null;
		}
	}

	private Nation getNation(final Player pl) {
		final Town town = getTown(pl);

		try {
			return town.getNation();

		} catch (final Throwable ex) {
			return null;
		}
	}

	private Town getTown(final Player pl) {
		final Resident res = getResident(pl);

		try {
			return res.getTown();

		} catch (final Throwable ex) {
			return null;
		}
	}

	Resident getResident(final Player player) {
		try {
			return TownyUniverse.getInstance().getResident(player.getName());

		} catch (final Throwable e) {
			return null;
		}
	}
}

class ProtocolLibHook {

	private final ProtocolManager manager;

	ProtocolLibHook() {
		this.manager = ProtocolLibrary.getProtocolManager();
	}

	final void addPacketListener(final Object listener) {
		Valid.checkBoolean(listener instanceof PacketListener, "Listener must extend or implements PacketListener or PacketAdapter");

		try {
			this.manager.addPacketListener((PacketListener) listener);

		} catch (final Throwable t) {
			Common.error(t, "Failed to register ProtocolLib packet listener! Ensure you have the latest ProtocolLib. If you reloaded, try a fresh startup (some ProtocolLib esp. for 1.8.8 fails on reload).");
		}
	}

	final void removePacketListeners(final Plugin plugin) {
		this.manager.removePacketListeners(plugin);
	}

	final void sendPacket(final PacketContainer packet) {
		for (final Player player : Remain.getOnlinePlayers())
			sendPacket(player, packet);
	}

	final void sendPacket(final Player player, final Object packet) {
		Valid.checkNotNull(player);
		Valid.checkBoolean(packet instanceof PacketContainer, "Packet must be instance of PacketContainer from ProtocolLib");

		try {
			this.manager.sendServerPacket(player, (PacketContainer) packet);

		} catch (final InvocationTargetException e) {
			Common.error(e, "Failed to send " + ((PacketContainer) packet).getType() + " packet to " + player.getName());
		}
	}

	final boolean isTemporaryPlayer(final Player player) {
		try {
			return player instanceof TemporaryPlayer;

		} catch (final NoClassDefFoundError err) {
			return false;
		}
	}
}

class VaultHook {

	private Chat chat;
	private Economy economy;
	private Permission permissions;

	VaultHook() {
		setIntegration();
	}

	void setIntegration() {
		final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
		final RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServicesManager().getRegistration(Chat.class);
		final RegisteredServiceProvider<Permission> permProvider = Bukkit.getServicesManager().getRegistration(Permission.class);

		if (economyProvider != null)
			this.economy = economyProvider.getProvider();

		if (chatProvider != null)
			this.chat = chatProvider.getProvider();

		if (permProvider != null)
			this.permissions = permProvider.getProvider();
	}

	boolean isChatIntegrated() {
		return this.chat != null;
	}

	boolean isEconomyIntegrated() {
		return this.economy != null;
	}

	// ------------------------------------------------------------------------------
	// Economy
	// ------------------------------------------------------------------------------

	String getCurrencyNameSG() {
		return this.economy != null ? Common.getOrEmpty(this.economy.currencyNameSingular()) : "Money";
	}

	String getCurrencyNamePL() {
		return this.economy != null ? Common.getOrEmpty(this.economy.currencyNamePlural()) : "Money";
	}

	double getBalance(final Player player) {
		return this.economy != null ? this.economy.getBalance(player) : -1;
	}

	void withdraw(final Player player, final double amount) {
		if (this.economy != null)
			this.economy.withdrawPlayer(player.getName(), amount);
	}

	void deposit(final Player player, final double amount) {
		if (this.economy != null)
			this.economy.depositPlayer(player.getName(), amount);
	}

	// ------------------------------------------------------------------------------
	// Permissions
	// ------------------------------------------------------------------------------

	Boolean hasPerm(@NonNull final OfflinePlayer player, final String perm) {
		try {
			return this.permissions != null ? perm != null ? this.permissions.playerHas((String) null, player, perm) : true : null;

		} catch (final Throwable t) {
			Common.logTimed(900,
					"SEVERE: Unable to ask Vault plugin if " + player.getName() + " has " + perm + " permission, returning false. "
							+ "This error only shows every 15 minutes. "
							+ "Run /vault-info and check if your permissions plugin is running correctly.");

			return false;
		}
	}

	Boolean hasPerm(@NonNull final String player, final String perm) {
		return this.permissions != null ? perm != null ? this.permissions.has((String) null, player, perm) : true : null;
	}

	Boolean hasPerm(@NonNull final String world, @NonNull final String player, final String perm) {
		return this.permissions != null ? perm != null ? this.permissions.has(world, player, perm) : true : null;
	}

	String getPrimaryGroup(final Player player) {
		return this.permissions != null ? this.permissions.getPrimaryGroup(player) : "";
	}

	// ------------------------------------------------------------------------------
	// Prefix / Suffix
	// ------------------------------------------------------------------------------

	String getPlayerPrefix(final Player player) {
		return lookupVault(player, VaultPart.PREFIX);
	}

	String getPlayerSuffix(final Player player) {
		return lookupVault(player, VaultPart.SUFFIX);
	}

	String getPlayerGroup(final Player player) {
		return lookupVault(player, VaultPart.GROUP);
	}

	private String lookupVault(final Player player, final VaultPart vaultPart) {
		if (this.chat == null)
			return "";

		final String[] groups = this.chat.getPlayerGroups(player);
		String fallback = vaultPart == VaultPart.PREFIX ? this.chat.getPlayerPrefix(player) : vaultPart == VaultPart.SUFFIX ? this.chat.getPlayerSuffix(player) : groups != null && groups.length > 0 ? groups[0] : "";

		if (fallback == null)
			fallback = "";

		if (vaultPart == VaultPart.PREFIX /*&& !SimplePlugin.getInstance().vaultMultiPrefix()*/ || vaultPart == VaultPart.SUFFIX /*&& !SimplePlugin.getInstance().vaultMultiSuffix()*/)
			return fallback;

		final List<String> list = new ArrayList<>();

		if (!fallback.isEmpty())
			list.add(fallback);

		if (groups != null)
			for (final String group : groups) {
				final String part = vaultPart == VaultPart.PREFIX ? this.chat.getGroupPrefix(player.getWorld(), group) : vaultPart == VaultPart.SUFFIX ? this.chat.getGroupSuffix(player.getWorld(), group) : group;

				if (part != null && !part.isEmpty() && !list.contains(part))
					list.add(part);
			}

		return StringUtils.join(list, vaultPart == VaultPart.GROUP ? ", " : "");
	}

	enum VaultPart {
		PREFIX,
		SUFFIX,
		GROUP,
	}
}

class PlaceholderAPIHook {

	private static volatile VariablesInjector injector;

	PlaceholderAPIHook() {
		try {
			injector = new VariablesInjector();
			injector.register();

		} catch (final Throwable throwable) {
			Common.error(throwable, "Failed to inject our variables into PlaceholderAPI!");
		}
	}

	final void unregister() {
		if (injector != null)
			try {
				injector.unregister();

			} catch (final Throwable t) {
				// Silence, probably plugin got removed in the meantime
			}
	}

	final String replacePlaceholders(final Player pl, final String msg) {
		try {
			return setPlaceholders(pl, msg);

		} catch (final Throwable t) {
			Common.error(t,
					"PlaceholderAPI failed to replace variables!",
					"Player: " + pl.getName(),
					"Message: " + msg,
					"Error: %error");

			return msg;
		}
	}

	private String setPlaceholders(final Player player, String text) {
		final Map<String, PlaceholderHook> hooks = PlaceholderAPI.getPlaceholders();

		if (hooks.isEmpty())
			return text;

		final Matcher matcher = Variables.BRACKET_PLACEHOLDER_PATTERN.matcher(text);

		while (matcher.find()) {
			String format = matcher.group(1);
			boolean frontSpace = false;
			boolean backSpace = false;

			if (format.startsWith("+")) {
				frontSpace = true;

				format = format.substring(1);
			}

			if (format.endsWith("+")) {
				backSpace = true;

				format = format.substring(0, format.length() - 1);
			}

			final int index = format.indexOf("_");

			if (index <= 0 || index >= format.length())
				continue;

			final String identifier = format.substring(0, index);
			final String params = format.substring(index + 1);

			if (hooks.containsKey(identifier)) {

				// Wait 0.5 seconds then kill the thread to prevent server
				// crashing on PlaceholderAPI variables hanging up on the main thread
				final Thread currentThread = Thread.currentThread();
				final BukkitTask watchDog = Common.runLater(20, () -> {
					Common.logFramed(
							"IMPORTANT: PREVENTED SERVER CRASH FROM PLACEHOLDERAPI",
							"Replacing a variable using PlaceholderAPI took",
							"longer than our maximum limit (1 second) and",
							"was forcefully interrupted to prevent your",
							"server from crashing. This is not error on",
							"our end, please contact the expansion author.",
							"",
							"Variable: " + identifier,
							"Player: " + player.getName());

					currentThread.stop();
				});

				String value = hooks.get(identifier).onRequest(player, params);

				// Indicate we no longer have to kill the thread
				watchDog.cancel();

				if (value != null) {
					value = Matcher.quoteReplacement(Common.colorize(value));

					text = text.replaceAll(Pattern.quote(matcher.group()), value.isEmpty() ? "" : (frontSpace ? " " : "") + value + (backSpace ? " " : ""));
				}
			}
		}

		return text;
	}

	final String replaceRelationPlaceholders(final Player one, final Player two, final String message) {
		try {
			return setRelationalPlaceholders(one, two, message);

		} catch (final Throwable t) {
			Common.error(t,
					"PlaceholderAPI failed to replace relation variables!",
					"Player one: " + one,
					"Player two: " + two,
					"Message: " + message,
					"Error: %error");

			return message;
		}
	}

	private String setRelationalPlaceholders(final Player one, final Player two, String text) {
		final Map<String, PlaceholderHook> hooks = PlaceholderAPI.getPlaceholders();

		if (hooks.isEmpty())
			return text;

		final Matcher matcher = Variables.BRACKET_REL_PLACEHOLDER_PATTERN.matcher(text);

		while (matcher.find()) {
			final String format = matcher.group(2);
			final int index = format.indexOf("_");

			if (index <= 0 || index >= format.length())
				continue;

			final String identifier = format.substring(0, index);
			final String params = format.substring(index + 1);

			if (hooks.containsKey(identifier)) {
				if (!(hooks.get(identifier) instanceof Relational))
					continue;

				final Relational rel = (Relational) hooks.get(identifier);
				final String value = one != null && two != null ? rel.onPlaceholderRequest(one, two, params) : "";

				if (value != null)
					text = text.replaceAll(Pattern.quote(matcher.group()), Matcher.quoteReplacement(Common.colorize(value)));
			}
		}

		return text;
	}

	private class VariablesInjector extends PlaceholderExpansion {

		/**
		 * Because this is an internal class,
		 * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
		 * PlaceholderAPI is reloaded
		 *
		 * @return true to persist through reloads
		 */
		@Override
		public boolean persist() {
			return true;
		}

		/**
		 * Because this is a internal class, this check is not needed
		 * and we can simply return {@code true}
		 *
		 * @return Always true since it's an internal class.
		 */
		@Override
		public boolean canRegister() {
			return true;
		}

		/**
		 * The name of the person who created this expansion should go here.
		 * <br>For convenience do we return the author from the plugin.yml
		 *
		 * @return The name of the author as a String.
		 */
		@Override
		public String getAuthor() {
			return SimplePlugin.getInstance().getDescription().getAuthors().toString();
		}

		/**
		 * The placeholder identifier should go here.
		 * <br>This is what tells PlaceholderAPI to call our onRequest
		 * method to obtain a value if a placeholder starts with our
		 * identifier.
		 * <br>This must be unique and can not contain % or _
		 *
		 * @return The identifier in {@code %<identifier>_<value>%} as String.
		 */
		@Override
		public String getIdentifier() {
			return SimplePlugin.getNamed().toLowerCase().replace("%", "").replace(" ", "").replace("_", "");
		}

		/**
		 * This is the version of the expansion.
		 * <br>You don't have to use numbers, since it is set as a String.
		 * <p>
		 * For convenience do we return the version from the plugin.yml
		 *
		 * @return The version as a String.
		 */
		@Override
		public String getVersion() {
			return SimplePlugin.getInstance().getDescription().getVersion();
		}

		/**
		 * Replace Foundation variables but with our plugin name added as prefix
		 * <p>
		 * We return null if an invalid placeholder (f.e. %ourplugin_nonexistingplaceholder%) is provided
		 */
		@Override
		public String onRequest(final OfflinePlayer offlinePlayer, @NonNull String identifier) {
			final Player player = offlinePlayer != null ? offlinePlayer.getPlayer() : null;

			if (player == null || !player.isOnline())
				return null;

			final boolean frontSpace = identifier.startsWith("+");
			final boolean backSpace = identifier.endsWith("+");

			identifier = frontSpace ? identifier.substring(1) : identifier;
			identifier = backSpace ? identifier.substring(0, identifier.length() - 1) : identifier;

			final Function<CommandSender, String> variable = Variables.getVariable(identifier);

			try {
				if (variable != null) {
					final String value = variable.apply(player);

					if (value != null)
						return value;
				}

				for (final SimpleExpansion expansion : Variables.getExpansions()) {
					final String value = expansion.replacePlaceholders(player, identifier);

					if (value != null) {
						final boolean emptyColorless = Common.stripColors(value).isEmpty();

						return (!value.isEmpty() && frontSpace && !emptyColorless ? " " : "") + value + (!value.isEmpty() && backSpace && !emptyColorless ? " " : "");
					}
				}

			} catch (final Exception ex) {
				Common.error(ex,
						"Error replacing PlaceholderAPI variables",
						"Identifier: " + identifier,
						"Player: " + player.getName());
			}

			return null;
		}
	}
}

class NickyHook {

	NickyHook() {
	}

	String getNick(final Player player) {
		final Constructor<?> nickConstructor = ReflectionUtil.getConstructor("io.loyloy.nicky.Nick", Player.class);
		final Object nick = ReflectionUtil.instantiate(nickConstructor, player);
		String nickname = ReflectionUtil.invoke("get", nick);

		if (nickname != null) {
			final Method formatMethod = ReflectionUtil.getMethod(nick.getClass(), "format", String.class);

			if (formatMethod != null)
				nickname = ReflectionUtil.invoke(formatMethod, nick, nickname);
		}

		return nickname != null && !nickname.isEmpty() ? nickname : null;
	}
}

class MVdWPlaceholderHook {

	MVdWPlaceholderHook() {
	}

	String replacePlaceholders(final Player player, final String message) {
		try {
			final Class<?> placeholderAPI = ReflectionUtil.lookupClass("be.maximvdw.placeholderapi.PlaceholderAPI");
			Valid.checkNotNull(placeholderAPI, "Failed to look up class be.maximvdw.placeholderapi.PlaceholderAPI");

			final Method replacePlaceholders = ReflectionUtil.getMethod(placeholderAPI, "replacePlaceholders", OfflinePlayer.class, String.class);
			Valid.checkNotNull(replacePlaceholders, "Failed to look up method PlaceholderAPI#replacePlaceholders(Player, String)");

			final String replaced = ReflectionUtil.invoke(replacePlaceholders, null, player, message);

			return replaced == null ? "" : replaced;

		} catch (final IllegalArgumentException ex) {
			if (!Common.getOrEmpty(ex.getMessage()).contains("Illegal group reference"))
				ex.printStackTrace();

		} catch (final Throwable t) {
			Common.error(t,
					"MvdWPlaceholders placeholders failed!",
					"Player: " + player.getName(),
					"Message: '" + message + "'",
					"Consider writing to developer of that library",
					"first as this may be a bug we cannot handle!",
					"",
					"Your chat message will appear without replacements.");
		}

		return message;
	}
}

class LWCHook {

	String getOwner(final Block block) {
		if (!LWC.ENABLED)
			return null;

		final Protection protection = LWC.getInstance().findProtection(block);

		if (protection != null) {
			final String uuid = protection.getOwner();

			if (uuid != null) {
				final OfflinePlayer opl = Remain.getOfflinePlayerByUUID(UUID.fromString(uuid));

				if (opl != null)
					return opl.getName();
			}
		}

		return null;
	}
}

class LocketteProHook {

	boolean isOwner(final Block block, final Player player) {
		final Class<?> locketteProAPI = ReflectionUtil.lookupClass("me.crafter.mc.lockettepro.LocketteProAPI");
		final Method isProtected = ReflectionUtil.getMethod(locketteProAPI, "isProtected", Block.class);
		final Method isOwner = ReflectionUtil.getMethod(locketteProAPI, "isOwner", Block.class, Player.class);

		return (boolean) ReflectionUtil.invoke(isProtected, null, block) ? ReflectionUtil.invoke(isOwner, null, block, player) : false;
	}
}


class WorldEditHook {

	public final boolean legacy;

	public WorldEditHook() {
		boolean ok = false;
		try {
			Class.forName("com.sk89q.worldedit.world.World");
			ok = true;
		} catch (final ClassNotFoundException e) {
		}

		this.legacy = !ok;
	}
}

class WorldGuardHook {

	private final boolean legacy;

	public WorldGuardHook(final WorldEditHook we) {
		final Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");

		this.legacy = !worldGuard.getDescription().getVersion().startsWith("7") || we != null && we.legacy;
	}

	public List<String> getRegionsAt(final Location loc) {
		final List<String> list = new ArrayList<>();

		getApplicableRegions(loc).forEach(reg -> {
			final String name = Common.stripColors(reg.getId());

			if (!name.startsWith("__"))
				list.add(name);
		});

		return list;
	}

	public Region getRegion(final String name) {
		for (final World world : Bukkit.getWorlds()) {
			final Object regionManager = getRegionManager(world);
			if (this.legacy)
				try {

					final Map<?, ?> regionMap = (Map<?, ?>) regionManager.getClass().getMethod("getRegions").invoke(regionManager);
					for (final Object regObj : regionMap.values()) {
						if (regObj == null)
							continue;

						if (Common.stripColors(((ProtectedRegion) regObj).getId()).equals(name)) {

							final Class<?> clazz = regObj.getClass();
							final Method getMax = clazz.getMethod("getMaximumPoint");
							final Method getMin = clazz.getMethod("getMinimumPoint");

							final Object regMax = getMax.invoke(regObj);
							final Object regMin = getMin.invoke(regObj);

							final Class<?> vectorClass = Class.forName("com.sk89q.worldedit.BlockVector");
							final Method getX = vectorClass.getMethod("getX");
							final Method getY = vectorClass.getMethod("getY");
							final Method getZ = vectorClass.getMethod("getZ");

							final Location locMax;
							final Location locMin;
							locMax = new Location(world, (Double) getX.invoke(regMax), (Double) getY.invoke(regMax), (Double) getZ.invoke(regMax));
							locMin = new Location(world, (Double) getX.invoke(regMin), (Double) getY.invoke(regMin), (Double) getZ.invoke(regMin));

							return new Region(name, locMin, locMax);
						}
					}

				} catch (final Throwable t) {
					t.printStackTrace();

					throw new FoException("Failed WorldEdit 6 legacy hook, see above & report");
				}
			else
				for (final ProtectedRegion region : ((com.sk89q.worldguard.protection.managers.RegionManager) regionManager).getRegions().values())
					if (region != null && region.getId() != null && Common.stripColors(region.getId()).equals(name)) {
						//if(reg instanceof com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion) {
						// just going to pretend that everything is a cuboid..
						final Location locMax;
						final Location locMin;
						final com.sk89q.worldedit.math.BlockVector3 maximumPoint = region.getMaximumPoint();
						final com.sk89q.worldedit.math.BlockVector3 minimumPoint = region.getMinimumPoint();

						locMax = new Location(world, maximumPoint.getX(), maximumPoint.getY(), maximumPoint.getZ());
						locMin = new Location(world, minimumPoint.getX(), minimumPoint.getY(), minimumPoint.getZ());

						return new Region(name, locMin, locMax);
					}
		}
		return null;
	}

	public List<String> getAllRegions() {
		final List<String> list = new ArrayList<>();

		for (final World w : Bukkit.getWorlds()) {
			final Object rm = getRegionManager(w);
			if (this.legacy)
				try {
					final Map<?, ?> regionMap = (Map<?, ?>) rm.getClass().getMethod("getRegions").invoke(rm);
					Method getId = null;
					for (final Object regObj : regionMap.values()) {
						if (regObj == null)
							continue;
						if (getId == null)
							getId = regObj.getClass().getMethod("getId");

						final String name = Common.stripColors(getId.invoke(regObj).toString());

						if (!name.startsWith("__"))
							list.add(name);
					}
				} catch (final Throwable t) {
					t.printStackTrace();

					throw new FoException("Failed WorldEdit 6 legacy hook, see above & report");
				}
			else
				((com.sk89q.worldguard.protection.managers.RegionManager) rm)
						.getRegions().values().forEach(reg -> {
					if (reg == null || reg.getId() == null)
						return;

					final String name = Common.stripColors(reg.getId());

					if (!name.startsWith("__"))
						list.add(name);
				});
		}

		return list;
	}

	private Iterable<ProtectedRegion> getApplicableRegions(final Location loc) {
		final Object rm = getRegionManager(loc.getWorld());

		if (this.legacy)
			try {
				return (Iterable<ProtectedRegion>) rm.getClass().getMethod("getApplicableRegions", Location.class).invoke(rm, loc);

			} catch (final Throwable t) {
				t.printStackTrace();

				throw new FoException("Failed WorldEdit 6 legacy hook, see above & report");
			}

		return ((com.sk89q.worldguard.protection.managers.RegionManager) rm)
				.getApplicableRegions(com.sk89q.worldedit.math.BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
	}

	private Object getRegionManager(final World w) {
		if (this.legacy)
			try {
				return Class.forName("com.sk89q.worldguard.bukkit.WGBukkit").getMethod("getRegionManager", World.class).invoke(null, w);

			} catch (final Throwable t) {
				t.printStackTrace();

				throw new FoException("Failed WorldGuard 6 legacy hook, see above & report");
			}

		// causes class errors..
		//return com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().get(new com.sk89q.worldedit.bukkit.BukkitWorld(w));
		// dynamically load modern WE
		try {

			final Class<?> bwClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitWorld");
			final Constructor<?> bwClassNew = bwClass.getConstructor(World.class);

			Object t = Class.forName("com.sk89q.worldguard.WorldGuard").getMethod("getInstance").invoke(null);
			t = t.getClass().getMethod("getPlatform").invoke(t);
			t = t.getClass().getMethod("getRegionContainer").invoke(t);
			return t.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World")).invoke(t, bwClassNew.newInstance(w));

		} catch (final Throwable t) {
			t.printStackTrace();

			throw new FoException("Failed WorldGuard hook, see above & report");
		}
	}
}

class PlotSquaredHook {

	private final boolean legacy;

	/**
	 *
	 */
	PlotSquaredHook() {
		final Plugin plugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
		Valid.checkNotNull(plugin, "PlotSquared not hooked yet!");

		this.legacy = plugin.getDescription().getVersion().startsWith("3");
	}

	List<Player> getPlotPlayers(final Player player) {
		final List<Player> players = new ArrayList<>();

		final Class<?> plotPlayerClass = ReflectionUtil.lookupClass((this.legacy ? "com.intellectualcrafters.plot.object" : "com.plotsquared.core.player") + ".PlotPlayer");
		Method wrap;

		try {
			wrap = plotPlayerClass.getMethod("wrap", Player.class);
		} catch (final ReflectiveOperationException ex) {
			try {
				wrap = plotPlayerClass.getMethod("wrap", Object.class);

			} catch (final ReflectiveOperationException ex2) {
				throw new NullPointerException("PlotSquared could not convert " + player.getName() + " into PlotPlayer! Is the integration outdated?");
			}
		}

		final Object plotPlayer = ReflectionUtil.invokeStatic(wrap, player);
		Valid.checkNotNull(plotPlayer, "Failed to convert player " + player.getName() + " to PlotPlayer!");

		final Object currentPlot = ReflectionUtil.invoke("getCurrentPlot", plotPlayer);

		if (currentPlot != null)
			for (final Object playerInPlot : (Iterable<?>) ReflectionUtil.invoke("getPlayersInPlot", currentPlot)) {
				final UUID id = ReflectionUtil.invoke("getUUID", playerInPlot);
				final Player online = Bukkit.getPlayer(id);

				if (online != null && online.isOnline())
					players.add(online);
			}

		return players;
	}
}

class CitizensHook {

	boolean isNPC(final Entity entity) {
		final NPCRegistry reg = CitizensAPI.getNPCRegistry();

		return reg != null && reg.isNPC(entity);
	}
}

class DiscordSRVHook implements Listener {

	Set<String> getChannels() {
		return DiscordSRV.getPlugin().getChannels().keySet();
	}

	/*boolean sendMessage(final String sender, final String channel, final String message) {
		final DiscordSender discordSender = new DiscordSender(sender);

		return sendMessage(discordSender, channel, message);
	}*/

	boolean sendMessage(final String channel, final String message) {
		return sendMessage((CommandSender) null, channel, message);
	}

	boolean sendMessage(final CommandSender sender, final String channel, final String message) {
		final TextChannel textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channel);

		// Channel not configured in DiscordSRV config.yml, ignore
		if (textChannel == null) {
			Debugger.debug("discord", "[MC->Discord] Could not find Discord channel '" + channel + "'. Available: " + String.join(", ", getChannels()) + ". Not sending: " + message);

			return false;
		}

		if (sender instanceof Player) {
			Debugger.debug("discord", "[MC->Discord] " + sender.getName() + " send message to '" + channel + "' channel. Message: '" + message + "'");

			final DiscordSRV instance = JavaPlugin.getPlugin(DiscordSRV.class);

			// Dirty: We have to temporarily unset value in DiscordSRV to enable the processChatMessage method to function
			final File file = new File(SimplePlugin.getData().getParent(), "DiscordSRV/config.yml");

			if (file.exists()) {
				final FileConfiguration discordConfig = YamlConfiguration.loadConfiguration(file);

				if (discordConfig != null) {
					final String outMessageKey = "DiscordChatChannelMinecraftToDiscord";
					final boolean outMessageOldValue = discordConfig.getBoolean(outMessageKey);

					discordConfig.set(outMessageKey, true);

					try {
						instance.processChatMessage((Player) sender, message, channel, false);

					} finally {
						discordConfig.set(outMessageKey, outMessageOldValue);
					}
				}
			}

		} else {
			Debugger.debug("discord", "[MC->Discord] " + (sender == null ? "No sender " : sender.getName() + " (generic)") + "sent message to '" + channel + "' channel. Message: '" + message + "'");

			DiscordUtil.sendMessage(textChannel, message);
		}
		return true;
	}
}

class BanManagerHook {

	/*
	 * Return true if the given player is muted
	 */
	boolean isMuted(final Player player) {
		try {
			final Class<?> api = ReflectionUtil.lookupClass("me.confuser.banmanager.common.api.BmAPI");
			final Method isMuted = ReflectionUtil.getMethod(api, "isMuted", UUID.class);

			return ReflectionUtil.invoke(isMuted, null, player.getUniqueId());

		} catch (final Throwable t) {
			if (!t.toString().contains("Could not find class"))
				Common.log("Unable to check if " + player.getName() + " is muted at BanManager. Is the API hook outdated? Got: " + t);

			return false;
		}
	}
}

class BossHook {

	/*
	 * Return the Boss name if the given player is a Boss or null
	 */
	String getBossName(final Entity entity) {
		try {
			final Class<?> api = ReflectionUtil.lookupClass("org.mineacademy.boss.api.BossAPI");
			final Method getBoss = ReflectionUtil.getMethod(api, "getBoss", Entity.class);

			final Object boss = ReflectionUtil.invoke(getBoss, null, entity);

			if (boss != null) {
				final Method getName = ReflectionUtil.getMethod(boss.getClass(), "getName");

				return ReflectionUtil.invoke(getName, boss);
			}

		} catch (final Throwable t) {
			Common.log("Unable to check if " + entity + " is a BOSS. Is the API hook outdated? Got: " + t);
		}

		return null;
	}
}

class MythicMobsHook {

	/*
	 * Attempt to return a MythicMob name from the given entity
	 * or null if the entity is not a MythicMob
	 */
	final String getBossName(final Entity entity) {
		try {
			final Class<?> mythicMobs = ReflectionUtil.lookupClass("io.lumine.xikage.mythicmobs.MythicMobs");
			final Object instance = ReflectionUtil.invokeStatic(mythicMobs, "inst");
			final Object mobManager = ReflectionUtil.invoke("getMobManager", instance);
			final Optional<Object> activeMob = ReflectionUtil.invoke(ReflectionUtil.getMethod(mobManager.getClass(), "getActiveMob", UUID.class), mobManager, entity.getUniqueId());
			final Object mob = activeMob != null && activeMob.isPresent() ? activeMob.get() : null;

			if (mob != null) {
				final Object mythicEntity = ReflectionUtil.invoke("getEntity", mob);

				if (mythicEntity != null)
					return (String) ReflectionUtil.invoke("getName", mythicEntity);
			}

		} catch (final NoSuchElementException ex) {
		}

		return null;
	}
}


class LiteBansHook {

	/*
	 * Return true if the given player is muted
	 */
	boolean isMuted(final Player player) {
		return false; // Problematic, we're investigating this
		/*try {
			final Class<?> api = ReflectionUtil.lookupClass("litebans.api.Database");
			final Object instance = ReflectionUtil.invokeStatic(api, "get");

			return ReflectionUtil.invoke("isPlayerMuted", instance, player.getUniqueId());

		} catch (final Throwable t) {
			if (!t.toString().contains("Could not find class")) {
				Common.log("Unable to check if " + player.getName() + " is muted at LiteBans. Is the API hook outdated? See console error:");

				t.printStackTrace();
			}

			return false;
		}*/
	}
}

class ItemsAdderHook {

	private final Class<?> itemsAdder;
	private final Method replaceFontImagesMethod;
	private final Method replaceFontImagesMethodNoPlayer;

	ItemsAdderHook() {
		this.itemsAdder = ReflectionUtil.lookupClass("dev.lone.itemsadder.api.FontImages.FontImageWrapper");
		this.replaceFontImagesMethod = ReflectionUtil.getDeclaredMethod(this.itemsAdder, "replaceFontImages", Player.class, String.class);
		this.replaceFontImagesMethodNoPlayer = ReflectionUtil.getDeclaredMethod(this.itemsAdder, "replaceFontImages", String.class);
	}

	/*
	 * Return true if the given player is muted
	 */
	String replaceFontImages(@Nullable final Player player, final String message) {
		if (player == null)
			return ReflectionUtil.invokeStatic(this.replaceFontImagesMethodNoPlayer, message);

		return ReflectionUtil.invokeStatic(this.replaceFontImagesMethod, player, message);
	}
}