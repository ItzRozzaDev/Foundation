package com.itzrozzadev.fo.menu.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

@UtilityClass
@FieldDefaults(makeFinal = true)
public class MojangUtils {

	protected static String STEVE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTU5MTU3NDcyMzc4MywKICAicHJvZmlsZUlkIiA6ICI4NjY3YmE3MWI4NWE0MDA0YWY1NDQ1N2E5NzM0ZWVkNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdGV2ZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82ZDNiMDZjMzg1MDRmZmMwMjI5Yjk0OTIxNDdjNjlmY2Y1OWZkMmVkNzg4NWY3ODUwMjE1MmY3N2I0ZDUwZGUxIgogICAgfSwKICAgICJDQVBFIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NTNjYWM4Yjc3OWZlNDEzODNlNjc1ZWUyYjg2MDcxYTcxNjU4ZjIxODBmNTZmYmNlOGFhMzE1ZWE3MGUyZWQ2IgogICAgfQogIH0KfQ==";

	public static final String SERVICE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

	public String getTextureHash(final UUID uuid) {
		return fetch(uuid);
	}

	public String fetch(final UUID uuid) {
		try {
			final String out = fetch0(uuid);
			if (isValid(out)) {
				return out;
			}
		} catch (final Throwable throwable) {
			System.err.println(
					"If you aren't in online mode, disable it in your settings.yml as well!"
			);
			System.err.println("Using Steve-Texture as default");
			throwable.printStackTrace();
		}
		return STEVE_TEXTURE;
	}

	private String fetch0(final UUID uuid) throws Exception {
		System.out.println("uuid: " + uuid);
		final URL url_1 = new URL(
				SERVICE_URL + uuid
						+ "?unsigned=false");
		final InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
		final JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject()
				.get("properties").getAsJsonArray().get(0).getAsJsonObject();

		return textureProperty.get("value").getAsString();
	}

	private boolean isValid(@Nullable final Object hash) {
		if (!(hash instanceof String)) {
			return false;
		}

		return !((String) hash).isEmpty();
	}
}