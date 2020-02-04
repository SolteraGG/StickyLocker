package com.dumbdogdiner.stixillocker.utils;

import org.bukkit.Material;
import org.bukkit.Sound;

public final class BlockSound {
	public static Sound getSoundForMaterial(Material material, boolean open) {
		switch (material) {
			case ACACIA_FENCE_GATE:
			case BIRCH_FENCE_GATE:
			case DARK_OAK_FENCE_GATE:
			case JUNGLE_FENCE_GATE:
			case SPRUCE_FENCE_GATE:
			case OAK_FENCE_GATE:
				return open ? Sound.BLOCK_FENCE_GATE_OPEN : Sound.BLOCK_FENCE_GATE_CLOSE;
			case OAK_DOOR:
			case SPRUCE_DOOR:
			case BIRCH_DOOR:
			case JUNGLE_DOOR:
			case ACACIA_DOOR:
			case DARK_OAK_DOOR:
				return open ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE;
			case IRON_DOOR:
				return open ? Sound.BLOCK_IRON_DOOR_OPEN : Sound.BLOCK_IRON_DOOR_CLOSE;
			case IRON_TRAPDOOR:
				return open ? Sound.BLOCK_IRON_TRAPDOOR_OPEN : Sound.BLOCK_IRON_DOOR_CLOSE;
			default:
				return open ? Sound.BLOCK_WOODEN_TRAPDOOR_OPEN : Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE;
		}
	}
}
