package com.muffin120706.worldweaver.common.worldmodel;

/**
 * Central definition of the bounded world's dimensions. Pure data, no
 * loader-specific dependencies, so it can be reused by Fabric/Forge ports
 * and by the generation logic itself.
 */
public final class WorldBounds {

    private WorldBounds() {
    }

    public static final double CENTER_X = 0.0;
    public static final double CENTER_Z = 0.0;

    /** Total width/depth of the playable world, in blocks. */
    public static final double SIZE = 10_000.0;

    public static final double HALF_SIZE = SIZE / 2.0;

    public static final double MIN_X = CENTER_X - HALF_SIZE;
    public static final double MAX_X = CENTER_X + HALF_SIZE;
    public static final double MIN_Z = CENTER_Z - HALF_SIZE;
    public static final double MAX_Z = CENTER_Z + HALF_SIZE;

    public static boolean isWithinBounds(double x, double z) {
        return x >= MIN_X && x <= MAX_X && z >= MIN_Z && z <= MAX_Z;
    }
}
