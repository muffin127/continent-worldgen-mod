package com.muffin120706.worldweaver.common.biome;

import java.util.Arrays;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/**
 * A group of related biomes: one large "primary" region and several
 * smaller "secondary" regions placed near it. Grouping is a curated,
 * hand-picked list (not derivable automatically), so it is hardcoded here
 * rather than inferred from tags.
 */
public record BiomeFamily(
        String name,
        ResourceKey<Biome> primary,
        List<ResourceKey<Biome>> secondaries,
        boolean isOcean
) {
    private static ResourceKey<Biome> key(String id) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.parse(id));
    }

    public static BiomeFamily land(String name, String primary, String... secondaries) {
        return new BiomeFamily(name, key(primary),
                Arrays.stream(secondaries).map(BiomeFamily::key).toList(), false);
    }

    public static BiomeFamily ocean(String name, String primary, String... secondaries) {
        return new BiomeFamily(name, key(primary),
                Arrays.stream(secondaries).map(BiomeFamily::key).toList(), true);
    }
}
