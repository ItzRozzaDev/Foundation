package com.itzrozzadev.fo;

import com.itzrozzadev.fo.remain.CompColor;
import com.itzrozzadev.fo.remain.CompMaterial;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.text.DecimalFormat;

@UtilityClass
public class StringUtil {
	public String capitalizeWord(String string) {
		if (string == null)
			return null;
		string = string.replaceAll("_", " ");
		final StringBuilder stringBuilder = new StringBuilder(string.length());

		for (final String word : string.split(" ")) {
			if (!word.isEmpty()) {
				stringBuilder.append(Character.toUpperCase(word.charAt(0)));
				stringBuilder.append(word.substring(1).toLowerCase());
			}
			if (!(stringBuilder.length() == string.length()))
				stringBuilder.append(" ");
		}

		return stringBuilder.toString();
	}

	public String formatDouble(final double amount) {
		if (amount > 0D) {
			final DecimalFormat formatter = new DecimalFormat("#,###.00");
			return formatter.format(amount).equals(".00") ? "0.00" : formatter.format(amount);

		} else if (amount < 0D) {
			final DecimalFormat formatter = new DecimalFormat("#,###.00");
			return formatter.format(amount);
		} else
			return "0.00";
	}

	public String formattedDoubleShort(final double amount) {
		if (amount != 0D) {
			final DecimalFormat formatter = new DecimalFormat("#,###");
			return formatter.format(amount);
		} else
			return "0";
	}

	public String formatLetterDouble(final double number) {
		return formatLetterDouble(number, "kMBTqQsS");
	}

	public String formatLetterDouble(final double number, final String letters) {
		if (number == 0) return "0";
		if (number < 1000) return "" + number;
		final int exponent = (int) (Math.log(number) / Math.log(1000));
		final DecimalFormat format = new DecimalFormat("0.#");
		final String value = format.format(number / Math.pow(1000, exponent));
		return String.format("%s%c", value, letters.charAt(exponent - 1));
	}


	private String materialNameCapitalize(final String materialName) {
		return capitalizeWord(materialName.toLowerCase().replace("_", " "));
	}

	public String materialNameCapitalize(final Material material) {
		return materialNameCapitalize(material.name());
	}

	public String materialNameCapitalize(final CompMaterial material) {
		return materialNameCapitalize(material.name());
	}

	public boolean isAlphanumeric(final String string) {
		final char[] charArray = string.toCharArray();
		for (final char character : charArray) {
			if (!Character.isLetterOrDigit(character))
				return false;
		}
		return true;
	}

	public String bracketText(final CompColor textColor, final String text) {
		return bracketText(CompColor.DARK_GRAY, textColor, text);
	}

	public String bracketText(final CompColor bracketColor, final CompColor textColor, final String text) {
		return bracketText(bracketColor, false, textColor, false, text);
	}

	public String bracketText(final CompColor bracketColor, final boolean bracketsBold, final CompColor textColor, final boolean textBold, final String text) {
		final ChatColor bracketChatColor = bracketColor.getChatColor();
		final ChatColor textChatColor = textColor.getChatColor();
		return (bracketsBold ? bracketChatColor + "" + ChatColor.BOLD : bracketChatColor) + "[&r" + (textBold ? textChatColor + "" + ChatColor.BOLD : textChatColor) + text + "&r" + bracketChatColor + "]";
	}

}
