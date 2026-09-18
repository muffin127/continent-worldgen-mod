package com.muffin120706.worldweaver.common.worldmodel.region;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import com.muffin120706.worldweaver.common.worldmodel.continent.ContinentShape;

/**
 * Two-tier weighted Voronoi lookup. Land/ocean is decided first by
 * ContinentShape (the authoritative source), then the closest matching
 * site (by distance / weight) is picked only from sites of the same
 * category - guaranteeing land points never resolve to an ocean biome
 * and vice versa, regardless of how sites are geographically clustered.
 */
public final class RegionMap {

    private final ContinentShape continent;
    private final List<RegionSite> landSites = new ArrayList<>();
    private final List<RegionSite> oceanSites = new ArrayList<>();

    public RegionMap(List<RegionSite> sites, Set<ResourceKey<Biome>> oceanBiomes, ContinentShape continent) {
        this.continent = continent;
        for (RegionSite site : sites) {
            if (oceanBiomes.contains(site.biome())) {
                oceanSites.add(site);
            } else {
                landSites.add(site);
            }
        }
    }

    public RegionSite closestSite(double x, double z) {
        boolean isLand = continent.isLand(x, z);
        List<RegionSite> candidates = isLand ? landSites : oceanSites;

        RegionSite best = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (RegionSite site : candidates) {
            double dx = x - site.x();
            double dz = z - site.z();
            double dist = Math.sqrt(dx * dx + dz * dz);
            double score = dist / site.weight();

            if (score < bestScore) {
                bestScore = score;
                best = site;
            }
        }

        return best;
    }

    public ResourceKey<Biome> biomeAt(double x, double z) {
        RegionSite site = closestSite(x, z);
        return site != null ? site.biome() : null;
    }
}
