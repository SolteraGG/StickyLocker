package com.dumbdogdiner.stixillocker.signs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Sign;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class SignManager {
	private final String nmsPrefix;
	private final String obcPrefix;

	final Class<?> BlockPosition;
	final Constructor<?> BlockPosition_new;
	final Class<?> ChatComponentText;
	final Constructor<?> ChatComponentText_new;
	final Class<?> ChatHoverable;
	final Method ChatHoverable_getChatComponent;
	final Constructor<?> ChatHoverable_new;
	final Class<?> ChatModifier;
	final Method ChatModifier_getGetHoverEvent;
	final Constructor<?> ChatModifier_new;
	final Class<?> CraftChatMessage;
	final Method CraftChatMessage_fromComponent;
	final Class<?> CraftWorld;
	final Method CraftWorld_getHandle;
	final Class<Enum<?>> EnumHoverAction;
	final Object EnumHoverAction_SHOW_TEXT;
	final Class<?> IChatBaseComponent;
	final Method IChatBaseComponent_getChatModifier;
	final Method ChatModifier_setChatHoverable;
	final Class<?> TileEntitySign;
	final Field TileEntitySign_lines;
	final Class<?> WorldServer;
	final Method WorldServer_getTileEntity;

	private final JsonParser jsonParser = new JsonParser();

	public SignManager() {
		String version = getMinecraftClassVersion();
		nmsPrefix = "net.minecraft.server." + version + ".";
		obcPrefix = "org.bukkit.craftbukkit." + version + ".";

		BlockPosition = getNMSClass("BlockPosition");
		WorldServer = getNMSClass("WorldServer");
		ChatModifier = getNMSClass("ChatModifier");
		ChatHoverable = getNMSClass("ChatHoverable");
		IChatBaseComponent = getNMSClass("IChatBaseComponent");
		EnumHoverAction = getAnyNMSEnum("EnumHoverAction", "ChatHoverable$EnumHoverAction");
		TileEntitySign = getNMSClass("TileEntitySign");
		ChatComponentText = getNMSClass("ChatComponentText");

		CraftWorld = getOBCClass("CraftWorld");
		CraftChatMessage = getOBCClass("util.CraftChatMessage");

		CraftWorld_getHandle = getMethod(CraftWorld, "getHandle");
		CraftChatMessage_fromComponent = getMethod(CraftChatMessage, "fromComponent", IChatBaseComponent);
		WorldServer_getTileEntity = getMethod(WorldServer, "getTileEntity", BlockPosition);
		IChatBaseComponent_getChatModifier = getMethod(IChatBaseComponent, "getChatModifier");
		ChatModifier_setChatHoverable = getMethod(ChatModifier, "setChatHoverable", ChatHoverable);
		ChatModifier_getGetHoverEvent = getMethod(ChatModifier, "getHoverEvent");
		ChatHoverable_getChatComponent = getMethod(ChatHoverable, "b");

		ChatComponentText_new = getConstructor(ChatComponentText, String.class);
		BlockPosition_new = getConstructor(BlockPosition, int.class, int.class, int.class);
		ChatModifier_new = getConstructor(ChatModifier);
		ChatHoverable_new = getConstructor(ChatHoverable, EnumHoverAction, IChatBaseComponent);

		TileEntitySign_lines = getTileSignLines(TileEntitySign);

		EnumHoverAction_SHOW_TEXT = enumFieldShowText(EnumHoverAction);
	}

	public JSONSign getJsonData(World world, int x, int y, int z) {
		// Get sign
		Optional<?> nmsSign = toNMSSign(world, x, y, z);
		if (nmsSign.isEmpty()) return JSONSign.EMPTY;

		Object sign = nmsSign.get();

		Optional<String> secretData = getSecretData(sign);
		if (secretData.isEmpty()) return JSONSign.EMPTY;

		// Find first line
		Object firstLineObj = ((Object[]) retrieve(nmsSign.get(), TileEntitySign_lines))[0];
		String firstLine = firstLineObj == null ? "" : chatComponentToString(firstLineObj);

		// Parse and sanitize the string
		JsonElement data = jsonParser.parse(secretData.get());
		if (data.isJsonObject()) return new JSONSign(firstLine, data.getAsJsonObject());

		return JSONSign.EMPTY;
	}

	public void setJsonData(Sign sign, JsonObject jsonObject) {
		Optional<?> nmsSign = toNMSSign(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
		if (nmsSign.isEmpty()) {
			throw new RuntimeException("No sign at " + sign.getLocation());
		}

		setSecretData(nmsSign.get(), jsonObject.toString());
	}

	private Optional<?> toNMSSign(World world, int x, int y, int z) {
		Object nmsWorld = call(world, CraftWorld_getHandle);

		Object tileEntity = call(nmsWorld, WorldServer_getTileEntity, getBlockPosition(x, y,z));

		if (!TileEntitySign.isInstance(tileEntity)) return Optional.empty();

		return Optional.of(tileEntity);
	}

	private Optional<String> getSecretData(Object tileEntitySign) {
		Object line = ((Object[]) retrieve(tileEntitySign, TileEntitySign_lines))[0];
		if (line == null) return Optional.empty();

		Object chatModifier = call(line, IChatBaseComponent_getChatModifier);
		if (chatModifier == null) return Optional.empty();

		Object chatHoverable = call(chatModifier, ChatModifier_getGetHoverEvent);
		if (chatHoverable == null) return Optional.empty();

		return Optional.of(
			chatComponentToString(
				call(chatHoverable, ChatHoverable_getChatComponent)
			)
		);
	}

	private void setSecretData(Object tileEntitySign, String data) {
		Object line = ((Object[]) retrieve(tileEntitySign, TileEntitySign_lines))[0];

		Object modifier = call(line, IChatBaseComponent_getChatModifier);
		if (modifier == null) modifier = newInstance(ChatModifier_new);

		Object chatComponentText = newInstance(ChatComponentText_new, data);
		Object hoverable = newInstance(ChatHoverable_new, EnumHoverAction_SHOW_TEXT, chatComponentText);
		call(modifier, ChatModifier_setChatHoverable, hoverable);
	}

	Object getBlockPosition(int x, int y, int z) {
		return newInstance(BlockPosition_new, x, y, z);
	}

	/**
	 * Gets the Minecraft class version of the server, like "v1_8_R2".
	 *
	 * @return Minecraft class version.
	 */
	private static String getMinecraftClassVersion() {
		String serverClassName = Bukkit.getServer().getClass().getName();
		String version = serverClassName.split("\\.")[3];
		if (!version.startsWith("v")) {
			throw new AssertionError("Failed to detect Minecraft version, found " + version + " in " + serverClassName);
		}
		return version;
	}

	private String chatComponentToString(Object chatComponent) {
		return (String) invokeStatic(CraftChatMessage_fromComponent, chatComponent);
	}

	// Private functions to obtain different functions needed for sign data storage

	static Object call(Object on, Method method, Object... parameters) {
		try {
			return method.invoke(on, parameters);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static Object invokeStatic(Method method, Object... parameters) {
		return call(null, method, parameters);
	}

	static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			return clazz.getMethod(name, parameterTypes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static Field getTileSignLines(Class<?> clazz) {
		try {
			return clazz.getField("lines");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static Object retrieve(Object on, Field field) {
		try {
			return field.get(on);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
		try {
			return clazz.getConstructor(paramTypes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static Object newInstance(Constructor<?> constructor, Object... params) {
		try {
			return constructor.newInstance(params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static Object enumFieldShowText(Class<Enum<?>> enumClass) {
		try {
			Method valueof = getMethod(Enum.class, "valueOf", Class.class, String.class);
			return invokeStatic(valueof, enumClass, "SHOW_TEXT");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	Class<?> getNMSClass(String name) {
		try {
			return Class.forName(nmsPrefix + name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	Class<Enum<?>> getNMSEnum(String name) {
		Class<?> clazz = getNMSClass(name);
		if (!clazz.isEnum()) {
			throw new IllegalArgumentException(clazz + " is not an enum");
		}
		@SuppressWarnings("unchecked")
		Class<Enum<?>> enumClazz = (Class<Enum<?>>) clazz;
		return enumClazz;
	}

	Class<?> getOBCClass(String name) {
		try {
			return Class.forName(obcPrefix + name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	Class<Enum<?>> getAnyNMSEnum(String... possibleNames) {
		Exception lastException = null;
		for (String name : possibleNames) {
			try {
				return getNMSEnum(name);
			} catch (Exception e) {
				lastException = e;
			}
		}
		throw new RuntimeException(lastException);
	}
}
