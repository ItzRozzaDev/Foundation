package com.itzrozzadev.fo.command;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.remain.Remain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public abstract class OfflinePlayerCommand extends SimpleCommand {

	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	private UUID uuid;
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	private String playerName;

	protected OfflinePlayerCommand(final String label) {
		super(label);
		setMinArguments(0);
		setUsage("<playerName>");
	}

	@Override
	protected void onCommand() {
		checkConsole();
		final OfflinePlayer player = Remain.getOfflinePlayerByName(this.args[0]);
		setUuid(player.getUniqueId());
		setPlayerName(player.getName());
		onCommandFor(player);
		Common.broadcast();
	}

	protected abstract void onCommandFor(OfflinePlayer player);
}
