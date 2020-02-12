package com.dumbdogdiner.stixillocker.signs;

import com.dumbdogdiner.stixillocker.utils.Constants;
import com.google.gson.JsonObject;

import java.util.UUID;

public class SignPlayer {
	public UUID uuid;
	public String playerName;

	public SignPlayer(String uuid, String playerName) {
		this.uuid = UUID.fromString(uuid);
		this.playerName = playerName;
	}

	/**
	 * Turns this sign player into a JSON object to be stored in the JSON
	 * @return The ready-to-serialize object
	 */
	public JsonObject toObject() {
		var obj = new JsonObject();
		obj.addProperty(Constants.JSONKeys.PlayerKeys.PLAYER_NAME, this.playerName);
		obj.addProperty(Constants.JSONKeys.PlayerKeys.UUID, this.uuid.toString());
		return obj;
	}

	/**
	 * Check if a sign player is equal to this one
	 * @param other The other sign player
	 * @return True if the UUID matches
	 */
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
		if (this.getClass() != other.getClass()) return false;

		var otherPlayer = (SignPlayer) other;

		return this.uuid.equals(otherPlayer.uuid);
	}

	public String getDisplayName() {
		return this.playerName;
	}

	public String toString() {
		return this.getClass()
			.getSimpleName() + "[ UUID = " + this.uuid + ", PlayerName = " + this.playerName + " ]";
	}
}
