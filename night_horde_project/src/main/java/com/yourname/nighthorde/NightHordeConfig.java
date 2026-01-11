package com.yourname.nighthorde;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches /config/night_horde.json
 * Non-coders: you only edit the JSON file.
 */
public final class NightHordeConfig {

    public Raid raid = new Raid();
    public Chat chat = new Chat();
    public Boss boss = new Boss();
    public BossBar bossBar = new BossBar();

    /** Weighted mob pool for the horde (add more IDs in JSON). */
    public List<MobEntry> mobTable = new ArrayList<>(List.of(
            new MobEntry("minecraft:zombie", 40),
            new MobEntry("minecraft:skeleton", 35),
            new MobEntry("minecraft:creeper", 25)
    ));

    public static final class Raid {
        public double baseNightChancePercent = 2.0;
        public boolean doubleChanceIf2OrMoreQualifiedPlayers = true;

        public int minY = 70;
        public int lodestoneRadiusBlocks = 16;

        public int minMobs = 15;
        public int maxMobs = 50;

        public int spawnRadiusAroundLodestoneBlocks = 20;
        public int maxSpawnAttemptsPerMob = 12;

        /** Default vanilla-ish night start tick. */
        public int nightStartTick = 13000;
    }

    public static final class Chat {
        public String announceTo = "WORLD"; // WORLD or QUALIFIED
        public String messageColorCode = "§4§l";
        public List<String> messages = new ArrayList<>(List.of(
                "A chill crawls through the air... The night horde awakens.",
                "The Lodestone hums. Something answers in the dark.",
                "You hear scraping footsteps... They are coming."
        ));
    }

    public static final class Boss {
        public boolean enabled = true;
        public double chancePerRaidPercent = 35.0;

        /** FROM_MOB_TABLE or FORCED_MOB */
        public String howToChooseBoss = "FROM_MOB_TABLE";
        public String forcedBossMobId = "minecraft:zombie";

        public double healthMultiplier = 3.0;
        public double damageMultiplier = 1.5;

        public boolean glowing = true;

        /** Name: "Boss <MobName>" */
        public String namePrefix = "Boss ";
        public String nameColorCode = "§4§l";
    }

    public static final class BossBar {
        public boolean enabled = true;
        public int showWithinBlocks = 48;
        public boolean darkenSky = false;
        public boolean playMusic = false;
    }

    public static final class MobEntry {
        public String mobId;
        public int weight;

        public MobEntry() {}
        public MobEntry(String mobId, int weight) {
            this.mobId = mobId;
            this.weight = weight;
        }
    }
}
