package com.muffin120706.worldweaver.common.biome;

import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Vanilla ocean biomes nearly all report the same getBaseTemperature()
 * (0.5), because Mojang distinguishes them via a separate internal noise
 * layer, not that field. This table supplies the relative "coldness" each
 * ocean's name implies, so our climate-axis placement can still order them
 * cold -> warm as expected, instead of collapsing them together.
 * Values are on the same -1..1-ish relative scale as normalized
 * temperature elsewhere, purely for ordering purposes.
 */
public final class OceanClimateOverrides {

    private OceanClimateOverrides() {
    }

    private static ResourceKey<Biome> key(String id) {
        return BiomeFamily.key(id);
    }

    public static final Map<ResourceKey<Biome>, Double> RELATIVE_TEMPERATURE = Map.of(
            key("minecraft:frozen_ocean"), -1.0,
            key("minecraft:cold_ocean"), -0.5,
            key("minecraft:ocean"), 0.0,
            key("minecraft:lukewarm_ocean"), 0.5,
            key("minecraft:warm_ocean"), 1.0
    );
}
