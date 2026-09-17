package com.muffin120706.worldweaver;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
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
import com.muffin120706.worldweaver.common.biome.BiomeCatalog;
import com.muffin120706.worldweaver.common.biome.BiomeClimateEntry;

import java.util.List;

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

        LOGGER.info("Worldweaver: world border set to {} blocks, centered at ({}, {})",
                WorldBounds.SIZE, WorldBounds.CENTER_X, WorldBounds.CENTER_Z);

        long worldSeed = overworld.getSeed();

        long climateSeed = worldSeed ^ 0x436C696D6174654CL;
        ClimateAxis climateAxis = ClimateAxis.fromSeed(climateSeed);
        LOGGER.info("Worldweaver: climate axis angle = {} radians ({} degrees)",
                climateAxis.angleRadians(), Math.toDegrees(climateAxis.angleRadians()));

        List<BiomeClimateEntry> biomes = BiomeCatalog.collectOverworldBiomes(event.getServer().registryAccess());
        LOGGER.info("Worldweaver: discovered {} Overworld-compatible biomes", biomes.size());

        long continentSeed = worldSeed ^ 0x436F6E74696E656EL;
        ContinentShape continentShape = new ContinentShape(continentSeed);

        int landCount = 0;
        int totalCount = 0;
        int step = 200;
        for (int x = (int) WorldBounds.MIN_X; x <= WorldBounds.MAX_X; x += step) {
            for (int z = (int) WorldBounds.MIN_Z; z <= WorldBounds.MAX_Z; z += step) {
                totalCount++;
                if (continentShape.isLand(x, z)) {
                    landCount++;
                }
            }
        }
        double landFraction = (double) landCount / totalCount;
        LOGGER.info("Worldweaver: continent sample grid ({} points, step={}) -> land fraction = {}",
                totalCount, step, landFraction);
        LOGGER.info("Worldweaver: landValue at spawn (0,0) = {}", continentShape.landValue(0, 0));

        // Worst-case check: sample a ring close to the border at many angles,
        // find the highest (most land-like) landValue - this is the real
        // risk indicator for land touching the border, not just one point.
        double ringRadius = WorldBounds.HALF_SIZE * 0.98;
        double maxNearBorder = Double.NEGATIVE_INFINITY;
        double maxAngleDeg = 0;
        int ringSamples = 72;
        for (int i = 0; i < ringSamples; i++) {
            double angle = 2.0 * Math.PI * i / ringSamples;
            double x = ringRadius * Math.cos(angle);
            double z = ringRadius * Math.sin(angle);
            double value = continentShape.landValue(x, z);
            if (value > maxNearBorder) {
                maxNearBorder = value;
                maxAngleDeg = Math.toDegrees(angle);
            }
        }
        LOGGER.info("Worldweaver: worst-case landValue near border (radius={}, {} angles) = {} at angle {} degrees",
                ringRadius, ringSamples, maxNearBorder, maxAngleDeg);
    }
}
