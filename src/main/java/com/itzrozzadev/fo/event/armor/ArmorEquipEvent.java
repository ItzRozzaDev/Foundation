package com.itzrozzadev.fo.event.armor;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ArmorEquipEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private boolean cancel = false;

	private final EquipMethod equipType;

	private final ArmorType type;

	private ItemStack oldArmorPiece;

	private ItemStack newArmorPiece;

	public ArmorEquipEvent(final Player player, final EquipMethod equipType, final ArmorType type, final ItemStack oldArmorPiece, final ItemStack newArmorPiece) {
		super(player);
		this.equipType = equipType;
		this.type = type;
		this.oldArmorPiece = oldArmorPiece;
		this.newArmorPiece = newArmorPiece;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public final @NotNull HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public final void setCancelled(final boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public final boolean isCancelled() {
		return this.cancel;
	}

	public final ArmorType getType() {
		return this.type;
	}

	public final ItemStack getOldArmorPiece() {
		return this.oldArmorPiece;
	}

	public final void setOldArmorPiece(final ItemStack oldArmorPiece) {
		this.oldArmorPiece = oldArmorPiece;
	}

	public final ItemStack getNewArmorPiece() {
		return this.newArmorPiece;
	}

	public final void setNewArmorPiece(final ItemStack newArmorPiece) {
		this.newArmorPiece = newArmorPiece;
	}

	public EquipMethod getMethod() {
		return this.equipType;
	}

	public enum EquipMethod {
		SHIFT_CLICK, DRAG, PICK_DROP, HOTBAR, HOTBAR_SWAP, BROKE, DEATH
	}
}