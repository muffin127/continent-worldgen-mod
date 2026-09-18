package com.muffin120706.worldweaver.common.worldmodel.region;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * A single placed Voronoi site: one biome's region centre, with a weight
 * controlling how much territory it claims relative to other sites.
 */
public record RegionSite(
        ResourceKey<Biome> biome,
        double x,
        double z,
        double weight,
        boolean primary,
        String familyName
) {
}
