package com.itzrozzadev.fo;

import com.itzrozzadev.fo.model.Whiteblacklist;
import com.itzrozzadev.fo.plugin.SimplePlugin;
import com.itzrozzadev.fo.remain.CompChatColor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.awt.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Utility class for managing in-game chat.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatUtil {

	/**
	 * The default center padding
	 */
	public final static int CENTER_PX = 152;

	/**
	 * The vertical lines a player can see at once in his chat history
	 */
	public final static int VISIBLE_CHAT_LINES = 20;

	/**
	 * Centers a message automatically for padding {@link #CENTER_PX}
	 *
	 * @param message - Message being centred
	 * @return - Centered message
	 */
	public static String center(final String message) {
		return center(message, ' ');
	}

	/**
	 * Centers a message for padding using the given center px
	 *
	 * @param message  - Message being centred
	 * @param centerPx - Centre px involved
	 * @return - Centered message
	 */
	public static String center(final String message, final int centerPx) {
		return center(message, ' ', centerPx);
	}

	/**
	 * Centers a message for padding {@link #CENTER_PX} with the given space character
	 * colored by the given chat color, example:
	 * <p>
	 * ================= My Centered Message ================= (if the character is '=')
	 *
	 * @param message   - Message being centred
	 * @param character - Added character
	 * @return - Centered message with character added
	 */
	public static String center(final String message, final char character) {
		return center(message, character, CENTER_PX);
	}

	/**
	 * Centers a message according to the given space character, color and padding
	 *
	 * @param message   - Message being centred
	 * @param character - Added character
	 * @param centerPx  - Centre px involved
	 * @return - Centered message with character added
	 */
	public static String center(final String message, final char character, final int centerPx) {
		if (message == null || message.equals(""))
			return "";

		int messagePxSize = 0;

		boolean previousCode = false;
		boolean isBold = false;

		for (final char c : message.toCharArray())

			if (c == '&' || c == ChatColor.COLOR_CHAR) {
				previousCode = true;

				continue;

			} else if (previousCode == true) {
				previousCode = false;

				if (c == 'l' || c == 'L') {
					isBold = true;

					continue;

				}

				isBold = false;

			} else {
				final DefaultFontInfo defaultFont = DefaultFontInfo.getDefaultFontInfo(c);

				messagePxSize += isBold ? defaultFont.getBoldLength() : defaultFont.getLength();
				messagePxSize++;
			}

		final StringBuilder builder = new StringBuilder();

		final int halvedMessageSize = messagePxSize / 2;
		final int toCompensate = centerPx - halvedMessageSize;
		final int spaceLength = DefaultFontInfo.getDefaultFontInfo(character).getLength() + (isBold ? 2 : 1);

		int compensated = 0;

		while (compensated < toCompensate) {
			builder.append(character);

			compensated += spaceLength;
		}

		return builder.toString() + " " + message + " " + builder.toString();
	}

	/**
	 * Moves the given messages to the center of the chat screen
	 *
	 * @param messages - Message being centered
	 * @return - Centered Message
	 */
	public static String[] verticalCenter(final String... messages) {
		return verticalCenter(Arrays.asList(messages));
	}

	/**
	 * Moves the given messages to the center of the chat screen
	 *
	 * @param messages - Message being centered
	 * @return - Centered Message
	 */
	public static String[] verticalCenter(final Collection<String> messages) {
		final List<String> lines = new ArrayList<>();
		final long padding = MathUtil.ceiling((float) (VISIBLE_CHAT_LINES - messages.size()) / 2);

		for (int i = 0; i < padding; i++)
			lines.add(RandomUtil.nextColorOrDecoration());

		lines.addAll(messages);

		for (int i = 0; i < padding; i++)
			lines.add(RandomUtil.nextColorOrDecoration());

		return lines.toArray(new String[0]);
	}

	/**
	 * Inserts dots '.' into the message. Ignores domains and numbers.
	 *
	 * @param message - The message to be processed
	 * @return _ The message with dots inserted
	 */
	public static String insertDot(String message) {
		if (message.isEmpty())
			return "";

		final String lastChar = message.substring(message.length() - 1);
		final String[] words = message.split("\\s");
		final String lastWord = words[words.length - 1];

		if (!isDomain(lastWord) && lastChar.matches("(?i)[a-z\u0400-\u04FF]"))
			message = message + ".";

		return message;
	}

	/**
	 * Makes first letters of sentences uppercase. Ignores domains and detects multiple
	 * sentences.
	 *
	 * @param message - The message to check
	 * @return -  The Capitalized message
	 */
	public static String capitalize(final String message) {
		if (message.isEmpty())
			return "";

		final String[] sentences = message.split("(?<=[!?.])\\s");
		final StringBuilder tempMessage = new StringBuilder();

		for (String sentence : sentences) {
			try {
				final String word = message.split("\\s")[0];

				if (!isDomain(word))
					sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1);

				tempMessage.append(sentence).append(" ");
			} catch (final ArrayIndexOutOfBoundsException ex) {
				// Probably an exotic language, silence
			}
		}

		return tempMessage.toString().trim();
	}

	/**
	 * Set the second character to lowercase in a sentence in the message.
	 *
	 * @param message - Message involved
	 * @return - Message with second character set to lowercase
	 */
	public static String lowercaseSecondChar(final String message) {
		if (message.isEmpty())
			return "";

		final String[] sentences = message.split("(?<=[!?.])\\s");
		String tempMessage = "";

		for (String sentence : sentences)
			try {
				if (sentence.length() > 2)
					if (!isDomain(message.split("\\s")[0]) && Character.isUpperCase(sentence.charAt(0)) && Character.isLowerCase(sentence.charAt(2)))
						sentence = sentence.charAt(0) + sentence.substring(1, 2).toLowerCase() + sentence.substring(2);

				tempMessage = tempMessage + sentence + " ";
			} catch (final NullPointerException ignored) {
			}
		return tempMessage.trim();
	}

	/**
	 * Attempts to remove all emojis from the given input
	 *
	 * @param message
	 * @return
	 * @author https://stackoverflow.com/a/32101331
	 */
	public static String removeEmoji(final String message) {
		if (message == null)
			return "";
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < message.length(); i++) {
			// Emojis are two characters long in java, e.g. a rocket emoji is "\uD83D\uDE80";
			if (i < (message.length() - 1)) {
				if (Character.isSurrogatePair(message.charAt(i), message.charAt(i + 1))) {
					// also skip the second character of the emoji
					i += 1;
					continue;
				}
			}
			builder.append(message.charAt(i));
		}
		return builder.toString();
	}


	/**
	 * The percentage uppercase letters the message has.
	 *
	 * @param message The message to check
	 * @return - The percentage of uppercase letters (from 0 to 100)
	 */
	public static double getCapsPercentage(final String message) {
		if (message.isEmpty())
			return 0;

		final String[] sentences = Common.stripColors(message).split(" ");
		String messageToCheck = "";
		double upperCount = 0;

		for (final String sentence : sentences)
			if (!isDomain(sentence))
				messageToCheck += sentence + " ";

		for (final char ch : messageToCheck.toCharArray())
			if (Character.isUpperCase(ch))
				upperCount++;

		return upperCount / messageToCheck.length();
	}

	/**
	 * How many uppercase letters the message has.
	 *
	 * @param message - The message to check
	 * @param ignored - The list of strings to ignore (whitelist)
	 * @return - How many uppercase letters are in message
	 */
	public static int getCapsInRow(final String message, final List<String> ignored) {
		if (message.isEmpty())
			return 0;

		final int[] caps = splitCaps(Common.stripColors(message), ignored);

		int sum = 0;
		int sumTemp = 0;

		for (final int i : caps)
			if (i == 1) {
				sumTemp++;
				sum = Math.max(sum, sumTemp);
			} else
				sumTemp = 0;

		return sum;
	}

	/**
	 * How many big letters the message has.
	 *
	 * @param message - The message to check
	 * @return - How many uppercase letters are in message
	 */
	public static int getCapsInRow(final String message, final Whiteblacklist list) {
		if (message.isEmpty())
			return 0;

		final int[] caps = splitCaps(Common.stripColors(message), list);

		int sum = 0;
		int sumTemp = 0;

		for (final int i : caps)
			if (i == 1) {
				sumTemp++;
				sum = Math.max(sum, sumTemp);
			} else
				sumTemp = 0;

		return sum;
	}

	/**
	 * Calculates the similarity (a double within 0.00 and 1.00) between two strings.
	 *
	 * @param first  - First string involved
	 * @param second - Second string involved
	 * @return - The similarity percentage (0.00 - 1.00)
	 */
	public static double getSimilarityPercentage(String first, String second) {
		if (first.isEmpty() && second.isEmpty())
			return 1D;

		first = removeSimilarity(first);
		second = removeSimilarity(second);

		String longer = first, shorter = second;

		if (first.length() < second.length()) { // longer should always have greater length
			longer = second;
			shorter = first;
		}

		final int longerLength = longer.length();

		if (longerLength == 0)
			return 0; /* both strings are zero length */

		return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
	}

	/*
	 * Removes any similarity traits of a message such as removing colors,
	 * making it lowercase it and removing diacritic
	 */
	private static String removeSimilarity(String message) {
		if (SimplePlugin.getInstance().similarityStripAccents())
			message = replaceDiacritic(message);

		message = replaceDiacritic(message);
		message = Common.stripColors(message);
		message = message.toLowerCase();

		return message;
	}

	/**
	 * Return true if the given string is a http(s) and/or www domain
	 *
	 * @param message - Message being checked
	 * @return - Is a domain?
	 */
	public static boolean isDomain(final String message) {
		return Common.regExMatch("(https?:\\/\\/(?:www\\.|(?!www))[^\\s\\.]+\\.[^\\s]{2,}|www\\.[^\\s]+\\.[^\\s]{2,})", message);
	}

	/**
	 * Replace special accented letters with their non-accented alternatives
	 * such as á is replaced by a
	 *
	 * @param message - Message being checked
	 * @return - Message with special accented latter replaced
	 */
	public static String replaceDiacritic(final String message) {
		return Normalizer.normalize(message, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	/**
	 * Return true if the given message contains [JSON] or any interactive part
	 *
	 * @param message - Message being checked
	 * @return - If message is JSON or any interactive part
	 */
	public static boolean isInteractive(final String message) {
		return message.startsWith("[JSON]") || message.startsWith("<toast>") || message.startsWith("<title>") || message.startsWith("<actionbar>") || message.startsWith("<bossbar>");
	}

	/**
	 * Automatically add gradient for the given string using
	 * the two colors as start/ending colors
	 *
	 * @param message - Message involved
	 * @param from    - Starting color
	 * @param to      - Ending color
	 * @return - Message with generated gradient
	 */

	public static String generateGradient(final String message, final CompChatColor from, final CompChatColor to) {
		if (!MinecraftVersion.atLeast(MinecraftVersion.V.v1_16))
			return message;

		final Color color1 = from.getColor();
		final Color color2 = to.getColor();

		final char[] letters = message.toCharArray();
		String gradient = "";

		ChatColor lastDecoration = null;

		for (int i = 0; i < letters.length; i++) {
			final char letter = letters[i];

			// Support color decoration and insert it manually after each character
			if (letter == ChatColor.COLOR_CHAR && i + 1 < letters.length) {
				final char decoration = letters[i + 1];

				if (decoration == 'k')
					lastDecoration = ChatColor.MAGIC;

				else if (decoration == 'l')
					lastDecoration = ChatColor.BOLD;

				else if (decoration == 'm')
					lastDecoration = ChatColor.STRIKETHROUGH;

				else if (decoration == 'n')
					lastDecoration = ChatColor.UNDERLINE;

				else if (decoration == 'o')
					lastDecoration = ChatColor.ITALIC;

				else if (decoration == 'r')
					lastDecoration = null;

				i++;
				continue;
			}

			final float ratio = (float) i / (float) letters.length;

			final int red = (int) (color2.getRed() * ratio + color1.getRed() * (1 - ratio));
			final int green = (int) (color2.getGreen() * ratio + color1.getGreen() * (1 - ratio));
			final int blue = (int) (color2.getBlue() * ratio + color1.getBlue() * (1 - ratio));

			final Color stepColor = new Color(red, green, blue);

			gradient += CompChatColor.of(stepColor).toString() + (lastDecoration == null ? "" : lastDecoration.toString()) + letters[i];
		}

		return gradient;
	}

	// --------------------------------------------------------------------------------
	// Helpers
	// --------------------------------------------------------------------------------

	private static int editDistance(String first, String second) {
		first = first.toLowerCase();
		second = second.toLowerCase();

		final int[] costs = new int[second.length() + 1];
		for (int i = 0; i <= first.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= second.length(); j++)
				if (i == 0)
					costs[j] = j;
				else if (j > 0) {
					int newValue = costs[j - 1];
					if (first.charAt(i - 1) != second.charAt(j - 1))
						newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
					costs[j - 1] = lastValue;
					lastValue = newValue;
				}
			if (i > 0)
				costs[second.length()] = lastValue;
		}
		return costs[second.length()];
	}

	private static int[] splitCaps(final String message, final List<String> ignored) {
		final int[] editedMsg = new int[message.length()];
		final String[] parts = message.split(" ");

		for (int i = 0; i < parts.length; i++)
			for (final String whitelisted : ignored)
				if (whitelisted.equalsIgnoreCase(parts[i]))
					parts[i] = parts[i].toLowerCase();

		for (int i = 0; i < parts.length; i++)
			if (isDomain(parts[i]))
				parts[i] = parts[i].toLowerCase();

		final String msg = StringUtils.join(parts, " ");

		for (int i = 0; i < msg.length(); i++)
			if (Character.isUpperCase(msg.charAt(i)) && Character.isLetter(msg.charAt(i)))
				editedMsg[i] = 1;
			else
				editedMsg[i] = 0;
		return editedMsg;
	}

	private static int[] splitCaps(final String message, final Whiteblacklist list) {
		final int[] editedMsg = new int[message.length()];
		final String[] parts = message.split(" ");

		for (int i = 0; i < parts.length; i++)
			if (list.isInList(parts[i]))
				parts[i] = parts[i].toLowerCase();

		for (int i = 0; i < parts.length; i++)
			if (isDomain(parts[i]))
				parts[i] = parts[i].toLowerCase();

		final String msg = StringUtils.join(parts, " ");

		for (int i = 0; i < msg.length(); i++)
			if (Character.isUpperCase(msg.charAt(i)) && Character.isLetter(msg.charAt(i)))
				editedMsg[i] = 1;
			else
				editedMsg[i] = 0;
		return editedMsg;
	}
}

/**
 * Contains information about all allowed Minecraft letters
 *
 * @deprecated new Minecraft versions support Unicode and a much broader range
 */
enum DefaultFontInfo {

	A('A', 5),
	a('a', 5),
	B('B', 5),
	b('b', 5),
	C('C', 5),
	c('c', 5),
	D('D', 5),
	d('d', 5),
	E('E', 5),
	e('e', 5),
	F('F', 5),
	f('f', 4),
	G('G', 5),
	g('g', 5),
	H('H', 5),
	h('h', 5),
	I('I', 3),
	i('i', 1),
	J('J', 5),
	j('j', 5),
	K('K', 5),
	k('k', 4),
	L('L', 5),
	l('l', 1),
	M('M', 5),
	m('m', 5),
	N('N', 5),
	n('n', 5),
	O('O', 5),
	o('o', 5),
	P('P', 5),
	p('p', 5),
	Q('Q', 5),
	q('q', 5),
	R('R', 5),
	r('r', 5),
	S('S', 5),
	s('s', 5),
	T('T', 5),
	t('t', 4),
	U('U', 5),
	u('u', 5),
	V('V', 5),
	v('v', 5),
	W('W', 5),
	w('w', 5),
	X('X', 5),
	x('x', 5),
	Y('Y', 5),
	y('y', 5),
	Z('Z', 5),
	z('z', 5),
	NUM_1('1', 5),
	NUM_2('2', 5),
	NUM_3('3', 5),
	NUM_4('4', 5),
	NUM_5('5', 5),
	NUM_6('6', 5),
	NUM_7('7', 5),
	NUM_8('8', 5),
	NUM_9('9', 5),
	NUM_0('0', 5),
	EXCLAMATION_POINT('!', 1),
	AT_SYMBOL('@', 6),
	NUM_SIGN('#', 5),
	DOLLAR_SIGN('$', 5),
	PERCENT('%', 5),
	UP_ARROW('^', 5),
	AMPERSAND('&', 5),
	ASTERISK('*', 5),
	LEFT_PARENTHESIS('(', 4),
	RIGHT_PERENTHESIS(')', 4),
	MINUS('-', 5),
	UNDERSCORE('_', 5),
	PLUS_SIGN('+', 5),
	EQUALS_SIGN('=', 5),
	LEFT_CURL_BRACE('{', 4),
	RIGHT_CURL_BRACE('}', 4),
	LEFT_BRACKET('[', 3),
	RIGHT_BRACKET(']', 3),
	COLON(':', 1),
	SEMI_COLON(';', 1),
	DOUBLE_QUOTE('"', 3),
	SINGLE_QUOTE('\'', 1),
	LEFT_ARROW('<', 4),
	RIGHT_ARROW('>', 4),
	QUESTION_MARK('?', 5),
	SLASH('/', 5),
	BACK_SLASH('\\', 5),
	LINE('|', 1),
	TILDE('~', 5),
	TICK('`', 2),
	PERIOD('.', 1),
	COMMA(',', 1),
	SPACE(' ', 3),
	DEFAULT('a', 4);

	private final char character;
	private final int length;

	DefaultFontInfo(final char character, final int length) {
		this.character = character;
		this.length = length;
	}

	public char getCharacter() {
		return this.character;
	}

	public int getLength() {
		return this.length;
	}

	public int getBoldLength() {
		if (this == DefaultFontInfo.SPACE)
			return this.getLength();
		return this.length + 1;
	}

	public static DefaultFontInfo getDefaultFontInfo(final char c) {
		for (final DefaultFontInfo dFI : DefaultFontInfo.values())
			if (dFI.getCharacter() == c)
				return dFI;

		return DefaultFontInfo.DEFAULT;
	}
}