package com.muffin120706.worldweaver.common.worldmodel.region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import com.muffin120706.worldweaver.common.biome.BiomeClimateEntry;
import com.muffin120706.worldweaver.common.biome.BiomeFamilies;
import com.muffin120706.worldweaver.common.biome.BiomeFamily;
import com.muffin120706.worldweaver.common.biome.OceanClimateOverrides;
import com.muffin120706.worldweaver.common.worldmodel.WorldBounds;
import com.muffin120706.worldweaver.common.worldmodel.climate.ClimateAxis;
import com.muffin120706.worldweaver.common.worldmodel.continent.ContinentShape;

/**
 * Places one primary site per biome family along the climate axis
 * (temperature/downfall -> position), enforcing a minimum separation
 * between primaries of different families. Secondary biomes are placed
 * near their primary; ocean secondaries specifically are placed along the
 * climate axis using OceanClimateOverrides, since vanilla ocean biomes
 * mostly share the same getBaseTemperature().
 */
public final class RegionSiteGenerator {

    private static final double PLACEMENT_RADIUS = WorldBounds.HALF_SIZE * 0.6;
    private static final double SECONDARY_MIN_OFFSET = 300.0;
    private static final double SECONDARY_MAX_OFFSET = 800.0;
    private static final double SECONDARY_MIN_FACTOR = 2.0;
    private static final double SECONDARY_MAX_FACTOR = 4.0;
    private static final double OCEAN_CLIMATE_SPREAD = 1200.0;

    private static final double MIN_PRIMARY_SEPARATION = 900.0;

    private static final double TEMP_MID = 0.65;
    private static final double TEMP_RANGE = 1.35;
    private static final double DOWNFALL_MID = 0.5;
    private static final double DOWNFALL_RANGE = 0.5;

    private RegionSiteGenerator() {
    }

    public static List<RegionSite> generate(
            long seed,
            ContinentShape continent,
            ClimateAxis axis,
            Map<ResourceKey<Biome>, BiomeClimateEntry> climateByKey
    ) {
        List<RegionSite> sites = new ArrayList<>();
        List<double[]> placedPrimaries = new ArrayList<>();
        Random rng = new Random(seed);

        for (BiomeFamily family : BiomeFamilies.ALL) {
            BiomeClimateEntry primaryClimate = climateByKey.get(family.primary());
            if (primaryClimate == null) {
                continue;
            }

            double[] ideal = idealPosition(primaryClimate, axis);
            double[] primaryPos = findPrimarySite(ideal[0], ideal[1], continent, family.isOcean(), placedPrimaries);
            placedPrimaries.add(primaryPos);

            sites.add(new RegionSite(family.primary(), primaryPos[0], primaryPos[1], 1.0, true, family.name()));

            for (ResourceKey<Biome> secondaryKey : family.secondaries()) {
                double[] secondaryPos;
                Double oceanRelativeTemp = OceanClimateOverrides.RELATIVE_TEMPERATURE.get(secondaryKey);

                if (oceanRelativeTemp != null) {
                    double sx = primaryPos[0] + axis.warmDirX() * oceanRelativeTemp * OCEAN_CLIMATE_SPREAD;
                    double sz = primaryPos[1] + axis.warmDirZ() * oceanRelativeTemp * OCEAN_CLIMATE_SPREAD;
                    secondaryPos = findSecondarySite(sx, sz, continent, family.isOcean());
                } else {
                    double offsetAngle = rng.nextDouble() * Math.PI * 2.0;
                    double offsetDist = SECONDARY_MIN_OFFSET + rng.nextDouble() * (SECONDARY_MAX_OFFSET - SECONDARY_MIN_OFFSET);
                    double sx = primaryPos[0] + Math.cos(offsetAngle) * offsetDist;
                    double sz = primaryPos[1] + Math.sin(offsetAngle) * offsetDist;
                    secondaryPos = findSecondarySite(sx, sz, continent, family.isOcean());
                }

                double factor = SECONDARY_MIN_FACTOR + rng.nextDouble() * (SECONDARY_MAX_FACTOR - SECONDARY_MIN_FACTOR);
                double weight = 1.0 / factor;

                sites.add(new RegionSite(secondaryKey, secondaryPos[0], secondaryPos[1], weight, false, family.name()));
            }
        }

        return sites;
    }

    private static double[] idealPosition(BiomeClimateEntry climate, ClimateAxis axis) {
        double tempNorm = clamp((climate.temperature() - TEMP_MID) / TEMP_RANGE, -1.0, 1.0);
        double downNorm = clamp((climate.downfall() - DOWNFALL_MID) / DOWNFALL_RANGE, -1.0, 1.0);

        double x = axis.warmDirX() * tempNorm * PLACEMENT_RADIUS
                + axis.wetDirX() * downNorm * PLACEMENT_RADIUS * 0.5;
        double z = axis.warmDirZ() * tempNorm * PLACEMENT_RADIUS
                + axis.wetDirZ() * downNorm * PLACEMENT_RADIUS * 0.5;

        return new double[]{x, z};
    }

    private static double[] findPrimarySite(
            double idealX, double idealZ, ContinentShape continent, boolean wantOcean, List<double[]> placedPrimaries
    ) {
        if (matchesLandOcean(continent, idealX, idealZ, wantOcean) && farEnough(idealX, idealZ, placedPrimaries)) {
            return new double[]{idealX, idealZ};
        }

        for (int r = 100; r <= 6000; r += 100) {
            for (int angleDeg = 0; angleDeg < 360; angleDeg += 15) {
                double rad = Math.toRadians(angleDeg);
                double x = idealX + r * Math.cos(rad);
                double z = idealZ + r * Math.sin(rad);

                if (!WorldBounds.isWithinBounds(x, z)) {
                    continue;
                }
                if (matchesLandOcean(continent, x, z, wantOcean) && farEnough(x, z, placedPrimaries)) {
                    return new double[]{x, z};
                }
            }
        }

        return new double[]{idealX, idealZ};
    }

    private static double[] findSecondarySite(double idealX, double idealZ, ContinentShape continent, boolean wantOcean) {
        if (matchesLandOcean(continent, idealX, idealZ, wantOcean)) {
            return new double[]{idealX, idealZ};
        }

        for (int r = 100; r <= 6000; r += 100) {
            for (int angleDeg = 0; angleDeg < 360; angleDeg += 30) {
                double rad = Math.toRadians(angleDeg);
                double x = idealX + r * Math.cos(rad);
                double z = idealZ + r * Math.sin(rad);

                if (!WorldBounds.isWithinBounds(x, z)) {
                    continue;
                }
                if (matchesLandOcean(continent, x, z, wantOcean)) {
                    return new double[]{x, z};
                }
            }
        }

        return new double[]{idealX, idealZ};
    }

    private static boolean matchesLandOcean(ContinentShape continent, double x, double z, boolean wantOcean) {
        boolean isLand = continent.isLand(x, z);
        return wantOcean != isLand;
    }

    private static boolean farEnough(double x, double z, List<double[]> placedPrimaries) {
        for (double[] other : placedPrimaries) {
            double dx = x - other[0];
            double dz = z - other[1];
            if (Math.sqrt(dx * dx + dz * dz) < MIN_PRIMARY_SEPARATION) {
                return false;
            }
        }
        return true;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
