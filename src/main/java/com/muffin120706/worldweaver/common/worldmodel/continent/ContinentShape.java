package com.muffin120706.worldweaver.common.worldmodel.continent;

import com.muffin120706.worldweaver.common.worldmodel.WorldBounds;
import com.muffin120706.worldweaver.common.worldmodel.noise.ValueNoise2D;

/**
 * Seed-dependent land/ocean mask for the bounded world. Combines a radial
 * falloff, fractal noise for an irregular coastline, and a hard edge
 * penalty that forces ocean near the world border regardless of noise -
 * guaranteeing an ocean buffer instead of merely making it likely.
 */
public final class ContinentShape {

    private static final double CONTINENT_RADIUS_FRACTION = 1.35;

    private static final int OCTAVES = 4;
    private static final double BASE_FREQUENCY = 1.0 / 3000.0;
    private static final double PERSISTENCE = 0.5;

    private static final double RADIAL_WEIGHT = 0.75;
    private static final double NOISE_WEIGHT = 0.40;

    // Narrower forced-ocean band: still reaches -EDGE_PENALTY_STRENGTH
    // exactly at the true border, guaranteeing ocean there, but starts
    // closer to the edge so more of the world can be land.
    private static final double EDGE_MARGIN_START = 0.92;
    private static final double EDGE_PENALTY_STRENGTH = 3.0;

    private static final double LAND_THRESHOLD = 0.0;

    private final ValueNoise2D noise;

    public ContinentShape(long continentSeed) {
        this.noise = new ValueNoise2D(continentSeed);
    }

    public double landValue(double x, double z) {
        if (!WorldBounds.isWithinBounds(x, z)) {
            return -1.0;
        }

        double effectiveRadius = WorldBounds.HALF_SIZE * CONTINENT_RADIUS_FRACTION;
        double nx = x / effectiveRadius;
        double nz = z / effectiveRadius;
        double radial = 1.0 - Math.sqrt(nx * nx + nz * nz);

        double shapeNoise = noise.fractal(x, z, OCTAVES, BASE_FREQUENCY, PERSISTENCE);

        double value = radial * RADIAL_WEIGHT + shapeNoise * NOISE_WEIGHT;

        double fullNx = x / WorldBounds.HALF_SIZE;
        double fullNz = z / WorldBounds.HALF_SIZE;
        double distFraction = Math.sqrt(fullNx * fullNx + fullNz * fullNz);

        if (distFraction > EDGE_MARGIN_START) {
            double t = (distFraction - EDGE_MARGIN_START) / (1.0 - EDGE_MARGIN_START);
            t = Math.min(t, 1.0);
            value -= t * t * EDGE_PENALTY_STRENGTH;
        }

        return value;
    }

    public boolean isLand(double x, double z) {
        return landValue(x, z) > LAND_THRESHOLD;
    }
}
