package com.muffin120706.worldweaver.common.biome;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * A single Overworld-compatible biome discovered in the registry, together
 * with the climate data it already carries (temperature, downfall).
 */
public record BiomeClimateEntry(
        ResourceKey<Biome> key,
        float temperature,
        float downfall
) {
    public String namespace() {
        return key.location().getNamespace();
    }

    public boolean isVanilla() {
        return "minecraft".equals(namespace());
    }
}
