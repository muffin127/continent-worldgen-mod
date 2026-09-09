package com.muffin120706.worldweaver.common.biome;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

/**
 * Scans the biome registry for every Overworld-compatible biome (vanilla or
 * modded) and extracts its built-in climate data. This is the raw input for
 * region placement; it does not decide positions or shapes.
 */
public final class BiomeCatalog {

    private BiomeCatalog() {
    }

    public static List<BiomeClimateEntry> collectOverworldBiomes(HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<Biome> biomeLookup =
                registries.lookupOrThrow(Registries.BIOME);

        List<BiomeClimateEntry> result = new ArrayList<>();

        biomeLookup.listElements().forEach(holder -> {
            if (!holder.is(BiomeTags.IS_OVERWORLD)) {
                return;
            }

            ResourceKey<Biome> key = holder.unwrapKey().orElse(null);
            if (key == null) {
                return;
            }

            Biome biome = holder.value();
            float temperature = biome.getBaseTemperature();
            float downfall = biome.getModifiedClimateSettings().downfall();

            result.add(new BiomeClimateEntry(key, temperature, downfall));
        });

        return result;
    }
}
