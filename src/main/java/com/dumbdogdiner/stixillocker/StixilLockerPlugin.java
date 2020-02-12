package com.dumbdogdiner.stixillocker;

import com.dumbdogdiner.stixillocker.pluginConfig.PluginConfig;
import com.dumbdogdiner.stixillocker.pluginConfig.Translations;
import com.dumbdogdiner.stixillocker.signs.SignManager;
import com.dumbdogdiner.stixillocker.utils.Constants;
import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class StixilLockerPlugin extends JavaPlugin {
	public SignManager signManager;
	public Translations translations;
	public PluginConfig config;

	@Override
	public void onEnable() {
		try {
			this.signManager = new SignManager(this);
		} catch (Throwable t) {
			this.getLogger().log(
				Level.SEVERE,
				"This Minecraft version is not supported. DM Vladdy#0002 ASAP",
				t
			);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		this.loadInternals();
		this.registerEvents();
	}

	public void reload() {
		this.reloadConfig();
		this.loadInternals();
	}

	private void loadInternals() {
		this.saveDefaultConfig();
		this.config = new PluginConfig(this.getLogger(), this.getConfig());

		this.translations = this.loadTranslations();
	}

	private void registerEvents() {
		var plugins = Bukkit.getPluginManager();
		// TODO: Register events for block break, place, interact and sign right click
		// TODO: /stixillocker
	}

	private Translations loadTranslations() {
		var file = new File(this.getDataFolder(), Constants.TRANSLATION_FILE);
		var config = YamlConfiguration.loadConfiguration(file);
		config.addDefaults(this.getJarTranslationConfig());

		var translator = new Translations(config);

		if (translator.needsSave()) {
			this.getLogger().info("Saving translations");
			try {
				translator.save(file);
			} catch (IOException e) {
				this.getLogger().log(
					Level.SEVERE,
					"Failed to save translation file",
					e
				);
			}
		}

		return translator;
	}

	private Configuration getJarTranslationConfig() {
		var resource = this.getResource(Constants.TRANSLATION_FILE);

		if (resource == null) return new YamlConfiguration();

		var reader = new InputStreamReader(resource, Charsets.UTF_8);
		var config = YamlConfiguration.loadConfiguration(reader);

		try {
			reader.close();
		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Failed to close stream", e);
		}

		return config;
	}
}
