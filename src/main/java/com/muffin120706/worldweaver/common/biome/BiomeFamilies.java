package com.muffin120706.worldweaver.common.biome;

import java.util.List;

/**
 * Curated list of biome families for the base game. Mountain-peak biomes
 * (frozen_peaks, jagged_peaks, snowy_slopes, grove) are intentionally
 * excluded here - they are vertical layers of a single mountain massif,
 * handled separately by the mountain-generation logic, not horizontal
 * neighbouring regions. Cave biomes and beach/river biomes are likewise
 * excluded, for the same reason discussed for the mountain layer.
 */
public final class BiomeFamilies {

    private BiomeFamilies() {
    }

    public static final List<BiomeFamily> ALL = List.of(
            BiomeFamily.land("snow", "minecraft:snowy_plains", "minecraft:snowy_beach"),
            BiomeFamily.land("ice_spikes", "minecraft:ice_spikes"),
            BiomeFamily.land("taiga", "minecraft:taiga",
                    "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga", "minecraft:snowy_taiga"),
            BiomeFamily.land("forest", "minecraft:forest",
                    "minecraft:flower_forest", "minecraft:old_growth_birch_forest", "minecraft:birch_forest"),
            BiomeFamily.land("dark_forest", "minecraft:dark_forest"),
            BiomeFamily.land("plains", "minecraft:plains", "minecraft:sunflower_plains"),
            BiomeFamily.land("swamp", "minecraft:swamp", "minecraft:mangrove_swamp"),
            BiomeFamily.land("jungle", "minecraft:jungle", "minecraft:bamboo_jungle", "minecraft:sparse_jungle"),
            BiomeFamily.land("savanna", "minecraft:savanna",
                    "minecraft:savanna_plateau", "minecraft:windswept_savanna"),
            BiomeFamily.land("desert", "minecraft:desert"),
            BiomeFamily.land("badlands", "minecraft:badlands",
                    "minecraft:eroded_badlands", "minecraft:wooded_badlands"),
            BiomeFamily.land("hills", "minecraft:windswept_hills",
                    "minecraft:windswept_gravelly_hills", "minecraft:windswept_forest", "minecraft:stony_peaks"),
            BiomeFamily.land("mushroom_fields", "minecraft:mushroom_fields"),
            BiomeFamily.land("cherry_grove", "minecraft:cherry_grove"),
            BiomeFamily.ocean("ocean", "minecraft:ocean",
                    "minecraft:cold_ocean", "minecraft:lukewarm_ocean", "minecraft:warm_ocean", "minecraft:frozen_ocean")
    );
}
