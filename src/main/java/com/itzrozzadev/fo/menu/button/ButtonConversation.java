package com.itzrozzadev.fo.menu.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.itzrozzadev.fo.Valid;
import com.itzrozzadev.fo.conversation.SimpleConversation;
import com.itzrozzadev.fo.conversation.SimplePrompt;
import com.itzrozzadev.fo.menu.Menu;
import com.itzrozzadev.fo.menu.model.ItemCreator;
import com.itzrozzadev.fo.remain.CompMaterial;

import lombok.Getter;

/**
 * A button that runs a server conversation
 */
public final class ButtonConversation extends Button {

	/**
	 * The server conversation to launch, or null if {@link SimplePrompt} is set
	 */
	private final SimpleConversation conversation;

	/**
	 * The prompt to show, null if {@link #conversation} is set
	 */
	private final SimplePrompt prompt;

	/**
	 * The item representing this button
	 */
	@Getter
	private final ItemStack item;

	/**
	 * Convenience shortcut for {@link ItemCreator#of(CompMaterial, String, String...)}
	 *
	 * @param convo
	 * @param material
	 * @param title
	 * @param lore
	 */
	public ButtonConversation(SimpleConversation convo, CompMaterial material, String title, String... lore) {
		this(convo, ItemCreator.of(material, title, lore));
	}

	/**
	 * Create a new button that starts a server conversation when clicked
	 *
	 * @param convo
	 * @param item
	 */
	public ButtonConversation(SimpleConversation convo, ItemCreator.ItemCreatorBuilder item) {
		this(convo, null, item.hideTags(true).build().make());
	}

	/**
	 * Create a new button that starts a server conversation when clicked
	 *
	 * @param convo
	 * @param item
	 */
	public ButtonConversation(SimpleConversation convo, ItemCreator item) {
		this(convo, null, item.make());
	}

	/**
	 * Convenience shortcut for {@link ItemCreator#of(CompMaterial, String, String...)}
	 *
	 * @param prompt
	 * @param material
	 * @param title
	 * @param lore
	 */
	public ButtonConversation(SimplePrompt prompt, CompMaterial material, String title, String... lore) {
		this(prompt, ItemCreator.of(material, title, lore));
	}

	/**
	 * Create a new conversation from a single prompt
	 *
	 * @param prompt
	 * @param item
	 */
	public ButtonConversation(SimplePrompt prompt, ItemCreator item) {
		this(null, prompt, item.make());
	}

	/**
	 * Create a new conversation from a single prompt
	 *
	 * @param prompt
	 * @param item
	 */
	public ButtonConversation(SimplePrompt prompt, ItemCreator.ItemCreatorBuilder item) {
		this(null, prompt, item.hideTags(true).build().make());
	}

	private ButtonConversation(SimpleConversation conversation, SimplePrompt prompt, ItemStack item) {
		this.conversation = conversation;
		this.prompt = prompt;
		this.item = item;
	}

	@Override
	public void onClickedInMenu(Player player, Menu menu, ClickType click) {
		Valid.checkBoolean(conversation != null || prompt != null, "Conversation and prompt cannot be null!");

		if (conversation != null) {
			conversation.setMenuToReturnTo(menu);

			conversation.start(player);

		} else
			prompt.show(player);

	}
}