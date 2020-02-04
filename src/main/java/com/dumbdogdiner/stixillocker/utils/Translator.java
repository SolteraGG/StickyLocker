package com.dumbdogdiner.stixillocker.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class Translator {
	public enum Translation {
		COMMAND_CANNOT_BE_USED_BY_COMSOLE,
		COMMAND_CANNOT_EDIT_OWNER,
		COMMAND_NO_PERMISSION,
		COMMAND_PLUGIN_RELOADED,

		PROTECTION_BYPASSED,
		PROTECTION_CAN_ONLY_ADD_PROTECTION_SIGN,
		PROTECTION_CANNOT_CHANGE_SIGN,
		PROTECTION_BLOCK_HINT,
		PROTECTION_CLAIMED_BLOCK,
		PROTECTION_CLAIMED_MANUALLY,
		PROTECTION_IS_CLAIMED_BY,
		PROTECTION_NO_ACCESS,
		PROTECTION_NO_PERMISSION_FOR_CLAIM,
		PROTECTION_NOT_NEARBY,

		TAG_EVERYONE,
		TAG_REDSTONE,
		TAG_PRIVATE;


		@Override
		public String toString() {
			return this.name().replaceFirst("_", ".").toLowerCase();
		}
	}

	/**
	 * Returns the translation with the given key. If no such translation
	 * exists, the key is returned.
	 *
	 * @param key
	 *            The key of the translation.
	 * @return The translation, or the key if not found.
	 */
	public abstract String get(Translation key);

	/**
	 * Returns a list of all possible translations.
	 *
	 * @param key
	 * 			The key of the translation.
	 * @return A list of all possible translations, or the key (in a list) if not found.
	 */
	public abstract List<String> getAll(Translation key);

	/**
	 * Same as {@link #getAll(Translation)}, but with
	 * {@link ChatColor#stripColor(String)} applied.
	 *
	 * @param key
	 * 			The key of the translation.
	 * @return A list of all possible translations, or the key (in a list) if not found.
	 */
	public abstract List<String> getAllWithoutColor(Translation key);

	/**
	 * Same as {@link #get(Translation)}, but with
	 * {@link ChatColor#stripColor(String)} applied.
	 *
	 * @param key
	 *            The key of the translation.
	 * @return The translation, or the key if not found.
	 */
	public abstract String getWithoutColor(Translation key);

	/**
	 * Sends the specified message translated to the given player.
	 *
	 * @param player
	 *            The player (or console) to the send the message to.
	 * @param translation
	 *            The message to send.
	 */
	public abstract void sendMessage(CommandSender player, Translation translation);

	/**
	 * Sends the specified message translated to the given player.
	 *
	 * @param player
	 *            The player (or console) to the send the message to.
	 * @param translation
	 *            The message to send.
	 * @param parameters
	 *            Replacements for the message. {0} will be replaced by the
	 *            first parameter, etc.
	 */
	public final void sendMessage(CommandSender player, Translation translation, String... parameters) {
		String translated = get(translation);
		for (int i = 0; i < parameters.length; i++) {
			translated = translated.replace("{" + i + "}", parameters[i]);
		}
		player.sendMessage(translated);
	}
}
