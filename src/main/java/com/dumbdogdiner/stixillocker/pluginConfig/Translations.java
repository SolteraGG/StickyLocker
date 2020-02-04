package com.dumbdogdiner.stixillocker.pluginConfig;

import com.dumbdogdiner.stixillocker.utils.Translator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Translations extends Translator {
	private boolean needsSave = false;
	private final Map<Translation, TranslationValue> translations;

	public Translations(ConfigurationSection config) {
		this.translations = new EnumMap<>(Translation.class);

		for (Translation translation : Translation.values()) {
			var key = translation.toString();

			if (!this.hasSpecifiedValue(config, key)) {
				this.needsSave = true;
			}

			var values = config.getStringList(key);

			if (values.size() <= 0) values.add(config.getString(key));

			for (int i = 0; i < values.size(); i++) {
				if (values.get(i) == null) {
					values.set(i, "~~MISSING " + key + "~~");
				}
			}

			translations.put(
				translation,
				(values.size() == 1) ? new TranslationValue(values.get(0)) : new MultiTranslationValue(values)
			);
		}
	}

	private boolean hasSpecifiedValue(ConfigurationSection config, String key) {
		return !Objects.equals(config.getString(key, "def"), "def");
	}

	@Override
	public String get(Translation key) {
		return translations.get(key).colored;
	}

	@Override
	public List<String> getAll(Translation key) {
		var all = new ArrayList<String>();

		translations.get(key).getAll()
			.forEach(value -> all.add(value.colored));

		return all;
	}

	@Override
	public List<String> getAllWithoutColor(Translation key) {
		var all = new ArrayList<String>();

		translations.get(key).getAll()
			.forEach(value -> all.add(value.uncolored));

		return all;
	}

	@Override
	public String getWithoutColor(Translation key) {
		return translations.get(key).uncolored;
	}

	@Override
	public void sendMessage(CommandSender player, Translation translation) {
		player.sendMessage(get(translation));
	}

	public boolean needsSave() {
		return this.needsSave;
	}

	public void save(File file) throws IOException {
		YamlConfiguration config = new YamlConfiguration();
		for (var entry : translations.entrySet()) {
			config.set(entry.getKey().toString(), entry.getValue().original);
		}
		config.save(file);
		this.needsSave = false;
	}

	private static class TranslationValue {
		private final String original;
		private final String uncolored;
		private final String colored;

		private TranslationValue(String original) {
			this.original = original.trim();
			this.colored = ChatColor.translateAlternateColorCodes('&', original);
			this.uncolored = ChatColor.stripColor(this.colored);
		}

		public List<TranslationValue> getAll() {
			return Collections.singletonList(this);
		}
	}

	private static class MultiTranslationValue extends TranslationValue {
		private final List<TranslationValue> aliases;

		private MultiTranslationValue(List<String> aliases) {
			super(aliases.get(0));

			this.aliases = new ArrayList<>();
			for (var alias : aliases.subList(1, aliases.size())) {
				this.aliases.add(new TranslationValue(alias));
			}
		}

		@Override
		public List<TranslationValue> getAll() {
			var all = new ArrayList<TranslationValue>();

			all.add(this);

			all.addAll(this.aliases);

			return all;
		}
	}
}
