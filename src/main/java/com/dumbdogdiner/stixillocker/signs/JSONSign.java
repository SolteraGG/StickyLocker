package com.dumbdogdiner.stixillocker.signs;

import com.dumbdogdiner.stixillocker.utils.Constants.JSONKeys;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

public class JSONSign {
	public static final JSONSign EMPTY = new JSONSign("", new JsonObject());

	public final String firstLine;
	public final JsonObject jsonData;

	public JSONSign(String firstLine, JsonObject jsonData) {
		this.firstLine = Preconditions.checkNotNull(firstLine);
		this.jsonData = Preconditions.checkNotNull(jsonData);
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
}
