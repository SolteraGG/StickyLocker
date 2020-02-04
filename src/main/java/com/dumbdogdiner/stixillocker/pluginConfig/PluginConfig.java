package com.dumbdogdiner.stixillocker.pluginConfig;

import com.dumbdogdiner.stixillocker.enums.AttackType;
import com.dumbdogdiner.stixillocker.enums.ProtectionType;
import com.dumbdogdiner.stixillocker.utils.Constants.ConfigKeys;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Logger;

public final class PluginConfig {
	private final Logger logger;

	private final boolean connectContainers;
	private final Set<AttackType> allowDestroyBy;
	private final Map<ProtectionType, Set<Material>> protectableMaterialsMap;
	/**
	 * Combination of the sets of all individual protection types.
	 */
	private final Set<Material> protectableMaterialsSet;

	public PluginConfig(Logger logger, FileConfiguration config) {
		this.logger = logger;

		this.connectContainers = config.getBoolean(ConfigKeys.CONNECT_CONTAINERS);

		this.allowDestroyBy = this.readAttackTypeSet(config.getStringList(ConfigKeys.ALLOW_DESTROY_BY));

		// Materials
		this.protectableMaterialsMap = new EnumMap<>(ProtectionType.class);

		this.protectableMaterialsMap.put(ProtectionType.CONTAINER, this.readMaterialSet(config.getStringList(ConfigKeys.PROTECTABLE_CONTAINERS)));
		this.protectableMaterialsMap.put(ProtectionType.DOOR, this.readMaterialSet(config.getStringList(ConfigKeys.PROTECTABLE_DOORS)));
		this.protectableMaterialsMap.put(ProtectionType.ATTACHABLE, this.readMaterialSet(config.getStringList(ConfigKeys.PROTECTABLE_ATTACHABLES)));

		this.protectableMaterialsSet = EnumSet.noneOf(Material.class);

		for (var protectableByType : protectableMaterialsMap.values()) {
			this.protectableMaterialsSet.addAll(protectableByType);
		}
	}

	/**
	 * Gets whether the given attack type can destroy protections.
	 *
	 * @param attackType
	 *            The attack type.
	 * @return True if the attack type can destroy protections, false otherwise.
	 */
	boolean canBeDestroyedBy(AttackType attackType) {
		return this.allowDestroyBy.contains(attackType);
	}

	/**
	 * Gets whether the material can be protected by any type.
	 *
	 * @param material
	 *            Material to check.
	 * @return True if the material can be protected by the given type, false
	 *         otherwise.
	 */
	boolean canBeProtected(Material material) {
		return this.protectableMaterialsSet.contains(material);
	}

	/**
	 * Gets whether the material can be protected by the given type.
	 *
	 * @param type
	 *            Protection type that must be checked for being able to protect
	 *            this type.
	 * @param material
	 *            Material to check.
	 *
	 * @return True if the material can be protected by the given type, false
	 *         otherwise.
	 */
	boolean canProtect(ProtectionType type, Material material) {
		var materials = this.protectableMaterialsMap.get(type);
		if (materials == null) {
			return false;
		}
		return materials.contains(material);
	}

	/**
	 * Gets whether containers should be connected.
	 *
	 * @return True if containers should be connected, false otherwise.
	 */
	boolean getConnectContainers() {
		return this.connectContainers;
	}

	private Set<AttackType> readAttackTypeSet(List<String> strings) {
		var materials = EnumSet.noneOf(AttackType.class);
		for (String string : strings) {
			try {
				materials.add(AttackType.valueOf(string.toUpperCase()));
			} catch (IllegalArgumentException e) {
				this.logger.warning("Unknown attack type " + string + ", ignoring");
			}
		}
		return materials;
	}

	private Set<Material> readMaterialSet(Collection<String> strings) {
		var materials = EnumSet.noneOf(Material.class);
		for (String string : strings) {
			var material = Material.matchMaterial(string);

			if (material == null) material = Material.matchMaterial(string, true);

			if (material == null) {
				this.logger.warning("Unknown material type " + string + ", ignoring");
				continue;
			}

			materials.add(material);
		}
		return materials;
	}
}
