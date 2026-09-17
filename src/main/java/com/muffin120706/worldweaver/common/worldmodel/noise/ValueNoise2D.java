package com.muffin120706.worldweaver.common.worldmodel.noise;

/**
 * Deterministic, dependency-free 2D value noise (hash-based, bilinear
 * interpolation). Not a Minecraft/Mojang class - written from scratch so
 * that the world-model has zero coupling to game internals.
 */
public final class ValueNoise2D {

    private final long seed;

    public ValueNoise2D(long seed) {
        this.seed = seed;
    }

    private static long hash(long seed, int x, int z) {
        long h = seed;
        h ^= (long) x * 0x9E3779B97F4A7C15L;
        h ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        h = (h ^ (h >>> 33)) * 0xFF51AFD7ED558CCDL;
        h = (h ^ (h >>> 33)) * 0xC4CEB9FE1A85EC53L;
        h ^= (h >>> 33);
        return h;
    }

    private double randomAt(int x, int z) {
        long h = hash(seed, x, z);
        return (h >>> 11) * 0x1.0p-53;
    }

    private static double smoothstep(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public double sample(double x, double z) {
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        int x1 = x0 + 1;
        int z1 = z0 + 1;

        double tx = smoothstep(x - x0);
        double tz = smoothstep(z - z0);

        double v00 = randomAt(x0, z0);
        double v10 = randomAt(x1, z0);
        double v01 = randomAt(x0, z1);
        double v11 = randomAt(x1, z1);

        double ix0 = lerp(v00, v10, tx);
        double ix1 = lerp(v01, v11, tx);
        return lerp(ix0, ix1, tz);
    }

    public double fractal(double x, double z, int octaves, double baseFrequency, double persistence) {
        double amplitude = 1.0;
        double frequency = baseFrequency;
        double sum = 0.0;
        double maxAmplitude = 0.0;

        for (int i = 0; i < octaves; i++) {
            sum += (sample(x * frequency, z * frequency) * 2.0 - 1.0) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            frequency *= 2.0;
        }

        return sum / maxAmplitude;
    }
}
