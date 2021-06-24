package com.itzrozzadev.fo.event.armor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorListener implements Listener {
	private final List<String> blockedMaterials;

	public ArmorListener() {
		this.blockedMaterials = new ArrayList<>();
	}

	public ArmorListener(final List<String> blockedMaterials) {
		this.blockedMaterials = blockedMaterials;
	}

	@EventHandler
	public final void onClick(final InventoryClickEvent event) {
		boolean shift = false, numberkey = false;
		if (event.isCancelled())
			return;
		if (event.getAction() == InventoryAction.NOTHING)
			return;
		if (event.getClick().equals(ClickType.SHIFT_LEFT) || event.getClick().equals(ClickType.SHIFT_RIGHT))
			shift = true;
		if (event.getClick().equals(ClickType.NUMBER_KEY))
			numberkey = true;
		if (event.getSlotType() != InventoryType.SlotType.ARMOR && event.getSlotType() != InventoryType.SlotType.QUICKBAR && event.getSlotType() != InventoryType.SlotType.CONTAINER)
			return;
		if (event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER))
			return;
		if (!event.getInventory().getType().equals(InventoryType.CRAFTING) && !event.getInventory().getType().equals(InventoryType.PLAYER))
			return;
		if (!(event.getWhoClicked() instanceof Player))
			return;
		ArmorType newArmorType = ArmorType.matchType(shift ? event.getCurrentItem() : event.getCursor());
		if (!shift && newArmorType != null && event.getRawSlot() != newArmorType.getSlot())
			return;
		if (shift) {
			newArmorType = ArmorType.matchType(event.getCurrentItem());
			if (newArmorType != null) {
				boolean equipping = true;
				if (event.getRawSlot() == newArmorType.getSlot())
					equipping = false;
				if ((newArmorType.equals(ArmorType.HELMET) && (equipping == isAirOrNull(event.getWhoClicked().getInventory().getHelmet()))) || (newArmorType.equals(ArmorType.CHESTPLATE) && (equipping ? isAirOrNull(event.getWhoClicked().getInventory().getChestplate()) : !isAirOrNull(event.getWhoClicked().getInventory().getChestplate()))) || (newArmorType.equals(ArmorType.LEGGINGS) && (equipping ? isAirOrNull(event.getWhoClicked().getInventory().getLeggings()) : !isAirOrNull(event.getWhoClicked().getInventory().getLeggings()))) || (newArmorType.equals(ArmorType.BOOTS) && (equipping ? isAirOrNull(event.getWhoClicked().getInventory().getBoots()) : !isAirOrNull(event.getWhoClicked().getInventory().getBoots())))) {
					final ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) event.getWhoClicked(), ArmorEquipEvent.EquipMethod.SHIFT_CLICK, newArmorType, equipping ? null : event.getCurrentItem(), equipping ? event.getCurrentItem() : null);
					Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
					if (armorEquipEvent.isCancelled())
						event.setCancelled(true);
				}
			}
		} else {
			ItemStack newArmorPiece = event.getCursor();
			ItemStack oldArmorPiece = event.getCurrentItem();
			if (numberkey) {
				if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
					final ItemStack hotbarItem = event.getClickedInventory().getItem(event.getHotbarButton());
					if (!isAirOrNull(hotbarItem)) {
						newArmorType = ArmorType.matchType(hotbarItem);
						newArmorPiece = hotbarItem;
						oldArmorPiece = event.getClickedInventory().getItem(event.getSlot());
					} else {
						newArmorType = ArmorType.matchType(!isAirOrNull(event.getCurrentItem()) ? event.getCurrentItem() : event.getCursor());
					}
				}
			} else if (isAirOrNull(event.getCursor()) && !isAirOrNull(event.getCurrentItem())) {
				newArmorType = ArmorType.matchType(event.getCurrentItem());
			}
			if (newArmorType != null && event.getRawSlot() == newArmorType.getSlot()) {
				ArmorEquipEvent.EquipMethod method = ArmorEquipEvent.EquipMethod.PICK_DROP;
				if (event.getAction().equals(InventoryAction.HOTBAR_SWAP) || numberkey)
					method = ArmorEquipEvent.EquipMethod.HOTBAR_SWAP;
				final ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) event.getWhoClicked(), method, newArmorType, oldArmorPiece, newArmorPiece);
				Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
				if (armorEquipEvent.isCancelled())
					event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL)
			return;
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final Player player = event.getPlayer();
			if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				final Material mat = event.getClickedBlock().getType();
				for (final String s : this.blockedMaterials) {
					if (mat.name().equalsIgnoreCase(s))
						return;
				}
			}
			final ArmorType newArmorType = ArmorType.matchType(event.getItem());
			if (newArmorType != null && ((
					newArmorType.equals(ArmorType.HELMET) && isAirOrNull(event.getPlayer().getInventory().getHelmet())) || (newArmorType.equals(ArmorType.CHESTPLATE) && isAirOrNull(event.getPlayer().getInventory().getChestplate())) || (newArmorType.equals(ArmorType.LEGGINGS) && isAirOrNull(event.getPlayer().getInventory().getLeggings())) || (newArmorType.equals(ArmorType.BOOTS) && isAirOrNull(event.getPlayer().getInventory().getBoots())))) {
				final ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(event.getPlayer(), ArmorEquipEvent.EquipMethod.HOTBAR, ArmorType.matchType(event.getItem()), null, event.getItem());
				Bukkit.getServer().getPluginManager().callEvent((Event) armorEquipEvent);
				if (armorEquipEvent.isCancelled()) {
					event.setCancelled(true);
					player.updateInventory();
				}
			}
		}
	}

	@EventHandler
	public void inventoryDrag(final InventoryDragEvent event) {
		final ArmorType type = ArmorType.matchType(event.getOldCursor());
		if (event.getRawSlots().isEmpty())
			return;
		if (type != null && type.getSlot() == event.getRawSlots().stream().findFirst().orElse(0)) {
			final ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) event.getWhoClicked(), ArmorEquipEvent.EquipMethod.DRAG, type, null, event.getOldCursor());
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if (armorEquipEvent.isCancelled()) {
				event.setResult(Event.Result.DENY);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onItemBreak(final PlayerItemBreakEvent event) {
		final ArmorType type = ArmorType.matchType(event.getBrokenItem());
		if (type != null) {
			final Player player = event.getPlayer();
			final ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.BROKE, type, event.getBrokenItem(), null);
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if (armorEquipEvent.isCancelled()) {
				final ItemStack i = event.getBrokenItem().clone();
				i.setAmount(1);
				i.setDurability((short) (i.getDurability() - 1));
				if (type.equals(ArmorType.HELMET)) {
					player.getInventory().setHelmet(i);
				} else if (type.equals(ArmorType.CHESTPLATE)) {
					player.getInventory().setChestplate(i);
				} else if (type.equals(ArmorType.LEGGINGS)) {
					player.getInventory().setLeggings(i);
				} else if (type.equals(ArmorType.BOOTS)) {
					player.getInventory().setBoots(i);
				}
			}
		}
	}

	@EventHandler
	public void onDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		for (final ItemStack i : player.getInventory().getArmorContents()) {
			if (!isAirOrNull(i))
				Bukkit.getServer().getPluginManager().callEvent(new ArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.DEATH, ArmorType.matchType(i), i, null));
		}
	}

	private boolean isAirOrNull(final ItemStack item) {
		return (item == null || item.getType().equals(Material.AIR));
	}
}