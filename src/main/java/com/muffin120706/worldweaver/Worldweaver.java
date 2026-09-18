package com.muffin120706.worldweaver;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

import com.muffin120706.worldweaver.common.worldmodel.WorldBounds;
import com.muffin120706.worldweaver.common.worldmodel.climate.ClimateAxis;
import com.muffin120706.worldweaver.common.worldmodel.continent.ContinentShape;
import com.muffin120706.worldweaver.common.worldmodel.region.RegionSite;
import com.muffin120706.worldweaver.common.worldmodel.region.RegionSiteGenerator;
import com.muffin120706.worldweaver.common.worldmodel.region.RegionMap;
import com.muffin120706.worldweaver.common.biome.BiomeCatalog;
import com.muffin120706.worldweaver.common.biome.BiomeClimateEntry;
import com.muffin120706.worldweaver.common.biome.BiomeFamilies;
import com.muffin120706.worldweaver.common.biome.BiomeFamily;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod(Worldweaver.MODID)
public class Worldweaver {
    public static final String MODID = "worldweaver";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public Worldweaver(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Worldweaver common setup complete");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            LOGGER.warn("Worldweaver: overworld level not found at server start, border not applied");
            return;
        }

        overworld.getWorldBorder().setCenter(WorldBounds.CENTER_X, WorldBounds.CENTER_Z);
        overworld.getWorldBorder().setSize(WorldBounds.SIZE);
        LOGGER.info("Worldweaver: world border set to {} blocks", WorldBounds.SIZE);

        long worldSeed = overworld.getSeed();

        long climateSeed = worldSeed ^ 0x436C696D6174654CL;
        ClimateAxis climateAxis = ClimateAxis.fromSeed(climateSeed);

        List<BiomeClimateEntry> biomes = BiomeCatalog.collectOverworldBiomes(event.getServer().registryAccess());
        Map<ResourceKey<Biome>, BiomeClimateEntry> climateByKey = new HashMap<>();
        for (BiomeClimateEntry entry : biomes) {
            climateByKey.put(entry.key(), entry);
        }

        long continentSeed = worldSeed ^ 0x436F6E74696E656EL;
        ContinentShape continentShape = new ContinentShape(continentSeed);

        long regionSeed = worldSeed ^ 0x5245474945454E44L;
        List<RegionSite> sites = RegionSiteGenerator.generate(regionSeed, continentShape, climateAxis, climateByKey);
        LOGGER.info("Worldweaver: generated {} region sites", sites.size());

        Set<ResourceKey<Biome>> oceanBiomes = new HashSet<>();
        for (BiomeFamily family : BiomeFamilies.ALL) {
            if (family.isOcean()) {
                oceanBiomes.add(family.primary());
                oceanBiomes.addAll(family.secondaries());
            }
        }

        RegionMap regionMap = new RegionMap(sites, oceanBiomes, continentShape);

        Map<ResourceKey<Biome>, Integer> coverage = new HashMap<>();
        int mismatchCount = 0;
        int totalSamples = 0;
        int step = 100;

        for (int x = (int) WorldBounds.MIN_X; x <= WorldBounds.MAX_X; x += step) {
            for (int z = (int) WorldBounds.MIN_Z; z <= WorldBounds.MAX_Z; z += step) {
                boolean isLand = continentShape.isLand(x, z);
                ResourceKey<Biome> biome = regionMap.biomeAt(x, z);
                if (biome == null) {
                    continue;
                }

                totalSamples++;
                coverage.merge(biome, 1, Integer::sum);

                boolean biomeIsOcean = oceanBiomes.contains(biome);
                if (biomeIsOcean == isLand) {
                    mismatchCount++;
                }
            }
        }

        LOGGER.info("Worldweaver: region coverage over {} samples (step={}), mismatches={} ({}%)",
                totalSamples, step, mismatchCount, String.format("%.2f", 100.0 * mismatchCount / totalSamples));

        final int finalTotalSamples = totalSamples;
        coverage.entrySet().stream()
                .sorted(Map.Entry.<ResourceKey<Biome>, Integer>comparingByValue().reversed())
                .forEach(e -> LOGGER.info("  {} -> {} samples ({}%)",
                        e.getKey().location(), e.getValue(),
                        String.format("%.2f", 100.0 * e.getValue() / finalTotalSamples)));
    }
}
