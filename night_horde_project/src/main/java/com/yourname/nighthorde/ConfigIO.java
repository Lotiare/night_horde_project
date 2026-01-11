package com.yourname.nighthorde;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads/saves /config/night_horde.json (creates default if missing). */
public final class ConfigIO {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigIO() {}

    public static NightHordeConfig loadOrCreate(Path configDir) {
        try {
            Files.createDirectories(configDir);
            Path file = configDir.resolve("night_horde.json");

            if (!Files.exists(file)) {
                NightHordeConfig cfg = new NightHordeConfig();
                Files.writeString(file, GSON.toJson(cfg), StandardCharsets.UTF_8);
                return cfg;
            }

            String json = Files.readString(file, StandardCharsets.UTF_8);
            NightHordeConfig cfg = GSON.fromJson(json, NightHordeConfig.class);

            if (cfg == null) cfg = new NightHordeConfig();
            if (cfg.raid == null) cfg.raid = new NightHordeConfig.Raid();
            if (cfg.chat == null) cfg.chat = new NightHordeConfig.Chat();
            if (cfg.boss == null) cfg.boss = new NightHordeConfig.Boss();
            if (cfg.bossBar == null) cfg.bossBar = new NightHordeConfig.BossBar();
            if (cfg.mobTable == null || cfg.mobTable.isEmpty()) cfg.mobTable = new NightHordeConfig().mobTable;

            if (cfg.raid.minMobs < 1) cfg.raid.minMobs = 15;
            if (cfg.raid.maxMobs < cfg.raid.minMobs) cfg.raid.maxMobs = cfg.raid.minMobs;
            if (cfg.raid.lodestoneRadiusBlocks < 1) cfg.raid.lodestoneRadiusBlocks = 16;
            if (cfg.raid.spawnRadiusAroundLodestoneBlocks < 2) cfg.raid.spawnRadiusAroundLodestoneBlocks = 20;
            if (cfg.raid.maxSpawnAttemptsPerMob < 1) cfg.raid.maxSpawnAttemptsPerMob = 12;

            return cfg;
        } catch (Exception e) {
            return new NightHordeConfig();
        }
    }
}
