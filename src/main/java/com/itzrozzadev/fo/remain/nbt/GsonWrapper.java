package com.itzrozzadev.fo.remain.nbt;

import com.google.gson.Gson;

class GsonWrapper {

	/**
	 * Private constructor
	 */
	private GsonWrapper() {

	}

	private static final Gson gson = new Gson();

	/**
	 * Turns Objects into Json Strings
	 *
	 * @param obj
	 * @return Json, representing the Object
	 */
	public static String getString(final Object obj) {
		return gson.toJson(obj);
	}

	/**
	 * Creates an Object of the given type using the Json String
	 *
	 * @param json
	 * @param type
	 * @return Object that got created, or null if the json is null
	 */
	public static <T> T deserializeJson(final String json, final Class<T> type) {
		try {
			if (json == null) {
				return null;
			}

			final T obj = gson.fromJson(json, type);
			return type.cast(obj);
		} catch (final Exception ex) {
			throw new NbtApiException("Error while converting json to " + type.getName(), ex);
		}
	}

}