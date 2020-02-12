package com.dumbdogdiner.stixillocker.signs;

import com.dumbdogdiner.stixillocker.StixilLockerPlugin;
import com.dumbdogdiner.stixillocker.utils.Constants.JSONKeys;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.logging.Level;

public class JSONSign {
	private StixilLockerPlugin plugin;
	public final String firstLine;
	public final JsonObject jsonData;

	public JSONSign(StixilLockerPlugin plugin, String firstLine, JsonObject jsonData) {
		this.plugin = plugin;
		this.firstLine = Preconditions.checkNotNull(firstLine);
		this.jsonData = Preconditions.checkNotNull(jsonData);
	}

	public String toJSONString() {
		return this.jsonData.toString();
	}

	/**
	 * Checks if this sign allows everyone
	 */
	public boolean allowsEveryone() {
		var bool = this.jsonData.get(JSONKeys.EVERYONE);
		return !bool.isJsonNull() && bool.getAsJsonPrimitive().getAsBoolean();
	}

	/**
	 * Checks if this sign allows redstone
	 */
	public boolean allowsRedstone() {
		var redstone = this.jsonData.get(JSONKeys.REDSTONE);
		return !redstone.isJsonNull() && redstone.getAsJsonPrimitive().getAsBoolean();
	}

	public Set<SignPlayer> getPlayers() {
		var rawPlayers = this.jsonData.get(JSONKeys.PLAYERS);
		var players = new HashSet<SignPlayer>();

		if (!rawPlayers.isJsonArray()) return players;

		var array = rawPlayers.getAsJsonArray();

		array.forEach(element -> {
			if (!element.isJsonObject()) return;

			var obj = element.getAsJsonObject();

			var uuidString = obj.get(JSONKeys.PlayerKeys.UUID).getAsJsonPrimitive().getAsString();
			var playerName = obj.get(JSONKeys.PlayerKeys.PLAYER_NAME).getAsJsonPrimitive().getAsString();

			if (uuidString == null || playerName == null) {
				this.plugin.getLogger().log(Level.INFO, "Received JSON without a UUID or a player name: ", obj.toString());
				return;
			}

			players.add(new SignPlayer(uuidString, playerName));
		});

		return players;
	}

	public void addPlayerToSign(SignPlayer player) {
		var playerArray = this.jsonData.get(JSONKeys.PLAYERS);

		if (playerArray.isJsonArray()) {
			playerArray.getAsJsonArray().add(player.toObject());
		} else {
			var players = new JsonArray();
			players.add(player.toObject());
			this.jsonData.add(JSONKeys.PLAYERS, players);
		}
	}

	public static JSONSign getEmpty(StixilLockerPlugin plugin) {
		return new JSONSign(plugin, "", new JsonObject());
	}
}
