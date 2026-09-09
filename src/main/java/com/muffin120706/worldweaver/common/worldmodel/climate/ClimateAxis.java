package com.muffin120706.worldweaver.common.worldmodel.climate;

import java.util.Random;

/**
 * A single seed-derived direction (in radians) representing "cold -> warm"
 * across the continent. The perpendicular direction represents "dry -> wet".
 * Different seeds produce different orientations; the same seed always
 * reproduces the same orientation.
 */
public record ClimateAxis(double angleRadians) {

    public static ClimateAxis fromSeed(long climateSeed) {
        Random random = new Random(climateSeed);
        double angle = random.nextDouble() * Math.PI * 2.0;
        return new ClimateAxis(angle);
    }

    /** Unit vector pointing towards "warm". */
    public double warmDirX() {
        return Math.cos(angleRadians);
    }

    public double warmDirZ() {
        return Math.sin(angleRadians);
    }

    /** Unit vector pointing towards "wet" (perpendicular to the warm axis). */
    public double wetDirX() {
        return -Math.sin(angleRadians);
    }

    public double wetDirZ() {
        return Math.cos(angleRadians);
    }
}
