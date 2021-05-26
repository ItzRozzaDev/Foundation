package com.itzrozzadev.fo.command;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.model.Replacer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class OnlinePlayerCommand extends SimpleCommand {
	@Getter
	protected String playerNotOnline = "Player {playername} is not online";
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	private UUID uuid;
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	private String playerName;

	protected OnlinePlayerCommand(final String label) {
		super(label);
		setMinArguments(1);
		setUsage("<playerName>");
	}

	@Override
	protected void onCommand() {
		checkConsole();
		final Player player = findPlayer(args[0], Replacer.replaceArray(playerNotOnline, "playername", args[0]));
		setUuid(player.getUniqueId());
		setPlayerName(player.getName());
		onCommandFor(player);
		Common.broadcast();
	}

	protected abstract void onCommandFor(Player player);
}
