package fr.nekotine.core.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import fr.nekotine.core.logging.NekotineLogger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import org.bukkit.Bukkit;

public class PlayerProfileUtil {

	/**
	 * Créé un profil avec un UUID random a partir de l'identifiant sur l'url
	 * textures.minecraft.net
	 *
	 * @param uriPart
	 */
	public static PlayerProfile makeProfileFromMinecraftNetUriPart(String uriPart) {
		return makeProfileFromSkinUrl("http://textures.minecraft.net/texture/" + uriPart);
	}

	public static PlayerProfile makeProfileFromSkinUrl(String url) {
		try {
			return makeProfileFromSkinUrl(URI.create(url).toURL());
		} catch (MalformedURLException e) {
			NekotineLogger.make().error("Un erreur est survenue lors de l'ajout de skull via l'url " + url, e);
			return null;
		}
	}

	public static PlayerProfile makeProfileFromSkinUrl(URL url) {
		var profile = Bukkit.createProfile(UUID.randomUUID());
		var texture = profile.getTextures();
		texture.setSkin(url);
		profile.setTextures(texture);
		return profile;
	}
}
