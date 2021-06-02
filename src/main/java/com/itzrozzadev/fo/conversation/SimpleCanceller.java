package com.itzrozzadev.fo.conversation;

import com.itzrozzadev.fo.Valid;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;

import java.util.Arrays;
import java.util.List;

/**
 * A simple conversation canceller
 * If the players message matches any word in the list, his conversation is cancelled
 */
public final class SimpleCanceller implements ConversationCanceller {

	/**
	 * The words that trigger the conversation cancellation
	 */
	private final List<String> cancelPhrases;
	private final SimpleConversation conversation;

	/**
	 * Create a new convo canceler based off the given strings
	 * If the players message matches any word in the list, his conversation is cancelled
	 *
	 * @param cancelPhrases
	 */
	public SimpleCanceller(final SimpleConversation conversation, final String... cancelPhrases) {
		this(conversation, Arrays.asList(cancelPhrases));

	}


	/**
	 * Create a new convo canceler from the given lists
	 * If the players message matches any word in the list, his conversation is cancelled
	 *
	 * @param cancelPhrases
	 */
	public SimpleCanceller(final SimpleConversation conversation, final List<String> cancelPhrases) {
		Valid.checkBoolean(!cancelPhrases.isEmpty(), "Cancel phrases are empty for conversation cancel listener!");
		this.conversation = conversation;
		this.cancelPhrases = cancelPhrases;
	}

	@Override
	public void setConversation(final Conversation conversation) {
	}

	/**
	 * Listen to cancel phrases and exit if they equals
	 */
	@Override
	public boolean cancelBasedOnInput(final ConversationContext context, final String input) {
		for (final String phrase : this.cancelPhrases)
			if (input.equalsIgnoreCase(phrase)) {
				this.conversation.onExit();
				return true;
			}

		return false;
	}

	@Override
	public ConversationCanceller clone() {
		return new SimpleCanceller(this.conversation, this.cancelPhrases);
	}

}