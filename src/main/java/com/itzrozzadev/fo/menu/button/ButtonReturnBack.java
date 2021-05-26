package com.itzrozzadev.fo.menu.button;

import com.itzrozzadev.fo.menu.Menu;
import com.itzrozzadev.fo.menu.model.ItemCreator;
import com.itzrozzadev.fo.remain.CompMaterial;
import com.itzrozzadev.fo.settings.SimpleLocalization;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a standardized button that will return back to the parent menu
 */
@RequiredArgsConstructor
public final class ButtonReturnBack extends Button {

	/**
	 * The material for this button, door by default
	 */
	@Getter
	@Setter
	private static CompMaterial material = CompMaterial.OAK_DOOR;

	/**
	 * The title of this button
	 */
	@Getter
	@Setter
	private static String title = SimpleLocalization.Menu.BUTTON_RETURN_TITLE;

	/**
	 * The lore of this button
	 */
	@Getter
	@Setter
	private static List<String> lore = Arrays.asList(SimpleLocalization.Menu.BUTTON_RETURN_LORE);


	@NonNull
	private final Menu menu;

	/**
	 * The icon for this button
	 */
	@Override
	public ItemStack getItem() {
		return ItemCreator.of(material).name(title).lores(lore).build().make();
	}

	/**
	 * Open the parent menu when clicked
	 */
	@Override
	public void onClickedInMenu(final Player pl, final Menu menu, final ClickType click) {
		menu.onBackClick(pl, this.menu);
	}
}