package skinsrestorer.shared.api;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import net.minecraft.util.com.google.common.collect.Iterables;
import skinsrestorer.bukkit.SkinsRestorer;
import skinsrestorer.bungee.SkinApplier;
import skinsrestorer.shared.storage.Locale;
import skinsrestorer.shared.storage.SkinStorage;
import skinsrestorer.shared.utils.MojangAPI;
import skinsrestorer.shared.utils.MojangAPI.SkinRequestException;
import skinsrestorer.shared.utils.ReflectionUtil;

public class SkinsRestorerAPI {

	/**
	 * This method is used to set player's skin.
	 * <p>
	 * Keep in mind it just sets the skin, <b>you have to apply the skin using
	 * another method! </b>
	 * <p>
	 * Method will not do anything if it fails to get the skin from MojangAPI or
	 * database!
	 * 
	 * @param playerName
	 *            = Player's nick name
	 * 
	 * @param skinName
	 *            = Skin's name
	 */
	public static void setSkin(final String playerName, final String skinName) {
		try {
			new Thread(new Runnable() {

				@Override
				public void run() {

					Object textures = null;

					try {
						textures = MojangAPI.getSkinProperty(MojangAPI.getUUID(skinName));

						if (textures == null)
							throw new SkinRequestException(Locale.NO_SKIN_DATA);

						SkinStorage.setSkinData(skinName, textures);
						SkinStorage.setPlayerSkin(playerName, skinName);
					} catch (SkinRequestException e) {
						SkinStorage.setPlayerSkin(playerName, skinName);
					}

				}

			}).run();
		} catch (Throwable t) {
			org.bukkit.entity.Player p = Iterables.getFirst(org.bukkit.Bukkit.getOnlinePlayers(), null);

			if (p != null) {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				try {
					out.writeUTF("SkinsRestorer");
					out.writeUTF(playerName);
					out.writeUTF(skinName);

					p.sendPluginMessage(SkinsRestorer.getInstance(), "BungeeCord", b.toByteArray());

					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method is used to check if player has set a skin. If player has no
	 * skin assigned (so playerName = skinName), the method will return false.
	 * Else if player has a skin assigned, returns true.
	 * 
	 * @param playerName
	 *            = Player's nick name
	 */
	public static boolean hasSkin(String playerName) {
		return SkinStorage.getPlayerSkin(playerName) != null;
	}

	/**
	 * This method is used to get player's skin name.
	 * 
	 * When player has no skin OR his skin name equals his username, returns
	 * null (this is because of cache clean ups)
	 * 
	 * @param playerName
	 *            = Player's nick name
	 */
	public static String getSkinName(String playerName) {
		return SkinStorage.getPlayerSkin(playerName);
	}

	/**
	 * Used for instant skin applying.
	 * 
	 * @param player
	 *            = Player's instance (either ProxiedPlayer or Player)
	 */
	public static void applySkin(Object player) {
		try {
			SkinsRestorer.getInstance().getFactory().updateSkin((org.bukkit.entity.Player) player);
		} catch (Throwable t) {
			try {
				ReflectionUtil.invokeMethod(SkinApplier.class, null, "applySkin", player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Used to remove player's skin.
	 * 
	 * You have to use apply method if you want instant results.
	 * 
	 * @param playername
	 *            = Player's nick name
	 * 
	 */
	public static void removeSkin(String playername) {
		SkinStorage.removePlayerSkin(playername);
	}
}