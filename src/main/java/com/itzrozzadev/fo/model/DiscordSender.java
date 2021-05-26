package com.itzrozzadev.fo.model;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.exception.FoException;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Discord command sender for Discord integration
 */
@Getter
@RequiredArgsConstructor
public final class DiscordSender implements CommandSender {

	private final User user;
	private final Member member;
	private final MessageChannel channel;
	private final Message message;
	//private final UUID uuid;

	@Override
	public boolean isPermissionSet(final String permission) {
		throw unsupported("isPermissionSet");
	}

	@Override
	public boolean isPermissionSet(final Permission permission) {
		throw unsupported("isPermissionSet");
	}

	@Override
	public boolean hasPermission(final String perm) {
		return false;

		//final OfflinePlayer offlinePlayer = Remain.getOfflinePlayerByUUID(this.uuid);
		//return perm == null ? true : offlinePlayer == null ? false : HookManager.hasVaultPermission(offlinePlayer, perm);
	}

	@Override
	public boolean hasPermission(final Permission perm) {
		return false;

		//final OfflinePlayer offlinePlayer = Remain.getOfflinePlayerByUUID(this.uuid);
		//return perm == null ? true : offlinePlayer == null ? false : HookManager.hasVaultPermission(offlinePlayer, perm.getName());
	}

	@Override
	public PermissionAttachment addAttachment(final Plugin plugin, final String name, final boolean value) {
		throw unsupported("addAttachment");
	}

	@Override
	public PermissionAttachment addAttachment(final Plugin plugin) {
		throw unsupported("addAttachment");
	}

	@Override
	public PermissionAttachment addAttachment(final Plugin plugin, final String name, final boolean value, final int ticks) {
		throw unsupported("addAttachment");
	}

	@Override
	public PermissionAttachment addAttachment(final Plugin plugin, final int ticks) {
		throw unsupported("addAttachment");
	}

	@Override
	public void removeAttachment(final PermissionAttachment attachment) {
		throw unsupported("removeAttachment");
	}

	@Override
	public void recalculatePermissions() {
		throw unsupported("recalculatePermissions");
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		throw unsupported("getEffectivePermissions");
	}

	@Override
	public boolean isOp() {
		throw unsupported("isOp");
	}

	@Override
	public void setOp(final boolean op) {
		throw unsupported("setOp");
	}

	@Override
	public void sendMessage(final String[] messages) {
		for (final String message : messages)
			sendMessage(message);
	}

	@Override
	public void sendMessage(final String message) {
		final String finalMessage = Common.stripColors(message);

		Common.runAsync(() -> {
			final Message sentMessage = channel.sendMessage(finalMessage).complete();

			try {
				// Automatically remove after a short while
				channel.deleteMessageById(sentMessage.getIdLong()).completeAfter(4, TimeUnit.SECONDS);

			} catch (final Throwable t) {

				// Ignore already deleted messages
				if (!t.toString().contains("Unknown Message"))
					t.printStackTrace();
			}
		});
	}

	@Override
	public String getName() {
		return Common.getOrDefaultStrict(member.getNickname(), user.getName());
	}

	@Override
	public Server getServer() {
		return Bukkit.getServer();
	}

	@Override
	public Spigot spigot() {
		throw unsupported("spigot");
	}

	private FoException unsupported(final String method) {
		return new FoException("DiscordSender cannot invoke " + method + "()");
	}

	/**
	 * @see org.bukkit.command.CommandSender#sendMessage(java.util.UUID, java.lang.String)
	 */
	//@Override - Disable to prevent errors in older MC
	@Override
	public void sendMessage(final UUID uuid, final String message) {
		this.sendMessage(message);
	}

	/**
	 * @see org.bukkit.command.CommandSender#sendMessage(java.util.UUID, java.lang.String[])
	 */
	//@Override - Disable to prevent errors in older MC
	@Override
	public void sendMessage(final UUID uuid, final String[] messages) {
		this.sendMessage(messages);
	}
}
